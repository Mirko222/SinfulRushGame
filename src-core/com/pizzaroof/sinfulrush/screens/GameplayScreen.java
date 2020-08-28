
package com.pizzaroof.sinfulrush.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.ResettableY;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.JuicyPlayer;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.ScrollingBackground;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.PlatformEnemy;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticleEmitter;
import com.pizzaroof.sinfulrush.actors.stage.OptimizedStage;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;
import com.pizzaroof.sinfulrush.attacks.Armory;
import com.pizzaroof.sinfulrush.input.GameplayGesture;
import com.pizzaroof.sinfulrush.input.GameplayInputFilter;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.missions.MissionDataCollector;
import com.pizzaroof.sinfulrush.missions.MissionManager;
import com.pizzaroof.sinfulrush.spawner.HoleFiller;
import com.pizzaroof.sinfulrush.spawner.enemies.EnemySpawner;
import com.pizzaroof.sinfulrush.spawner.enemies.MarkovEnemySpawner;
import com.pizzaroof.sinfulrush.spawner.platform.HoleCatcher;
import com.pizzaroof.sinfulrush.spawner.platform.PatternPlatformSpawner;
import com.pizzaroof.sinfulrush.spawner.platform.PlatformSpawner;
import com.pizzaroof.sinfulrush.spawner.platform.UniformPlatformSpawner;
import com.pizzaroof.sinfulrush.util.Pair;
import com.pizzaroof.sinfulrush.util.PlayerPower;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.io.IOException;

/**schermata del gameplay*/
public class GameplayScreen extends AbstractScreen {

    /**a quanto può arrivare la y (in valore assoluto) prima di essere resettata?*/
    public static final float MAX_Y_COORD = 100000; //3000; per fast test (circa 400 a piattaforma)

    /**probabilità di usare pattern nello spawner di piattaforme*/
    public static final float PATTERN_PROBABILITY = 0.1f;

    /**layers considerati:
     * 1) background
     * 2) piattaforme
     * 3) personaggi in secondo piano
     * 4) personaggi in primo piano
     * 5) cover effect layer (cover sopra tutti i personaggi, ma comunque sotto gli effetti)
     * 6) effetti sopra personaggi
     * 7) boss (che deve stare sopra gli effetti perché è sopra tutti gli altri elementi dell'ambiente di giooc)
     * 8) weapon layer
     * */
    public static final int NUM_LAYERS = 8;
    /**layer di background*/
    public static final int BACKGROUND_LAYER = 0;
    /**layer delle piattaforme*/
    public static final int PLATFORM_LAYER = 1;
    /**gruppo personaggi in secondo piano*/
    public static final int BACK_PLAYER_LAYER = 2;
    /**gruppo personaggi primo piano*/
    public static final int FRONT_PLAYER_LAYER = 3;
    /**layer per cover sopra i personaggi, ma sotto gli effetti (tipo le cover delle piattaforme)*/
    public static final int COVER_PLAYER_LAYER = 4;
    /**layer degli effetti (che staranno sopra i personaggi)*/
    public static final int EFFECTS_LAYER = 5;
    /**layer per il boss*/
    public static final int BOSS_LAYER = 6;
    /**layer per le armi*/
    public static final int WEAPON_LAYER = 7;

    protected World2D world2d; //mondo 2d
    public Player player;

    /**spawner per le piattaforme*/
    protected PlatformSpawner padSpawner;

    /**individua buchi lasciati dal padSpawner*/
    protected com.pizzaroof.sinfulrush.spawner.platform.HoleCatcher holeCatcher;

    /**indica come riempire i buchi individuati dal catcher*/
    protected com.pizzaroof.sinfulrush.spawner.HoleFiller holeFiller;

    /**spawner per i nemici*/
    protected EnemySpawner enemySpawner;

    /**è uno scenario in cui si va verso l'alto o verso il basso? (true per verso l'alto...)*/
    protected boolean goingUp;

    public CameraController camController; //controllore della telecamera

    /**array dei gruppi di oggetti, usato per creare vari layer*/
    protected Group layerGroups[];

    /**gruppo con tutti i nemici vivi*/
    public Group enemiesGroup;

