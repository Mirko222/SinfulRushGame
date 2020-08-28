package com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Shape;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticleEmitter;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.IceBlock;
import com.pizzaroof.sinfulrush.actors.physics.ParticleActor;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.LivingEntity;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.missions.MissionDataCollector;
import com.pizzaroof.sinfulrush.util.Pair;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.io.BufferedReader;
import java.io.IOException;

/**classe per un nemico generico
 * Un nemico è identificato univocamente da una sua cartella, che contiene il file 'animations.scml' contenente tutte le animazioni necessarie. un file
 * 'shape.txt' per lo shape del body, un file 'info.txt' che contiene informazioni extra sul nemico. le prime informazioni sono:
 * original_width drawing_width drawing_height width height dead_offeset_x dead_offset_y [characterMapsId - opzionale]
 * id_animazione_1 durata_anim1 | ... | id_animazione_n durata_anim_n (durate in secondi)
 * blood_color_1 blood_color_2 (terne rgb in [0,1]) min_blood_particles max_blood_particles blood_radius blood_path disappear_sprite instant_disappear
 * hp iceOffsetX iceOffsetY iceWidth iceHeight (NB: iceOffsetX = dobbiamo sommarlo all'x del centro per ottenere x del ghiaccio, analogo per y)*/
public class Enemy extends ParticleActor implements LivingEntity {

    protected static final int MIN_ICE_PARTICLES = 4, MAX_ICE_PARTICLES = 8;

    /**ci salviamo asset manager*/
    protected AssetManager assetManager;

    /**hp massimi del nemico*/
    protected int maxHp;

    /**punti vita del nemico*/
    protected int hp;

    /**giocatore: manteniamo un riferimento cosi il nemico sà come comportarsi*/
    protected com.pizzaroof.sinfulrush.actors.physics.game_actors.Player player;

    /**gruppo degli effetti... cioè il posto in cui dobbiamo mettere gli effetti generati da questo nemico*/
    protected Group effectGroup;
    /**gruppo dietro il nemico*/
    protected Group backgroundGroup;

    /**booleano che indica se abbiamo già fatto sparire/lo stiamo facendo, questo body*/
    private boolean disappeared;

    /**offsets per il centro da morto (in pixel) NB: y cresce verso l'alto (e sono relativi all'immagine su screen idelae)*/
    protected Vector2 deadCenterOffsets;

    /**manteniamo riferimento al controller della camera:
     * può essere utile se vogliamo modificare qualche caratteristica in base a stati del nemico.
     * Es: quando colpiamo un nemico, facciamo shake*/
    protected com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController cameraController;

    /**emitter fisico per il sangue fisico*/
    private PhysicParticleEmitter bloodEmitter;

    /**colori del sangue (ne abbiamo due perché poi li usiamo per interpolare, il modo da aggiungere un po' di casualità)*/
    private Color bloodColor1, bloodColor2;
    /**quante particelle di sangue da creare*/
    private int minBloodParticleCount, maxBloodParticleCount;
    /**raggio di spawn massimo per le particelle di sangue*/
    private float bloodParticleSpawnRadius;

    /**path dell'emitter di sangue*/
    private String bloodEmitterPath;

    /**path dell'animazione per sparire*/
    private String disappearingSpritePath;

    /**barra degli hp*/
    protected com.pizzaroof.sinfulrush.actors.HealthBar healthBar;

    /**deve saltare in aria alla morte?*/
    private boolean blowUpOnDeath;

    /**blocco di ghiaccio che congela questo nemico*/
    protected com.pizzaroof.sinfulrush.actors.IceBlock iceBlock;

    /**offset per dove spawnare il blocco di ghiaccio*/
    protected float iceOffsetX, iceOffsetY;
    /**larghezza e altezza necessaria al ghiaccio per coprire il nemico*/
    protected float iceWidth, iceHeight;

    protected EnemyCallback callback;

    /**testo per i danni*/
    protected com.pizzaroof.sinfulrush.actors.DamageFlashText damageFlashText;

    protected SoundManager soundManager;

    protected String myDirectory;

    protected boolean instantDisappear;

    protected boolean canVibrate;

