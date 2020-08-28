package com.pizzaroof.sinfulrush.screens;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.brashmonkey.spriter.gdx.SpriterData;
import com.brashmonkey.spriter.gdx.SpriterDataLoader;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticleEmitter;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticleEmitterLoader;
import com.pizzaroof.sinfulrush.spawner.HoleFiller;
import com.pizzaroof.sinfulrush.spawner.enemies.MarkovEnemySpawner;
import com.pizzaroof.sinfulrush.util.PlayerPower;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.util.ArrayList;

/**gameplay screen in cui ci sono varie fasi. Da una fase all'altra cambiano i vari spawn, sia dei bonus che dei nemici*/
public class PhaseGameplayScreen extends BossGameScreen {

    /**un delta per aspettare un numero casuale di piattaforme per passare alla prossima fase*/
    protected final static int DELTA_NUM_PLATFORMS = 0; //2

    protected int actualPhase;

    protected int nextDelta; //prossimo delta casuale per le fasi

    /**tutte le catene di markov per le varie fasi*/
    protected ArrayList<ChainPhase> chains;

    /**oggetti da caricare. NB: si assume che siano ordinati per fase*/
    protected ArrayList<ObjectToLoad> objsToLoad;

    /**il prossimo oggetto di objsToLoad da caricare è loadingIndex*/
    protected int loadingIndex;

    /**quante piattaforme prima iniziare a caricare gli assets?*/
    protected int platformsNeededToLoad;

    protected AssetManager assetManager;
    private SpriterDataLoader.SpriterDataParameter spriterDataParameter;

    public PhaseGameplayScreen(NGame game, boolean goingUp, String directory, int charmaps, PlayerPower powers) {
        super(game, goingUp, directory, charmaps, powers);
        platformsNeededToLoad = 50;
    }

    @Override
    public void beforeBuildingScreen() {
        this.assetManager = game.getAssetManager();
        spriterDataParameter = new SpriterDataLoader.SpriterDataParameter();
        objsToLoad = new ArrayList<>();
        loadingIndex = 0;
        nextDelta = com.pizzaroof.sinfulrush.util.Utils.randInt(-DELTA_NUM_PLATFORMS, DELTA_NUM_PLATFORMS);
        actualPhase = 0;
    }

    /**cambia dinamicamente le catene, man mano che saliamo*/
    protected void changeChainsIfNeeded() {
        if(actualPhase >= chains.size()-1) return;

        if(getNumGeneratedPlatforms() - getNumGeneratedPlatformsDuringBoss() >= chains.get(actualPhase+1).startingPlatform + nextDelta &&          //siamo arrivati a una piattaforma abbastanza avanti
                (loadingIndex >= objsToLoad.size() || objsToLoad.get(loadingIndex).phase > actualPhase+1) && //abbiamo caricato tutto per questa fase
                !isBossActive()) { //e non stiamo interrompendo il boss
            actualPhase++;
            nextDelta = com.pizzaroof.sinfulrush.util.Utils.randInt(-DELTA_NUM_PLATFORMS, DELTA_NUM_PLATFORMS);
            initMarkovChains((com.pizzaroof.sinfulrush.spawner.enemies.MarkovEnemySpawner)enemySpawner, holeFiller);
        }
    }

    /**continua il caricamento degli oggetti per le varie fasi*/
    protected void keepLoading() {
        if(loadingIndex < objsToLoad.size()) {

            int phase = objsToLoad.get(loadingIndex).phase;
            if(phase >= chains.size() || chains.get(phase).startingPlatform - getNumGeneratedPlatforms() <= platformsNeededToLoad) {

                if (isLoaded(objsToLoad.get(loadingIndex))) { //ultimo oggetto già caricato... passiamo a quello dopo
                    loadingIndex++;

                    //Gdx.app.log("debug", loadingIndex + " caricati su " + objsToLoad.size());

                    if (loadingIndex >= objsToLoad.size()) return;
                    loadObject(objsToLoad.get(loadingIndex));
                }

                assetManager.update();
            }
        }
    }

    @Override
    public void initEnemySpawner() {
        super.initEnemySpawner();
        chains = new ArrayList<>();
    }

    @Override
    protected void initMarkovChains(com.pizzaroof.sinfulrush.spawner.enemies.MarkovEnemySpawner enemySpawner, HoleFiller holeFiller) {
        if(chains == null) return;

        int chain = Math.min(actualPhase, chains.size()-1); //quale catena usare?
        setPhase(chain);
    }