    /**gruppo con tutti i bonus*/
    protected Group bonusGroup;

    /**gestore delle armi dell'utente (armeria)*/
    protected Armory armory;

    protected ScrollingBackground background;

    /**quante piattaforme iniziali vanno lasciate vuote?*/
    protected static final int EMPTY_INITIAL_PLATFORMS = 1;

    /**abbiamo già chiamato la callback di morte del giocatore?*/
    private boolean playerDeadCallback;
    /**quante volte è morto il giocatore? (potrà essere 0 o 1)*/
    private int numPlayerDeaths;

    /**è un pausa?*/
    protected boolean inPause;

    protected int highscore;

    protected int numGeneratedPlatforms;

    protected Vector2 upDownBoundings;

    protected boolean canVibrate;

    public boolean canPause;

    /**cose di statistiche*/
    protected long timeStart, numKilledEnemies, numKilledFriends, numSavedFriends, timePlayed, numJumpedPlatforms, numKilledBoss;

    protected String plStr;
    protected int plCharmaps;
    protected PlayerPower ppowers;

    protected MissionDataCollector missionDataCollector;

    //DEBUG
    //private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer(); //debug use
    //private OrthographicCamera camera; //camera per debug

    public GameplayScreen(NGame game, boolean goingUp, String directory, int charmaps, PlayerPower powers) {
        super(game);
        createMissionDataCollector();
        game.getMissionManager().makeProgressBackup();
        plStr = directory;
        plCharmaps = charmaps;
        ppowers = powers;

        numKilledEnemies = getPreferences().getLong(com.pizzaroof.sinfulrush.Constants.ENEMIES_KILLED_PREFS, 0);
        numKilledFriends = getPreferences().getLong(com.pizzaroof.sinfulrush.Constants.FRIENDS_KILLED_PREFS, 0);
        numSavedFriends = getPreferences().getLong(com.pizzaroof.sinfulrush.Constants.FRIENDS_SAVED_PREFS, 0);
        numJumpedPlatforms = getPreferences().getLong(com.pizzaroof.sinfulrush.Constants.PLATFORMS_JUMPED_PREFS, 0);
        timePlayed = getPreferences().getLong(com.pizzaroof.sinfulrush.Constants.TIME_PLAYED_PREFS, 0);
        numKilledBoss = getPreferences().getLong(getLevelPrefix()+ com.pizzaroof.sinfulrush.Constants.BOSS_KILLED_PREFS, 0);

        canPause = true;

        canVibrate = getPreferences().getBoolean(com.pizzaroof.sinfulrush.Constants.VIBRATIONS_PREFS);
        upDownBoundings = new Vector2();

        beforeBuildingScreen();

        numGeneratedPlatforms = 0;
        getStage().addActor(new Image());
        playerDeadCallback = false;
        numPlayerDeaths = 0;
        Gdx.input.setCatchBackKey(true);

        world2d = game.getWorld2d();
        world2d.clear(); //pulisce world (perchè è condiviso tra più gameplay)
        Pools.cleanPools();//puliamo pools (perché contengono cose del vecchio mondo/stage)

        stage.addActor(world2d); //ora lo aggiungiamo allo stage

        layerGroups = new Group[NUM_LAYERS];
        for(int i=0; i<NUM_LAYERS; i++) {
            layerGroups[i] = new Group();
            stage.addActor(layerGroups[i]);
        }
        Group bgFrontLayer = new Group();
        stage.addActor(bgFrontLayer);

        //setta parametri dell'emitter che mancano...
        initParticlePool();

        enemiesGroup = new Group();
        bonusGroup = new Group();

        this.goingUp = goingUp;
        buildPlatformSpawner();

        armory = new Armory(stage, assetManager, world2d, layerGroups[WEAPON_LAYER], enemiesGroup, this, powers);

        this.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        try {
            padSpawner.getNextPlatformType(); //genera tipo della piattaforma
            Platform firstPlatform = padSpawner.getNextPlatform(); //prendi prima piattaforma in assoluto (ci serve per sapere la posizione iniziale del giocatore)
            buildPlayer(firstPlatform, directory, charmaps, ppowers);

            player.addPlatform(firstPlatform); //aggiungi attori
            layerGroups[PLATFORM_LAYER].addActor(firstPlatform);
            layerGroups[FRONT_PLAYER_LAYER].addActor(player);

            buildCameraController();
            getStage().addActor(camController);

            padSpawner.setPlayer(player); //abbiamo creato il giocatore... diamolo allo spawner: può usarlo per controlli più accurati

            holeCatcher = new HoleCatcher(player);
            initHoleFiller();
            //holeFiller = new HoleFiller(world2d, stage, assetManager, getSoundManager(), layerGroups, player, enemiesGroup, bonusGroup, camController, armory, getPreferences());

            initEnemySpawner();
            //inizializza catene di markov per spawner e filler
            initMarkovChains((com.pizzaroof.sinfulrush.spawner.enemies.MarkovEnemySpawner)enemySpawner, holeFiller);

            holeCatcher.setHoleFiller(holeFiller);
            addPlatformsIfNeeded(); //aggiungi tutte le piattaforme necessarie
        }catch(IOException e) { //non dovrebbe capitare
            e.printStackTrace();
        }

        layerGroups[FRONT_PLAYER_LAYER].addActor(enemiesGroup); //mettiamo i nemici sul front layer
        //layerGroups[BACK_PLAYER_LAYER].addActor(bonusGroup);
        layerGroups[FRONT_PLAYER_LAYER].addActor(bonusGroup); //bonus sul front layer
        buildBackground(camController, assetManager, bgFrontLayer);
        layerGroups[BACKGROUND_LAYER].addActor(background);

        ((ShaderStage)stage).setArmory(armory);
        armory.setPunchAttack();

        //l'input non è controllato solo dallo stage: eseguiamo codice di molti handler, in cascata
        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(new GameplayInputFilter(this));
        mux.addProcessor(stage);
        mux.addProcessor(armory);
        mux.addProcessor(new GameplayGesture(game, this, stage, bonusGroup));
        Gdx.input.setInputProcessor(mux);

        enemiesGroup.setTransform(false);
        bonusGroup.setTransform(false);
        for(int i=0; i<NUM_LAYERS; i++)
            layerGroups[i].setTransform(false);

        inPause = false;

        highscore = getPreferences().getInteger(getHighscoreName(), 0);

        assetManager.get(getSoundtrackPath(), Music.class).stop(); //la facciamo ripartire da capo
        initSoundtrack();

        timeStart = TimeUtils.millis();

        //COSE PER DEBUG!!! andranno tolte
        /*camera = new OrthographicCamera(Constants.VIRTUAL_WIDTH/Constants.PIXELS_PER_METER, Constants.VIRTUAL_HEIGHT/Constants.PIXELS_PER_METER);
        camera.position.x += Constants.VIRTUAL_WIDTH/Constants.PIXELS_PER_METER / 2; //metti camera al centro
        camera.position.y += Constants.VIRTUAL_HEIGHT/Constants.PIXELS_PER_METER / 2;
        camera.update();*/
    }

