package com.pizzaroof.sinfulrush.spawner;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.bonus.Bonus;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.bonus.CallbackBuilder;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.FlyingSniperEnemy;
import com.pizzaroof.sinfulrush.actors.stage.OptimizedStage;
import com.pizzaroof.sinfulrush.attacks.Armory;
import com.pizzaroof.sinfulrush.missions.MissionDataCollector;
import com.pizzaroof.sinfulrush.screens.GameplayScreen;
import com.pizzaroof.sinfulrush.util.MarkovChain;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.util.pools.Pools;
import com.pizzaroof.sinfulrush.util.Utils;

import java.util.HashMap;

/**classe usata per riempire i buchi che si formano nella creazione di piattaforme*/
public class HoleFiller {

    private static final float RECURSIVE_HOLE_PADDING = 0.7f; //deve essere >= 0.5, il padding vero e proprio è RECURSIVE_HOLE_PADDING - 0.5f

    /**mondo fisico*/
    protected World2D world2d;

    /**stage di gioco*/
    protected Stage stage;

    /**asset manager*/
    protected AssetManager assetManager;

    protected SoundManager soundManager;

    /**chi è il player? se genero nemici mi servirà...*/
    protected Player player;

    /**tutti i gruppi disponibili nello stage*/
    protected Group[] groups;

    /**per generare cose random*/
    protected RandomXS128 rand;

    /**catena di markov per creare oggetti per riempire buchi (primo oggetto del buco)*/
    protected MarkovChain<String> verticalChain;

    /**catena di markov per riempire orizzontalmente (quindi sapendo che ci sono già stati elementi)
     * NB: LE CATENE ORIZZONTALE E VERTICALE, DEVONO AVERE GLI STESSI OGGETTI*/
    protected MarkovChain<String> horizontalChain;

    /**quanti livelli di ricorsione possiamo effettuare?
     * 0=nessuno, 1=un riempimento ricorsivo, e cosi via.
     * Riempimento ricorsivo: prendo un oggetto, lo inserisco e riempio i buchi che quest'oggetto lascia
     * nel buco originale*/
    protected int recursionLevel;

    /**gruppo di tutti i nemici vivi*/
    protected Group enemiesGroup;

    /**gruppo con tutti i bonus*/
    protected Group bonusGroup;

    /**armory: può essere modificata dai bonus*/
    protected Armory armory;

    /**controller della camera: può essere usato per creare effetti vari (es: shake...)*/
    protected CameraController cameraController;

    /**per ogni bonus ci salviamo quanti ne sono attualmente presenti sullo schermo*/
    protected HashMap<String, Integer> bonusCounts;

    protected int verticalInitState;

    protected boolean canVibrate;

    /**applichiamo filtri alle armi/bonus che escono?*/
    private boolean applyFilters;

    private MissionDataCollector missionDataCollector;

    /**@param groups gruppi dei layers
     * @param enemiesGroup gruppo con tutti i nemici*/
    public HoleFiller(World2D world, Stage stage, AssetManager assetManager, SoundManager soundManager, Group[] groups, Player player, Group enemiesGroup, Group bonusGroup, CameraController cameraController, Armory armory, Preferences preferences, MissionDataCollector missionDataCollector) {
        world2d = world;
        this.stage = stage;
        this.missionDataCollector = missionDataCollector;
        this.assetManager = assetManager;
        this.groups = groups;
        this.bonusGroup = bonusGroup;
        this.player = player;
        this.armory = armory;
        this.soundManager = soundManager;

        applyFilters = true;

        this.enemiesGroup = enemiesGroup;
        this.cameraController = cameraController;

        rand = new RandomXS128();
        verticalChain = null;
        horizontalChain = null;
        bonusCounts = new HashMap<>();
        recursionLevel = 0; //di default niente ricorsione

        canVibrate = preferences.getBoolean(Constants.VIBRATIONS_PREFS);
    }
    /**funzione da chiamare quando si forma un buco*/
    public void fillHole(float minx, float miny, float maxx, float maxy) {
        fillHole(minx, miny, maxx, maxy, false, 0);
    }