    /**setta le catene di markov per una certa fase*/
    public void setPhase(int num) {
        if(num >= chains.size() || num < 0) return;
        ChainPhase phase = chains.get(num);

        ((com.pizzaroof.sinfulrush.spawner.enemies.MarkovEnemySpawner)enemySpawner).changeMarkovChain(phase.enemyVer);
        ((MarkovEnemySpawner)enemySpawner).changeExtraChain(phase.enemyHor);
        enemySpawner.setMaxEnemiesPerPlatform(phase.maxEnemyPerPlatform);

        holeFiller.setVerticalChain(phase.bonusVer);
        holeFiller.setHorizontalChain(phase.bonusHor);
        holeFiller.setMaxRecursionLevel(phase.maxBonusPerHole);
        player.changeSpeed(phase.playerSpeed);
        enemySpawner.setThreeChibisOnMorePlatformsEnabled(phase.threeChibisEnabled);
        enemySpawner.setExpandBigGuysEnabled(phase.expandBigGuysEnabled);

        onEnteringPhase(num);
    }

    /**chiamata quando si sta entrando nella fase @num*/
    public void onEnteringPhase(int num) {
    }

    @Override
    public void updateLogic(float delta) {
        changeChainsIfNeeded();
        keepLoading();
        super.updateLogic(delta);
    }

    public int getActualPhase() {
        return actualPhase;
    }

    /**aggiunge una fase: bisogna fornire tutte le catene di markov (eventualmente metti null)
     * @param duration durata della fase in piattaforme?
     * @param mepp massimo numero di nemici su una piattaforma
     * @param mbph massimo numero di bonus in un buco
     * @param threeChibisEnabled abilitata opzione 3chibis nello spawner?*/
    public void addPhase(String enemyVer, String enemyHor, String bonusVer, String bonusHor, int mepp, int mbph, float playerSpeed, int duration, boolean threeChibisEnabled, boolean expandBigGuys) {
        ChainPhase phase = new ChainPhase();
        phase.enemyVer = enemyVer;
        phase.enemyHor = enemyHor;
        phase.bonusVer = bonusVer;
        phase.bonusHor = bonusHor;
        phase.maxEnemyPerPlatform = mepp;
        phase.maxBonusPerHole = mbph;
        phase.duration = duration;
        phase.threeChibisEnabled = threeChibisEnabled;
        phase.expandBigGuysEnabled = expandBigGuys;
        int size = chains.size();
        phase.startingPlatform = size == 0 ? 0 : chains.get(size-1).startingPlatform + chains.get(size-1).duration;
        phase.playerSpeed = playerSpeed * ppowers.getSpeedMultiplier();
        chains.add(phase);
    }

    public void addPhase(String enemyVer, String enemyHor, String bonusVer, String bonusHor, int mepp, int mbph, float playerSpeed, int duration, boolean threeChibis) {
        addPhase(enemyVer, enemyHor, bonusVer, bonusHor, mepp, mbph, playerSpeed, duration, threeChibis, false);
    }

    public void addPhase(String enemyVer, String enemyHor, String bonusVer, String bonusHor, int mepp, int mbph, float playerSpeed, int duration) {
        addPhase(enemyVer, enemyHor, bonusVer, bonusHor, mepp, mbph, playerSpeed, duration, false, false);
    }

    /**aggiunge un oggetto da caricare: NB: non vengono fatti controlli sul fatto che phase deve essere crescente*/
    public void addObjToLoad(String obj, int phase, LoadObjectType type) {
        ObjectToLoad o = new ObjectToLoad();
        o.path = obj;
        o.phase = phase;
        o.type = type;
        objsToLoad.add(o);

        if(objsToLoad.size() == 1) //primo elemento da caricare... lo metto in coda nell'assetmanager
            loadObject(o);
    }

    /**carica un particle emitter to load*/
    public void addPhysicParticleEmitterToLoad(String path, int phase, int minRadius, int maxRadius, boolean selfCollision, boolean envCollision, int layerNum) {
        ObjectToLoad o = new ObjectToLoad();
        o.path = path;
        o.phase = phase;
        o.type = LoadObjectType.PHYSIC_PARTICLE_EMITTER;
        o.prmsI = new int[]{minRadius, maxRadius, layerNum};
        o.prmsB = new boolean[]{selfCollision, envCollision};
        objsToLoad.add(o);

        if(objsToLoad.size() == 1) //primo elemento da caricare... lo metto in coda nell'assetmanager
            loadObject(o);
    }