    @Override
    public void updateLogic(float delta) {
        if(getStage() == null) return;

        if(!isInPause()) {
            resetY();
            super.updateLogic(delta);

            if (!playerDeadCallback && player.getHp() <= 0) {
                playerDeadCallback = true;
                onPlayerDied(numPlayerDeaths == 0);
                numPlayerDeaths++;
            }
            if (player.getHp() > 0)
                playerDeadCallback = false;

            getSoundManager().update(delta);
        }

        if(getStage() == null) return; //in seguito a chiamata a super

        //if(Gdx.app.getType().equals(Application.ApplicationType.Android)) //rallentamento musica
        //    ((TimescaleMusic) soundtrack).setSpeed(((TimescaleStage) stage).getTimeMultiplier());

        //aggiornamenti che non dipendono dal frame rate (quindi il giochetto di sopra, non avrebbe effetti positivi)
        try {
            addPlatformsIfNeeded(); //aggiungiamo piattaforme se è necessario (stanno finendo)
        }catch(IOException e) { //non dovrebbe avvenire...
            e.printStackTrace();
        }
        //System.out.println(camController.getCameraY());
    }

    /**aggiunge nuove piattaforme se è necessario*/
    private void addPlatformsIfNeeded() throws IOException {
        int dir = goingUp ? 1 : -1;
        Platform lastPlatform = padSpawner.getLastCreatedPlatform();
        float minHeight = getMinimumYToFillWithPlatforms(); //aggiungiamo piattaforme finchè non superiamo quest'altezza
        while(dir * lastPlatform.getY() <= dir * minHeight) { //finchè non abbiamo raggiunto quell'altezza continuiamo a generare (NB: dir generalizza la condizione anche a quando si scende)
            com.pizzaroof.sinfulrush.util.Pair<String, Vector2> padType = padSpawner.getNextPlatformType(); //genera nuovo tipo internamente
            Pair<String, Vector2> enemyType = enemySpawner.getNextEnemyType(padType); //genera i nemici

            //settiamo l'altezza dei mostri sulla piattaforma (non abbiamo creato i nemici, ma abbiamo abbastanza informazioni per farlo)
            ((UniformPlatformSpawner)padSpawner).setHeightOfMonstersOnNextPlatform(enemyType.v1 == null ? 0 : enemyType.v2.y * 1.5f); //NB: ricorda che potrebbe essere il nemico nullo

            Platform oldPlatform = padSpawner.getLastCreatedPlatform();
            lastPlatform = padSpawner.getNextPlatform(); //genera nuova piattaforma
            player.addPlatform(lastPlatform); //aggiungi piattaforma al player
            layerGroups[PLATFORM_LAYER].addActor(lastPlatform); //aggiungi la piattaforma al gruppo di background

            enemySpawner.createEnemies(lastPlatform, player, world2d, assetManager, layerGroups[BACK_PLAYER_LAYER], layerGroups[EFFECTS_LAYER], enemiesGroup, stage); //crea vero nemico

            for(PlatformEnemy enemy : lastPlatform.getEnemies()) { //i nemici vengono subito aggiunti alla piattaforma
                enemy.setPreviousPlatform(oldPlatform); //settiamogli la vecchia piattaforma
                enemy.setMissionDataCollector(missionDataCollector);
                enemiesGroup.addActor(enemy); //e aggiungiamoli al gruppo di tutti i nemici
                enemy.setCameraController(camController);
                if(stage instanceof OptimizedStage)
                    enemy.setEnemyCallback((OptimizedStage)stage);
                enemy.setCanVibrate(canVibrate);
            }

            onPlatformCreated(lastPlatform);
            holeCatcher.addPlatform(lastPlatform, world2d); //aggiungi piattaforma per il catcher (qui eventualmente verrà eseguita la callback)
            numGeneratedPlatforms++;
        }
    }