    /**
     * @param horizontal in sostanza è false se è il primo elemento che stiamo mettendo nel buco, ed è true
     *                  se abbiamo già messo qualcosa (in sostanza, si formeranno, probabilmente, spazi orizzontali*/
    protected void fillHole(float minx, float miny, float maxx, float maxy, boolean horizontal, int recLevel) {
        if(verticalChain == null) //non abbiamo impostato una catena di markov da usare...
            return;
        if(recLevel > recursionLevel) //finiti i livelli di ricorsione
            return;

        try {
            String obj;
            if(!horizontal || horizontalChain == null) { //usiamo la vericale, sia se è quella giusta da usare, che se non abbiamo definito un'orizzontale
                obj = verticalChain.moveTranslatedState(); //prendi nuovo oggetto dalla catena di markov
            } else {
                //usiamo l'orizzontale, ma partendo dallo stato della verticale: è per questo che vogliamo che abbiano
                //gli stessi oggetti
                horizontalChain.setInitialState(verticalChain.getActualState());

                obj = horizontalChain.moveTranslatedState();
            }

            obj = validityCheckOnNextObject(obj); //controllo di validità su obj (ci sono situazioni particolari che non possiamo controllare con la catena di markov)
            //obj = Constants.ICE_BONUS_NAME;

            if(obj != null) { //non è l'oggetto vuoto
                //NB: se è l'oggetto vuoto, sicuro non ricorriamo, lasciamo tutto quel sottospazio vuoto

                FillerType type = Utils.getFillerType(obj);
                String pathDim = type.equals(FillerType.BONUS) ? Utils.getBonusDirectoryFromName(obj) : obj;
                Vector2 dim = Utils.getFillerObjectDimensions(pathDim); //dimensione dell'oggetto che creeremo

                if(dim.x <= maxx - minx && dim.y <= maxy - miny) { //entra nel buco... allora lo creiamo veramente (altrimenti lo lasciamo vuoto)
                    Vector2 pos = randomPositionInHole(minx, miny, maxx, maxy, dim); //posizione random nel buco
                    Vector2 meterPos = new Vector2(pos.x / world2d.getPixelPerMeter(), pos.y / world2d.getPixelPerMeter()); //posizione in metri

                    //if(obj != null && Utils.getFillerType(obj).equals(FillerType.BONUS) && Utils.getBonusDirectoryFromName(obj).equals(Constants.SWORD_BONUS_DIRECTORY))
                    //    System.out.println(obj+" "+bonusCounts.getOrDefault(obj, 0));

                    switch (type) {
                        case ENEMY: //spawnato un nemico
                            Group effectGroup = groups[GameplayScreen.EFFECTS_LAYER];

                            FlyingSniperEnemy e = (FlyingSniperEnemy) Utils.getEnemyFromDirectory(obj, assetManager, world2d, meterPos, groups[GameplayScreen.BACK_PLAYER_LAYER], effectGroup, enemiesGroup, stage, soundManager, player);
                            e.setHorDirection(pos.x < cameraController.getCameraX() ? SpriteActor.HorDirection.RIGHT : SpriteActor.HorDirection.LEFT);
                            e.setPlayer(player);
                            e.setCameraController(cameraController);
                            e.setMissionDataCollector(missionDataCollector);
                            e.setCanVibrate(canVibrate);
                            enemiesGroup.addActor(e);
                            if(stage instanceof OptimizedStage)
                                e.setEnemyCallback((OptimizedStage)stage);
                            break;

                        case BONUS: //spawnato un bonus/arma
                            Bonus b = Pools.obtainBonus(Utils.getBonusDirectoryFromName(obj), assetManager, stage, soundManager);
                            Utils.resetAnimationsId(b, obj);
                            b.init(pos, Utils.charMapIdFromName(obj));
                            b.setArmory(armory);
                            b.setEnemiesGroup(enemiesGroup);
                            b.setPlayer(player);
                            b.setMissionDataCollector(missionDataCollector);
                            b.setBonusCallback(CallbackBuilder.getBonusCallback(obj, assetManager, stage, groups[GameplayScreen.EFFECTS_LAYER], bonusCounts, cameraController, soundManager));
                            b.setBonusType(CallbackBuilder.getBonusType(obj));
                            //b.setHorDirection(Utils.randChoice(0, 1) > 0 ? SpriteActor.HorDirection.RIGHT : SpriteActor.HorDirection.LEFT);
                            bonusGroup.addActor(b);
                            onBonusSpawn(obj);
                            break;
                    }

                    objectCreatedInHole(minx, miny, maxx, maxy, pos, dim, recLevel);
                }
                else //non sono riuscito a metterlo... allora torna indietro nella catena di markov
                    if(!horizontal || horizontalChain == null)
                        verticalChain.setInitialState(verticalInitState); //ci mettiamo in fase iniziale
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*Shape shapes[] = new Shape[1]; //debug: mostra area del buco
        //float radius = (maxy - miny) / world2d.getPixelPerMeter() / 2;
        //radius = Math.min(radius, (maxx - minx)/world2d.getPixelPerMeter()/2);
        //shapes[0] = Utils.getCircleShape(radius);
        shapes[0] = Utils.getBoxShape((maxx - minx)/world2d.getPixelPerMeter(), (maxy - miny)/world2d.getPixelPerMeter());
        Vector2 pos = new Vector2((minx + maxx) / 2 / world2d.getPixelPerMeter(), (miny + maxy) / 2 / world2d.getPixelPerMeter());
        PhysicSpriteActor actor = new PhysicSpriteActor(world2d, BodyDef.BodyType.KinematicBody, 0, 0, 0, pos, true, shapes);
        stage.addActor(actor);*/
    }

    /**restituisce una posizione a caso (in pixel) per il buco definito dai min/max hole x/y (questi valori sono in pixel)
     * sapendo che l'oggetto avrà dimensione @objDim (NB: restituisce posizione centrale)*/
    protected Vector2 randomPositionInHole(float minHoleX, float minHoleY, float maxHoleX, float maxHoleY, Vector2 objDim) {
        Vector2 pos = new Vector2(); //posizione random all'interno dell'area consentita
        if((int)(maxHoleX - minHoleX - objDim.x) > 0)
            pos.x = rand.nextInt((int)(maxHoleX - minHoleX - objDim.x)) + minHoleX + objDim.x/2; //random x
        else
            pos.x = minHoleX + objDim.x / 2;
        if((int)(maxHoleY - minHoleY - objDim.y) > 0)
            pos.y = rand.nextInt((int)(maxHoleY - minHoleY - objDim.y)) + minHoleY + objDim.y/2; //random y
        else
            pos.y = minHoleY + objDim.y / 2;
        return pos;
    }

    /**funzione da chiamare quando un oggetto viene creato nel buco: permette di riciclare ancora più spazio se il buco
     * è abbastanza grande (in sostanza richiama fillHole sui punti ancora vuoti)
     * tutte le misure sono in pixel*/
    protected void objectCreatedInHole(float minHoleX, float minHoleY, float maxHoleX, float maxHoleY, Vector2 objpos, Vector2 objdim, int recLvl) {
        //buco a sinistra
        float rightX = objpos.x - objdim.x * RECURSIVE_HOLE_PADDING; //aggiungo un po' di padding (10% larghezza oggetto, NB: le coordinate sono relative al centro)
        if(rightX > minHoleX) //controllo che sia un buco valido...
            fillHole(minHoleX, minHoleY, rightX, maxHoleY, true, recLvl+1);

        //buco a destra
        float leftX = objpos.x + objdim.x * RECURSIVE_HOLE_PADDING; //aggiungo un po' di padding (10% larghezza oggetto, NB: le coordinate sono relative al centro)
        if(leftX < maxHoleX) //controlla che sia un buco valido
            fillHole(leftX, minHoleY, maxHoleX, maxHoleY, true, recLvl+1);

        //if(leftX >= maxHoleX || rightX <= minHoleX) //per i buchi in alto bisogna avere spazio
        //    return;

        float upRightX = Math.min(leftX, maxHoleX); //leftX >= maxHoleX ? maxHoleX : leftX;
        float upLeftX = Math.max(rightX, minHoleX); //rightX <= minHoleX ? minHoleX : rightX;

        //buco sopra
        float upY = objpos.y + objdim.y * RECURSIVE_HOLE_PADDING; //aggiungo un po' di padding (10% altezza oggetto, NB: le coordinate sono relative al centro)
        if(upY < maxHoleY)
            fillHole(upLeftX, upY, upRightX, maxHoleY, true, recLvl+1);

        //buco sotto
        float downY = objpos.y - objdim.y * RECURSIVE_HOLE_PADDING;
        if(downY > minHoleY)
            fillHole(upLeftX, minHoleY, upRightX, downY, true, recLvl+1);
    }

    /**setta massimo livello di ricorsione*/
    public void setMaxRecursionLevel(int recLev) {
        recursionLevel = recLev;
    }

    /**setta la catena di markov da usare verticalmente... dalla path*/
    public void setVerticalChain(String path) {
        verticalChain = MarkovChain.fromFile(path);
        verticalInitState = verticalChain == null ? 0 : verticalChain.getActualState();
    }

    public void setHorizontalChain(String path) {
        horizontalChain = MarkovChain.fromFile(path);
    }

    /**bonus obj spawnato*/
    public void onBonusSpawn(String obj) {
        if(Utils.getBonusDirectoryFromName(obj).equals(Constants.SWORD_BONUS_DIRECTORY)) {
            if(obj.equals(Constants.DOUBLE_RAGE_SWORD_BONUS_NAME)) obj = Constants.RAGE_SWORD_BONUS_NAME;
            else if(obj.equals(Constants.DOUBLE_SWORD_BONUS_NAME)) obj = Constants.SWORD_BONUS_NAME;
            int count = bonusCounts.containsKey(obj) ? bonusCounts.get(obj) : 0;
            bonusCounts.put(obj, count + 1);
        }
    }

    public int getBonusCount(String obj) {
        if(obj.equals(Constants.DOUBLE_SWORD_BONUS_NAME)) obj = Constants.SWORD_BONUS_NAME;
        else if(obj.equals(Constants.DOUBLE_RAGE_SWORD_BONUS_NAME)) obj = Constants.RAGE_SWORD_BONUS_NAME;
        return bonusCounts.containsKey(obj) ? bonusCounts.get(obj) : 0;
    }

    /**fa un controllo che il prossimo oggetto sia valido (eventualmente cambia oggetto, anche null)*/
    public String validityCheckOnNextObject(String obj) {
        if(obj == null) return null;
        if(!applyFilters) return obj;

        if(obj.equals(Constants.SWORD_BONUS_NAME)) {
            if(armory.isUsingSword(true) && armory.getFingerSword() > 1) //sta usando la rage con il massimo delle dita: questa è inutile
                return null;
            int count = (armory.isUsingSword(false) ? armory.getFingerSword() : 0) + getBonusCount(obj);
            //System.out.println(count);
            if(count > 1 || getBonusCount(obj) > 0) //può già arrivare a 2 sword (o già le ha) quindi questa è inutile
                return null;
            if(count == 1) //hai la singola: questa facciamola doppia
                return Constants.DOUBLE_SWORD_BONUS_NAME;
        } else if(obj.equals(Constants.RAGE_SWORD_BONUS_NAME)) {
            int count = (armory.isUsingSword(true) ? armory.getFingerSword() : 0) + getBonusCount(obj);
            if(count > 1 || getBonusCount(obj) > 0) //ha già il disponibile per arrivare a 2 rage sword, questa è inutile
                return null;
            if(count == 1) //hai già la singola: questa facciamola diventare doppia
                return Constants.DOUBLE_RAGE_SWORD_BONUS_NAME;
        } else if(obj.equals(Constants.SCEPTRE_BONUS_NAME)) {
            if(armory.isUsingSceptre(false) || armory.isUsingSceptre(true) || //NB: va bene sia quello con split che senza (se abbiamo già split, questo a che serve? :) )
                    getBonusCount(obj) > 0) //lo scettro è limitato a un dito: se stiamo già usando uno scettro o se ne è già uscito uno, è inutile farne uscire altri
                return null;
        } else if(obj.equals(Constants.SCEPTRE_SPLIT_BONUS_NAME)) {
            if(armory.isUsingSceptre(true) || getBonusCount(obj) > 0) //lo scettro è limitato a un dito: se stiamo già usando uno scettro o se ne è già uscito uno, è inutile farne uscire altri
                return null;
        } else if (obj.equals(Constants.GLOVE_BONUS_NAME)) {
            if(armory.isUsingGlove() || getBonusCount(obj) > 0)
                return null;
        }
        return obj;
    }

    public void setApplyFilters(boolean f) {
        applyFilters = f;
    }
}