    protected MissionDataCollector missionDataCollector;

    public Enemy(World2D world, SoundManager soundManager, BodyDef.BodyType bodyType, float density, float friction, float restitution, Vector2 initPosition, String dir, AssetManager asset, Stage stage, Group effectGroup, Group backgroundGroup, Shape... shapes) {
        super(world, bodyType, density, friction, restitution, initPosition, false, com.pizzaroof.sinfulrush.Constants.ENEMIES_CATEGORY_BITS,
                com.pizzaroof.sinfulrush.util.Utils.maskToNotCollideWith(com.pizzaroof.sinfulrush.Constants.PLAYER_CATEGORY_BITS, com.pizzaroof.sinfulrush.Constants.PARTICLES_CATEGORY_BITS), shapes);
        initFromDirectory(dir, asset, stage);
        myDirectory = dir;
        this.soundManager = soundManager;
        this.assetManager = asset;
        this.effectGroup = effectGroup;
        disappeared = false;
        blowUpOnDeath = false;
        canVibrate = true;

        this.backgroundGroup = backgroundGroup;

        //emitter del sangue fisico ---> spostate in initFromDirectory
        //setBloodEmitterPath(Constants.PHYSIC_PARTICLE_BLOOD);
        //setDisappearingSpritePath(Constants.DISAPPEARING_SMOKE); //di default spariamo con nuvoletta

        TextureAtlas atlas = asset.get(com.pizzaroof.sinfulrush.Constants.HEALTH_BAR_ATLAS, TextureAtlas.class);

        healthBar = new com.pizzaroof.sinfulrush.actors.HealthBar(atlas.findRegion(com.pizzaroof.sinfulrush.Constants.HEALTH_BAR_IN_CENTER_NAME), atlas.findRegion(com.pizzaroof.sinfulrush.Constants.HEALTH_BAR_IN_BORDER_NAME),
                                    atlas.findRegion(com.pizzaroof.sinfulrush.Constants.HEALTH_BAR_OUT_CENTER_NAME), atlas.findRegion(com.pizzaroof.sinfulrush.Constants.HEALTH_BAR_OUT_BORDER_NAME));
        healthBar.setGradient(new com.pizzaroof.sinfulrush.util.Pair<>(1.f, Color.GREEN), new com.pizzaroof.sinfulrush.util.Pair<>(0.5f, Color.ORANGE), new Pair<>(0f, Color.RED));
        healthBar.setRangeDimensions((int)(getWidth()/2.f), 10, (int)(getWidth()*1.1f), 35);
        healthBar.setYoffset(30);

        damageFlashText = null;
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);
        if(getHp() > 0) {
            Vector2 cPos = centerPosition();
            healthBar.setCenterPosition(cPos.x, cPos.y + getHeight() * 0.6f);
        }
        else
            if(isOnStage() && !isInCameraView()) //è ancora sullo stage ma è morto e non è nella view... rimuovilo
                remove();