    public int getNumGeneratedPlatforms() {
        return numGeneratedPlatforms;
    }

    /**minima altezza a cui riempire con piattaforme*/
    protected float getMinimumYToFillWithPlatforms() {
        int dir = goingUp ? 1 : -1;
        return dir * getStage().getCamera().viewportHeight * 0.75f + getStage().getCamera().position.y; //aggiungiamo piattaforme finchè non superiamo quest'altezza (riempiamo più di tutto lo schermo)
    }

    /**crea il pad spawner*/
    protected void buildPlatformSpawner() {
        padSpawner = new PatternPlatformSpawner(goingUp, assetManager, com.pizzaroof.sinfulrush.Constants.VIRTUAL_WIDTH, world2d, PATTERN_PROBABILITY);
        padSpawner.addPadAvailable(com.pizzaroof.sinfulrush.Constants.HELL_PAD_1); //inseriamo tutte le piattaforme
        padSpawner.addPadAvailable(com.pizzaroof.sinfulrush.Constants.HELL_PAD_2);
        padSpawner.addPadAvailable(com.pizzaroof.sinfulrush.Constants.HELL_PAD_3);
        padSpawner.addPadAvailable(com.pizzaroof.sinfulrush.Constants.HELL_PAD_4);
    }

    /**@param directory directory del player
     * @param charmaps charmaps all'interno della directory
     * @param powers eventuali poteri speciali del personaggio*/
    protected void buildPlayer(Platform firstPlatform, String directory, int charmaps, PlayerPower powers) throws IOException {
        float hplayer = Float.parseFloat(com.pizzaroof.sinfulrush.util.Utils.getInternalReader(com.pizzaroof.sinfulrush.util.Utils.playerInfoPath(com.pizzaroof.sinfulrush.Constants.THIEF_DIRECTORY)).readLine().split(" ")[1]);
        Vector2 pInitPos = new Vector2((firstPlatform.getX() + firstPlatform.getWidth()/2) / com.pizzaroof.sinfulrush.Constants.PIXELS_PER_METER, //x iniziale: centro piattaforma
                (firstPlatform.getY() + firstPlatform.getHeight() + hplayer/2) / com.pizzaroof.sinfulrush.Constants.PIXELS_PER_METER); //y iniziale: sopra piattaforma
        player = JuicyPlayer.createPlayer(directory, world2d, 1, pInitPos,1f, assetManager, stage,
                        getPreferences().getBoolean(Constants.VIBRATIONS_PREFS, true), getSoundManager(), layerGroups[EFFECTS_LAYER], powers, getSoundtrackPath());
        player.setCharacterMaps(charmaps);
    }