    /**rappresenta le catene di markov da usare in una fase*/
    public class ChainPhase {
        public String enemyVer, enemyHor;
        public String bonusVer, bonusHor;
        public int maxEnemyPerPlatform, maxBonusPerHole;
        public int startingPlatform, duration;
        public float playerSpeed;
        public boolean threeChibisEnabled, expandBigGuysEnabled;
    }

    /**tipo di oggetto da caricare*/
    public enum LoadObjectType {
        ENEMY, //è un enemy che va caricato
        SHEET_SPRITER_EFFECT, //effetto in spriter
        TEXTURE_ATLAS, //semplicemente texture atlas
        BONUS,
        PHYSIC_PARTICLE_EMITTER,
        PARTICLE_EFFECT,
        SOUND,
        MUSIC
    }

    /**oggetto da caricare*/
    public class ObjectToLoad {
        public String path; //path dell'oggetto
        public int phase; //qual è la prima fase in cui serve?
        public LoadObjectType type; //tipo dell'oggetto
        public int [] prmsI; //parametri interi
        public boolean [] prmsB; //parametri booleani
    }

    protected void loadObject(ObjectToLoad obj) {
        switch (obj.type) {
            case ENEMY:
                assetManager.load(com.pizzaroof.sinfulrush.util.Utils.enemyScmlPath(obj.path), SpriterData.class, spriterDataParameter);
                break;
            case SHEET_SPRITER_EFFECT:
                assetManager.load(com.pizzaroof.sinfulrush.util.Utils.sheetEffectScmlPath(obj.path), SpriterData.class, spriterDataParameter);
                break;
            case TEXTURE_ATLAS:
                assetManager.load(obj.path, TextureAtlas.class);
                break;
            case BONUS:
                assetManager.load(com.pizzaroof.sinfulrush.util.Utils.bonusScmlPath(obj.path), SpriterData.class, spriterDataParameter);
                break;

            case PHYSIC_PARTICLE_EMITTER:
                PhysicParticleEmitterLoader.PhysicParticleEmitterParameter parameter = new PhysicParticleEmitterLoader.PhysicParticleEmitterParameter();
                parameter.minParticleRadius = obj.prmsI[0];
                parameter.maxParticleRadius = obj.prmsI[1];
                parameter.loadedCallback = new AssetLoaderParameters.LoadedCallback() {
                    @Override
                    public void finishedLoading(AssetManager assetManager, String fileName, Class type) {
                        PhysicParticleEmitter emitter = assetManager.get(fileName);
                        emitter.setWorld(world2d, obj.prmsB[0], obj.prmsB[1]);
                        emitter.setParticleGroup(layerGroups[obj.prmsI[2]]);
                    }
                };
                assetManager.load(obj.path, PhysicParticleEmitter.class, parameter);
                break;

            case PARTICLE_EFFECT:
                ParticleEffectLoader.ParticleEffectParameter param = new ParticleEffectLoader.ParticleEffectParameter();
                param.atlasFile = Constants.PHYSIC_PARTICLE_ATLAS; //usiamo atlas invece di singoli file
                param.loadedCallback = new AssetLoaderParameters.LoadedCallback() {
                    @Override
                    public void finishedLoading(AssetManager assetManager, String fileName, Class type) {
                        Pools.addEffectPool(fileName, assetManager.get(fileName));
                    }
                };
                assetManager.load(obj.path, ParticleEffect.class, param);
                break;

            case SOUND:
                assetManager.load(obj.path, Sound.class);
                break;

            case MUSIC:
                assetManager.load(obj.path, Music.class);
                break;
        }
    }

    protected boolean isLoaded(ObjectToLoad obj) {
        return isLoaded(obj.path, obj.type);
    }

    protected boolean isLoaded(String path, LoadObjectType type) {
        switch (type) {
            case ENEMY: return assetManager.isLoaded(com.pizzaroof.sinfulrush.util.Utils.enemyScmlPath(path));
            case SHEET_SPRITER_EFFECT: return assetManager.isLoaded(com.pizzaroof.sinfulrush.util.Utils.sheetEffectScmlPath(path));
            case BONUS: return assetManager.isLoaded(Utils.bonusScmlPath(path));
            default: return assetManager.isLoaded(path);
        }
    }

    @Override
    protected boolean isPossibleToSpawnBoss() {
        return isLoaded(getNextBossDirectory(), LoadObjectType.ENEMY) && super.isPossibleToSpawnBoss();
    }
}