        recomputeColor();
        healthBar.setSmoothHp((float) getHp() / (float) getMaxHp());
    }

    protected void recomputeColor() {
        if(getStage() instanceof ShaderStage && isOnStage() && ((ShaderStage) getStage()).isRageModeOn())
            setColor(Color.RED);
        else
            setColor(Color.WHITE);
    }

    public void setMissionDataCollector(MissionDataCollector mdc) {
        missionDataCollector = mdc;
    }

    /**setta giocatore (il "nemico" di questo nemico)*/
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**metodo da chiamare per far prendere danno al nemico*/
    @Override
    public void takeDamage(int dmg, PhysicSpriteActor attacker, Mission.BonusType damageType) {
        if(hp <= 0) return; //è già morto
        if(!Utils.actorWellVisible(cameraController.getDownUpBoundings(), this)) return; //se non è ben visibile non è attaccabile
        hp -= dmg;

        if(callback != null)
            callback.onHpChanged(this);

        if(hp <= 0)  //morto
            dying(damageType);
        else  //ancora vivo, ha solo preso danni
            hurt();
    }

    /**prende danni senza fare controlli (controlli sulla visibilità, usato per esempio da lightning)*/
    public void takeDamageUnchecked(int dmg, Mission.BonusType damageType) {
        if(hp <= 0) return; //è già morto
        hp -= dmg;

        if(callback != null)
            callback.onHpChanged(this);

        if(hp <= 0)  //morto
            dying(damageType);
        else  //ancora vivo, ha solo preso danni
            hurt();
    }

    public void takeDamage(int dmg, Mission.BonusType damageType) {
        takeDamage(dmg, null, damageType);
    }

    /**oltre a prendere danno stampa anche il danno
     * @param hitPoint punto dove è stato colpito, e dove bisogna stampare il danno
     * @param color colore del danno*/
    public void takeDamage(int dmg, Vector2 hitPoint, Color color, Mission.BonusType damageType) {
        takeDamage(dmg, damageType);
        printDamage(dmg, hitPoint, color);
    }

    /**cura questo nemico di hp health points*/
    @Override
    public void heal(int hp) {
        if(getHp() <= 0) return; //già morto
        int hp1 = getHp();
        setHp(Math.min(getMaxHp(), getHp() + hp));

        if(hp1 < getHp()) {
            if (callback != null)
                callback.onHpChanged(this);

            if(!hasHealParticleEffect())
                if(getWidth() > com.pizzaroof.sinfulrush.Constants.BIG_ENEMY_THRESHOLD || getHeight() > com.pizzaroof.sinfulrush.Constants.BIG_ENEMY_THRESHOLD) //nemico grande
                    addEffect(Constants.LARGE_HEAL_EFFECT);
                else if(getWidth() > com.pizzaroof.sinfulrush.Constants.MEDIUM_ENEMY_THRESHOLD || getHeight() > com.pizzaroof.sinfulrush.Constants.MEDIUM_ENEMY_THRESHOLD) //nemico medio
                    addEffect(Constants.MEDIUM_HEAL_EFFECT);
                else
                    addEffect(Constants.HEAL_EFFECT);
            printDamage(getHp() - hp1, Utils.randomDamagePosition(this), new Color(0.2f, 0.8f, 0.2f, 1f), "+");
        }
    }

    /**questo nemico ha già l'effetto di cura? non ne mettiamo molteplici sullo stesso nemico, per evitare di creare tante particelle che poi
     * sono in realtà inutili*/
    private boolean hasHealParticleEffect() {
        return effects.size() > 0;
    }

    /**callback appena gli hp vanno in negativo (chiamato solo una volta)*/
    protected void dying(Mission.BonusType deathType) {
        //if(cameraController != null)
        //    cameraController.incrementTrauma(DYING_TRAUMA);
        //addEffect(Constants.BLOOD_PARTICLE_EFFECT);
        updatePlayerStatsOnDeath(deathType);
        emitBlood();
        playEnemyDeath();
        if(healthBar.getStage() != null) { //se il nemico è stato one-shottato, non ha l'health bar e non serve farla apparire
            healthBar.setSmoothHp(0);
            healthBar.disappear();
        }
        if(blowUpOnDeath)
            blowUp();
    }

    protected void playEnemyDeath() {
        soundManager.enemyDeath();
    }

    /**callback per quando subisce danni*/
    protected void hurt( ) {
        //if(cameraController != null) //se conosco un camera controller... aumento il trauma shake quando vengo colpito
        //    cameraController.incrementTrauma(HURT_TRAUMA);

        if(healthBar.getStage() == null)
            effectGroup.addActor(healthBar);

        healthBar.appear();
        if(isFreezing())
            emitIceParticles();

        //addEffect(Constants.BLOOD_PARTICLE_EFFECT);
    }

    /**fa sparire nemico mettendo nuvoletta di fumo*/
    protected void disappear() {
        if(!disappeared && disappearingSpritePath != null) {
            com.pizzaroof.sinfulrush.actors.DisappearSmoke smoke = com.pizzaroof.sinfulrush.actors.DisappearSmoke.create(disappearingSpritePath, assetManager, getStage());
            //removeBody(); //rimuoviamo il corpo intanto
            smoke.setDisapperingActor(this);
            effectGroup.addActor(smoke); //aggiungiamo il fumo agli effetti
            disappeared = true;
        }
    }

    public void mustBlowUpOnDeath(boolean b) {
        blowUpOnDeath = b;
    }

    public boolean mustBlowUpOnDeath() {
        return blowUpOnDeath;
    }

    /**fa saltare in aria il nemico*/
    protected void blowUp() {
        float strenght = com.pizzaroof.sinfulrush.util.Utils.randFloat(2.3f, 4.5f);
        float mul = com.pizzaroof.sinfulrush.util.Utils.randFloat(0.4f, 0.9f);
        int dir = com.pizzaroof.sinfulrush.util.Utils.randChoice(-1, 1);
        body.setLinearVelocity(body.getLinearVelocity().x + dir * strenght * mul, body.getLinearVelocity().y + strenght);
    }

    @Override
    public boolean remove() {
        boolean r = super.remove();
        if(healthBar != null && healthBar.getStage() != null)
            healthBar.remove();
        if(iceBlock != null && iceBlock.isOnStage())
            iceBlock.remove();

        if(callback != null)
            callback.onRemoved(this);

        //System.out.println("ENEMY REMOVED "+myDirectory);

        return r;
    }

    protected void emitBlood() {
        initBloodParticleEmitter();
        if(bloodEmitter != null) { //abbiamo un emitter fisico per il sangue
            bloodEmitter.setSpawnPoint(aliveCenterPosition()); //prendiamo la posizione centrale da vivo, perché è appena morto quindi sarà ancora in atto l'animazione
            bloodEmitter.setColors(bloodColor1, bloodColor2);
            bloodEmitter.setSpawnRadius(bloodParticleSpawnRadius);
            bloodEmitter.setParticleCount(minBloodParticleCount, maxBloodParticleCount);
            bloodEmitter.generateParticles();
        }
    }

    public void setMaxHp(int hp) {
        maxHp = hp;
    }

    /**setta gli hp del nemico (dovrebbe essere usato solo per inizializzarli)*/
    public void setHp(int hp) {
        this.hp = hp;
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public int getMaxHp() {
        return maxHp;
    }

    /**callback per quando si aggiunge un'animazione
     * @param id id dell'animazione aggiunta
     * @param num numero dell'animazione nel file (se è la prima, o la seconda, etc.); va da 0 a n-1*/
    protected void onSpriterAnimationAdded(int id, int num) {
    }

    /**posizione centrale da vivo*/
    public Vector2 aliveCenterPosition() {
        return super.centerPosition();
    }

    @Override
    public Vector2 centerPosition() {
        if(getHp() > 0)
            return super.centerPosition();

        if(getHp() <= 0) { //quando è morto, usiamo gli offset per ricalcolare il centro
            float xoff = originalDirection.equals(getHorDirection()) ? deadCenterOffsets.x : getDrawingWidth() - deadCenterOffsets.x; //guarda se devi flippare la x
            return new Vector2(getX() + xoff, getY() + deadCenterOffsets.y);
        }
        return null;
    }

    public void setBloodEmitterPath(String emitterPath) {
        this.bloodEmitterPath = emitterPath;
    }

    /**setta il camera controller*/
    public void setCameraController(com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController c) {
        cameraController = c;
    }

    public CameraController getCameraController() {
        return cameraController;
    }

    /**inizializza emettitore di sangue: magari alcuni nemici hanno sangue specifico.
     * e.g: il golem ha dei sassolini fatti apposta per lui*/
    protected void initBloodParticleEmitter() {
        //di default prendiamo il sangue classico
        if(assetManager.isLoaded(bloodEmitterPath)) {
            bloodEmitter = assetManager.get(bloodEmitterPath, PhysicParticleEmitter.class);
        }
    }

    public void setDisappearingSpritePath(String path) {
        disappearingSpritePath = path;
    }

    public boolean getBlowUpOnDeath() {
        return blowUpOnDeath;
    }

    /**congela il nemico. NB: il congelamento viene gestito in maniera diversa da nemici diversi,
     * quindi bisogna fornire implementazioni per ogni nemico*/
    public void freeze() {
        if(iceBlock == null)
            createIceBlock();
        iceBlock.init();
        iceBlock.setFrozenActor(this);
        iceBlock.freeze();
        effectGroup.addActor(iceBlock);
    }

    /**la creazione dell'ice block è lasciata alle sottoclassi: possono avere diversi tipi di blocchi*/
    protected void createIceBlock() {
        //mettiamo comunque un default
        iceBlock = new IceBlock(assetManager.get(Utils.sheetEffectScmlPath(Constants.ICE_EFFECT)), getStage().getBatch(),
                0, 0.5f, 2, 0.5f, 1, 0.5f,
                        Constants.ORIGINAL_ICE_WIDTH, Constants.ORIGINAL_ICE_HEIGHT, getFreezeDuration());
    }

    /**dice se il nemico è congelato. NB: il congelamento viene gestito in maniera diversa da nemici diversi,
     * quindi bisogna fornire implementazioni per ogni nemico*/
    public boolean isFrozen() {
        return iceBlock != null && iceBlock.isFrozen();
    }

    /**congelato oppure si sta congelando*/
    public boolean isFreezing() {
        return iceBlock != null && iceBlock.isFreezing();
    }

    public float getIceOffsetX() {
        return iceOffsetX;
    }

    public float getIceOffsetY() {
        return iceOffsetY;
    }

    public float getIceWidth() {
        return iceWidth;
    }

    public float getIceHeight() {
        return iceHeight;
    }

    /**quanto dura il freeze?*/
    protected float getFreezeDuration() {
        return -1; //-1 = infinito
    }

    protected void emitIceParticles() {
        PhysicParticleEmitter iceEmitter = assetManager.get(Constants.PHYSIC_PARTICLE_BLOOD);
        iceEmitter.setSpawnPoint(aliveCenterPosition()); //prendiamo la posizione centrale da vivo, perché è appena morto quindi sarà ancora in atto l'animazione
        iceEmitter.setColors(IceBlock.MIN_ICE_COLOR, IceBlock.MAX_ICE_COLOR);
        iceEmitter.setSpawnRadius(bloodParticleSpawnRadius * 1.3f);
        iceEmitter.setParticleCount(MIN_ICE_PARTICLES, MAX_ICE_PARTICLES);
        iceEmitter.generateParticles();
    }

    /*@Override
    protected void updateSpriterAnimation(float delta) {
        if(!isFrozen())
            super.updateSpriterAnimation(delta);
    }*/

    @Override
    protected int computeSpriterAnimationSpeed(float delta) {
        if(!isFrozen())
            return super.computeSpriterAnimationSpeed(delta);
        return 0;
    }

    /**inizializza varie cose dalla directory */
    protected void initFromDirectory(String directory, AssetManager asset, Stage stage) {
        try {
            BufferedReader reader = Utils.getInternalReader(Utils.enemyInfoPath(directory));

            String strs[] = reader.readLine().split(" "); //leggo le dimensioni...
            setOriginalWidth(Float.parseFloat(strs[0]));
            setDrawingWidth(Float.parseFloat(strs[1]));
            setDrawingHeight(Float.parseFloat(strs[2]));
            setWidth(Float.parseFloat(strs[3]));
            setHeight(Float.parseFloat(strs[4]));

            //contiene poi offset per quando è morto:
            //infatti quando è morto il centro grafico potrebbe spostarsi, con questi offset siamo in grado di inviduarlo
            deadCenterOffsets = new Vector2(Float.parseFloat(strs[5]), Float.parseFloat(strs[6]));

            int charMapId = -1;
            if(strs.length > 7) //se c'è un altro valore sulla prima riga, allora è l'id della character maps
                charMapId = Integer.parseInt(strs[7]);

            strs = reader.readLine().split(" "); //ora leggo animazioni

            setSpriterData(asset.get(Utils.enemyScmlPath(directory)), stage.getBatch()); //setta data per spriter
            recomputeSpriterScale(); //ricalcola subito cose per spriter
            recomputeSpriterFlip();
            recomputePosition();

            spriterPlayer.setCharacterMaps(charMapId);
            spriterPlayer.update();

            for(int i=0; i<strs.length; i+=2) { //aggiungi tutte le animazioni
                int id = Integer.parseInt(strs[i]);
                addSpriterAnimation(id, Float.parseFloat(strs[i+1]), Animation.PlayMode.NORMAL);
                onSpriterAnimationAdded(id, i/2);
            }

            //terza riga: colore sangue
            strs = reader.readLine().split(" "); //colore sangue
            bloodColor1 = new Color(Float.parseFloat(strs[0]), Float.parseFloat(strs[1]), Float.parseFloat(strs[2]), 1.f);
            bloodColor2 = new Color(Float.parseFloat(strs[3]), Float.parseFloat(strs[4]), Float.parseFloat(strs[5]), 1.f);
            minBloodParticleCount = Integer.parseInt(strs[6]); //altri parametri su blood particles
            maxBloodParticleCount = Integer.parseInt(strs[7]);
            bloodParticleSpawnRadius = Float.parseFloat(strs[8]);
            setBloodEmitterPath(strs[9]);//path blood particles
            setDisappearingSpritePath(strs[10].equals("null") ? null : strs[10]);
            setInstantDisappear(Boolean.parseBoolean(strs[11]));

            //sulla quarta riga ci sono gli hp, iceOffsetX, iceOffsetY, iceWidth, iceHeight
            strs = reader.readLine().split(" ");
            setMaxHp(Integer.parseInt(strs[0]));
            setHp(getMaxHp());
            iceOffsetX = Float.parseFloat(strs[1]);
            iceOffsetY = Float.parseFloat(strs[2]);
            iceWidth = Float.parseFloat(strs[3]);
            iceHeight = Float.parseFloat(strs[4]);

            reader.close();
        }catch(IOException e) { //non dovrebbe accadere
            e.printStackTrace();
        }
    }

    public void setEnemyCallback(EnemyCallback callback) {
        this.callback = callback;
    }

    /**stampa danno ricevuto*/
    protected void printDamage(int dmg, Vector2 hitPoint, Color color, String prefix) {
        if(damageFlashText == null || damageFlashText.isDecreasing() || !Utils.sameColorRGB(damageFlashText.getColor(), color)) {
            damageFlashText = null;
            //damageFlashText = new DamageFlashText(assetManager.get(Constants.DEFAULT_SKIN_PATH), null);
            damageFlashText = Pools.obtainFlashText(assetManager.get(Constants.DEFAULT_SKIN_PATH, FreeTypeSkin.class));
            damageFlashText.setPosition(hitPoint.x, hitPoint.y);
            damageFlashText.setDuration(Utils.randFloat(0.6f, 1.f));
            damageFlashText.setColor(color);
            damageFlashText.setPrefix(prefix);
            final short id = damageFlashText.getId();
            damageFlashText.setOnRemoveCallback(new Runnable() {
                @Override
                public void run() {
                    if(damageFlashText != null && damageFlashText.getId() == id)
                        damageFlashText = null;
                }
            });
            effectGroup.addActor(damageFlashText);
            onDamageCreated(hitPoint);
        }
        damageFlashText.increaseDamage(dmg);
    }

    protected void printDamage(int dmg, Vector2 hitPoint, Color color) {
        printDamage(dmg, hitPoint, color, null);
    }


    protected void onDamageCreated(Vector2 hitPoint) {
    }

    protected void updatePlayerStatsOnDeath(Mission.BonusType deathType) {
        player.increaseEnemiesKilled();

        if(missionDataCollector != null) { //se è null, non registriamo missioni
            missionDataCollector.updateEnemiesKills(this, deathType, player, (ShaderStage)getStage());
        }
    }

    @Override
    public void reset() {
        super.reset();
        setHp(getMaxHp());
        healthBar.setHp(1);
        disappeared = false;
        blowUpOnDeath = false;
    }

    @Override
    public void resetY(float maxy) {
        if(body != null)
            super.resetY(maxy);
        else {
            Vector2 pos = aliveCenterPosition();
            pos.y -= maxy;
            setPositionFromCenter(pos);
        }
    }

    public String getDirectory() {
        return myDirectory;
    }

    public void setInstantDisappear(boolean b) {
        instantDisappear = b;
    }

    public void setCanVibrate(boolean b) {
        this.canVibrate = b;
    }
}