    /*@Override
    public void redraw() {
        super.redraw();

        float w = stage.getViewport().getWorldWidth() / world2d.getPixelPerMeter(), h = stage.getViewport().getWorldHeight() / world2d.getPixelPerMeter();
        camera = new OrthographicCamera(w, h);
        camera.position.set(stage.getCamera().position.x / world2d.getPixelPerMeter(), stage.getCamera().position.y / world2d.getPixelPerMeter(), 0);
        camera.update();
        stage.getViewport().apply();
        debugRenderer.render(world2d.getBox2DWorld(), camera.combined);
    }*/

    @Override
    public void initStage() {
        stage = new OptimizedStage(assetManager.get(com.pizzaroof.sinfulrush.Constants.MAIN_SCREEN_SHADER));
        ((ShaderStage)stage).setSoundManager(getSoundManager());

        //nel gameplay usiamo una extend viewport, perchè sarebbe troppo scomodo gestire le barre nere
        //NB: adesso la dimensione della viewport non è detto che sia VIRTUAL_WIDTHxVIRTUAL_HEIGHT (solo uno dei due lati sarà sicuramente di quella dimensione)
        //l'altro lato potrà essere più grande, perchè la extend, aumenta una dimensione per togliere le barre nere,
        //per accedere alle vere dimensioni usa stage.getCamera().viewportWidth e stage.getCamera().viewportHeight
        //NB2: puoi comunque usare le dimensioni che useresti per VIRTUAL_WIDTHxVIRTUAL_HEIGHT: infatti l'aspect ratio è conservato, cambia solo che vedi più cose nello schermo
        stage.setViewport(new ExtendViewport(com.pizzaroof.sinfulrush.Constants.VIRTUAL_WIDTH, com.pizzaroof.sinfulrush.Constants.VIRTUAL_HEIGHT, stage.getCamera()));
    }

    /**chiamato per inizializzare le pool di particelle fisiche*/
    protected void initParticlePool() {
        PhysicParticleEmitter emitter = assetManager.get(com.pizzaroof.sinfulrush.Constants.PHYSIC_PARTICLE_BLOOD, PhysicParticleEmitter.class);
        emitter.setWorld(world2d, false, true);
        emitter.setParticleGroup(layerGroups[EFFECTS_LAYER]);

        //aggiungiamo callback all'effetto lava rock2...
        /*assetManager.get(Constants.PHYSIC_PARTICLE_LAVA_ROCKS_2, PhysicParticleEmitter.class).setParticleCallback(new PPECallback() {
            @Override
            public void onCollisionWith(PhysicParticle particle, PhysicSpriteActor actor) {
                if(actor instanceof Platform) { //quando entra in collisione con una piattaforma lo facciamo esplodere
                    if (Utils.randFloat() <= 0.3f) { //30% probabilità di esplodere
                        DisappearSmoke smoke = DisappearSmoke.create(Constants.EXPLOSION_EFFECT, assetManager, getStage());
                        smoke.setDisapperingActor(particle);
                        layerGroups[EFFECTS_LAYER].addActor(smoke);
                    }
                }
            }
        });*/
    }

    /**resetta la y di tutti gli oggetti, se si va troppo in alto/basso (NB: questo perché dopo un certo valore, le cose non funzionano più bene, circa verso 10M)*/
    public void resetY() {
        float mul = player.getY() < 0 ? -1 : 1;
        if(camController.getCameraY() * mul > MAX_Y_COORD) { //superata y massima... dobbiamo fare il reset
            float maxy = mul * MAX_Y_COORD;
            background.resetY(maxy);
            armory.resetY(maxy);
            camController.resetY(maxy);
            padSpawner.resetY(maxy);

            resetGroupY(layerGroups[BACKGROUND_LAYER], maxy);
            resetGroupY(layerGroups[PLATFORM_LAYER], maxy);
            resetGroupY(layerGroups[BACK_PLAYER_LAYER], maxy);
            resetGroupY(layerGroups[FRONT_PLAYER_LAYER], maxy);
            resetGroupY(layerGroups[EFFECTS_LAYER], maxy);
            resetGroupY(layerGroups[BOSS_LAYER], maxy);
            resetGroupY(layerGroups[WEAPON_LAYER], maxy);
            resetGroupY(enemiesGroup, maxy);
            resetGroupY(bonusGroup, maxy);

            //player.resetY(MAX_Y_COORD * mul); //dentro front layer
        }
    }

    public boolean isSfxOn() {
        return getSoundManager().useSfx();
    }

    public boolean isMusicOn() {
        return getSoundManager().useMusic();
    }

    /**in sostanza fa reset y su tutto il gruppo*/
    private void resetGroupY(Group g, float maxy) {
        for(Actor a : g.getChildren())
            if (a instanceof ResettableY)
                ((ResettableY)a).resetY(maxy);
    }

    @Override
    public void resize(int width, int height) {
        float tmpy = getStage().getCamera().position.y;
        super.resize(width, height); //questo sposta la telecamera al centro dello schermo
        getStage().getCamera().position.y = tmpy; //rimetti la telecamera dove stava prima

        float viewportH = getStage().getCamera().viewportHeight;
        float viewportW = getStage().getCamera().viewportWidth;

        //ricalcoliamo l'offset della camera e muoviamola subito al punto in cui serve
        if(camController != null) {
            float camOffset = goingUp ? viewportH * 0.5f * 0.8f : -viewportH * 0.5f + player.getHeight() * 3f;
            if (Math.abs(camController.getStartingOffset() - camOffset) > com.pizzaroof.sinfulrush.Constants.EPS) { //facciamolo solo se l'offset è cambiato
                camController.setStartingOffset(camOffset);
                camController.instantMoveCamera();
            }
        }

        //alcuni padspawner hanno bisogno della larghezza della viewport
        if(padSpawner instanceof com.pizzaroof.sinfulrush.spawner.platform.UniformPlatformSpawner)
            ((UniformPlatformSpawner) padSpawner).setViewportWidth(getStage().getCamera().viewportWidth);

        //settiamo width della viewport per l'hole catcher
        if(holeCatcher != null)
            holeCatcher.setViewportWidth(viewportW);

        if(background != null) {
            beforeBackgroundResize(width, height);
            background.onScreenResized();
        }
    }

    /**lasciato per le sottoclassi...*/
    protected void beforeBackgroundResize(int w, int h) {}

    public boolean isGoingUp() {
        return goingUp;
    }

    /**inizializza le catene di markov.*/
    protected void initMarkovChains(com.pizzaroof.sinfulrush.spawner.enemies.MarkovEnemySpawner enemySpawner, com.pizzaroof.sinfulrush.spawner.HoleFiller holeFiller) {
    }

    /**chiamato quando il giocatore muore*/
    protected void onPlayerDied(boolean firstDeath) {
        //if(Utils.getScore(player) >= highscore)
        updatePreferences(firstDeath);
        if(armory.isUsingSword())
            armory.getSwordAttack().clearPoints();
    }

    protected void buildBackground(CameraController cameraController, AssetManager assetManager, Group bgFrontLayer) {
    }

    protected void initHoleFiller() {
        holeFiller = new HoleFiller(world2d, stage, assetManager, getSoundManager(), layerGroups, player, enemiesGroup, bonusGroup, camController, armory, getPreferences(), missionDataCollector);
    }

    /**ricomincia il gioco: va reimplementato dai vare game screen*/
    protected void restartGame() {
    }

    /**callback per quando si crea una piattaforma: utile se si vogliono aggiungere dettagli extra*/
    protected void onPlatformCreated(Platform platform) {
    }

    protected String getSoundtrackPath() {
        return com.pizzaroof.sinfulrush.Constants.HELL_SOUNDTRACK;
    }

    protected String getHighscoreName() {
        return com.pizzaroof.sinfulrush.Constants.HELL_HIGHSCORE_PREFS;
    }

    @Override
    public void hide() {
        super.hide();
        armory.cleanPools();
        dispose();
    }

    protected void updateMissions(boolean gameFinished) {
        if(missionDataCollector != null) {
            MissionManager manager = game.getMissionManager();
            for (Mission m : manager.getActiveMissions())
                if (!m.isCompleted())
                    missionDataCollector.updateMissionStatus(m, player, gameFinished);
        }
    }

    protected void updateMissionPrefs() {
        game.getMissionManager().putMissionsOnPrefs();
        game.getMissionManager().updateActiveMissions();
        game.getPreferences().flush();
    }

    public void updatePreferences(boolean reset) {
        getPreferences().putInteger(getHighscoreName(), Math.max(highscore, Utils.getScore(player)));
        getPreferences().putLong(com.pizzaroof.sinfulrush.Constants.ENEMIES_KILLED_PREFS,numKilledEnemies + player.getNumEnemiesKilled());
        getPreferences().putLong(com.pizzaroof.sinfulrush.Constants.FRIENDS_KILLED_PREFS, numKilledFriends + player.getNumFriendsKilled());
        getPreferences().putLong(com.pizzaroof.sinfulrush.Constants.FRIENDS_SAVED_PREFS, numSavedFriends + player.getNumFriendsSaved());
        getPreferences().putLong(com.pizzaroof.sinfulrush.Constants.PLATFORMS_JUMPED_PREFS, numJumpedPlatforms + player.getJumpedPlatforms());
        getPreferences().putLong(getLevelPrefix()+ com.pizzaroof.sinfulrush.Constants.BOSS_KILLED_PREFS, numKilledBoss + player.getNumBossKilled());

        if(timeStart > 0) { //le aggiorniamo solo una volta... chiama bene
            long timePassed = Math.max(0, TimeUtils.millis() - timeStart) / 1000L; //secondi
            getPreferences().putLong(com.pizzaroof.sinfulrush.Constants.TIME_PLAYED_PREFS, timePlayed + timePassed);
            if(!reset)
                timeStart = -1;
        }

        getPreferences().flush();
    }

    protected void initEnemySpawner() {
        enemySpawner = new MarkovEnemySpawner(EMPTY_INITIAL_PLATFORMS, getSoundManager(), player);
    }

    protected void initSoundtrack() {
        getSoundManager().changeSoundtrack(getSoundtrackPath(), JuicyPlayer.INIT_COOLDOWN + 0.2f);
    }

    public boolean isInPause() {
        return inPause;
    }

    public void setInPause(boolean pause) {
        this.inPause = pause;
    }

    /**chiamata quando l'arma cambia*/
    public void onWeaponChanged() {
    }

    /**chiamato all'inizio del costruttore*/
    protected void beforeBuildingScreen() {
    }

    protected void buildCameraController() {
        camController = new CameraController(player, getStage().getCamera(), 0, goingUp,
                getPreferences().getBoolean(com.pizzaroof.sinfulrush.Constants.SCREENSHAKE_PREFS, true)); //controllore della camera
    }

    protected void createMissionDataCollector() {
        missionDataCollector = new MissionDataCollector(Mission.LevelType.HELL, game);
    }

    protected String getLevelPrefix() {
        return Constants.HELL_LOCAL_PREF;
    }

    @Override
    public void dispose() {
        super.dispose();
        stage = null;
    }
}
