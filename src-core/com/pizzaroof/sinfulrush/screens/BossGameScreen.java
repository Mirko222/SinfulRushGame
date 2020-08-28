package com.pizzaroof.sinfulrush.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.JuicyPlayer;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Boss;
import com.pizzaroof.sinfulrush.spawner.enemies.MarkovEnemySpawner;
import com.pizzaroof.sinfulrush.util.PlayerPower;
import com.pizzaroof.sinfulrush.util.Utils;

/**screen di gameplay, anche in grado di spawnare boss*/
public class BossGameScreen extends GameplayScreen {

    /**quante piattaforme servono per far spawnare il boss la prima volta?*/
    public static final int FIRST_BOSS_PLATFORMS_SPAWN = 100;
    /**dopo la prima volta, dopo quante volte spawna il boss?*/
    public static final int BOSS_PLATFORMS_SPAWN = 30;
    /**si aspettano BOSS_PLATFORMS_SPAWN +- delta piattaforme*/
    protected int BOSS_SPAWN_DELTA = 0; //3

    private int platformsToFirstSpawn, platformsToReappear, platformsToRespawn;

    /**devo spawnare boss?*/
    private boolean mustSpawnBoss;

    /**il boss vero e proprio*/
    protected Boss boss;

    /**a che piattaforma si spawna il boss?*/
    private int platformNeededToBoss;

    /**boss già evocato?*/
    private boolean evocated;

    /**il boss è attivo?*/
    private boolean bossActive;

    private boolean bossVisible;

    /**quando è vero, lo spawn del boss è più accurato considerando il numero di piattaforme*/
    private boolean accurateBoss;

    private int numGeneratedPlatformsDuringBoss;

    public BossGameScreen(NGame game, boolean goingUp, String directory, int charmaps, PlayerPower powers) {
        super(game, goingUp, directory, charmaps, powers);
        mustSpawnBoss = false;
        bossVisible = false;
        boss = null;
        evocated = false;
        bossActive = false;
        accurateBoss = true;

        platformsToFirstSpawn = FIRST_BOSS_PLATFORMS_SPAWN;
        platformsToReappear = BOSS_PLATFORMS_SPAWN;
        platformsToRespawn = BOSS_PLATFORMS_SPAWN;

        platformNeededToBoss = com.pizzaroof.sinfulrush.util.Utils.randInt(platformsToFirstSpawn - BOSS_SPAWN_DELTA, platformsToFirstSpawn + BOSS_SPAWN_DELTA);

        numGeneratedPlatformsDuringBoss = 0;
    }

    @Override
    public void updateLogic(float delta) {
        //if(!mustSpawnBoss && Gdx.input.isKeyJustPressed(Input.Keys.B)) //DEBUG
        //    evocateBoss();

        if(!isInPause()) {
            if (isPossibleToSpawnBoss())
                evocateBoss();

            if (mustSpawnBoss && canSpawnBoss()) {
                //spawniamo il boss...
                mustSpawnBoss = false;
                createBoss(this);
                if (boss != null) { //se createBoss fa il suo dovere... non dovrebbe mai essere null
                    layerGroups[BOSS_LAYER].addActor(boss);
                    holeFiller.setVerticalChain(com.pizzaroof.sinfulrush.Constants.BOSS_BONUS_MARKOV_CHAIN);
                }
                evocated = false;
            }

            //prima volta che vediamo il boss
            if(!bossVisible && boss != null && boss.isInCameraView()) {
                //cambiamo musica
                player.changeSpeed(ppowers.getSpeedMultiplier()); //il player deve andare lento col boss...
                getSoundManager().changeSoundtrack(getBossIntroSoundtrack(),1.5f);
                getSoundManager().setOnSoundtrackChanged(new Runnable() {
                    @Override
                    public void run() {
                        getSoundManager().getCurrentSoundtrack().setLooping(false); //togliamo looping alla soundtrack
                        getSoundManager().getCurrentSoundtrack().setOnCompletionListener(new Music.OnCompletionListener() {
                            @Override
                            public void onCompletion(Music music) {
                                getSoundManager().playSoundtrack(getBossLoopSoundtrack());
                            }
                        });
                    }
                });

                if(canVibrate) Gdx.input.vibrate((int)(1.5f * JuicyPlayer.DEF_VIBRATE_MILLIS));
                camController.incrementTrauma(CameraController.MAX_TRAUMA);

                bossVisible = true;
            } else if(bossVisible && (boss == null || !boss.isInCameraView())) { //primo frame dopo che il boss sparisce
                getSoundManager().changeSoundtrack(getSoundtrackPath(), 1.f);
                bossVisible = false;
            }
        }

        super.updateLogic(delta);
    }

    /**posso spawnare il boss?*/
    private boolean canSpawnBoss() {
        return (player.getJumpedPlatforms() >= platformNeededToBoss || !accurateBoss) && enemiesGroup.getChildren().size == 0;
    }

    /**callback per quando il boss sta scomparendo (sta scappando) (NB: viene chiamata direttamente dal boss)*/
    public void onBossDisappearing() {
        bossActive = false;
        initMarkovChains((com.pizzaroof.sinfulrush.spawner.enemies.MarkovEnemySpawner)enemySpawner, holeFiller); //rimette le catene di markov in azione
    }

    /**boss sparito: è riuscito a scappare*/
    public void onBossDisappeared() {
        bossActive = false;
        platformNeededToBoss = getNumGeneratedPlatforms() + com.pizzaroof.sinfulrush.util.Utils.randInt(platformsToReappear - BOSS_SPAWN_DELTA, platformsToReappear + BOSS_SPAWN_DELTA); //calcoliamo quando dovrà tornare
    }

    @Override
    protected void onPlatformCreated(Platform platform) {
        super.onPlatformCreated(platform);
        if(isBossActive())
            numGeneratedPlatformsDuringBoss++;
    }

    public int getNumGeneratedPlatformsDuringBoss() {
        return numGeneratedPlatformsDuringBoss;
    }

    /**chiamata alla morte del boss*/
    public void onBossDeath() {
        player.increaseBossKilled();
        bossActive = false;
        initMarkovChains((com.pizzaroof.sinfulrush.spawner.enemies.MarkovEnemySpawner)enemySpawner, holeFiller); //rimette le catene di markov in azione
        boss = null; //morto: mettiamolo a null
        platformNeededToBoss = getNumGeneratedPlatforms() + com.pizzaroof.sinfulrush.util.Utils.randInt(platformsToRespawn - BOSS_SPAWN_DELTA, platformsToRespawn + BOSS_SPAWN_DELTA); //facciamo spawnare il prossimo boss tra un po' di piattaforme
        //platformNeededToBoss = -1; //non facciamo più spawnare il boss dopo che è morto
    }

    /**metodo che deve creare il boss in @boss*/
    protected void createBoss(BossGameScreen screen) {
        boolean firstTime = boss == null;

        if(boss == null)
            boss = createBossInstance(screen);
        Camera camera = getStage().getCamera();
        float mul = firstTime || !isGoingUp() ? 1 : -1;
        Vector2 position = new Vector2((com.pizzaroof.sinfulrush.util.Utils.randFloat() * (camera.viewportWidth - boss.getWidth()) + boss.getWidth()*0.5f) / world2d.getPixelPerMeter(),
                                        (camera.position.y - camera.viewportHeight * 0.5f * mul - boss.getDrawingHeight() * 0.5f * mul) / world2d.getPixelPerMeter());
        boss.instantSetPosition(position);
        boss.setHp(Math.min((int)(boss.getHp() + boss.getMaxHp() * .2f), boss.getMaxHp()));
        boss.setPlayer(player);
        boss.setCameraController(camController);
        boss.startFight();
    }

    protected Boss createBossInstance(BossGameScreen screen) {
        return Boss.createBoss(world2d, getSoundManager(), Vector2.Zero, getNextBossDirectory(), assetManager, stage, layerGroups[BOSS_LAYER], layerGroups[BACK_PLAYER_LAYER], screen, armory);
    }

    /**evoca il boss... poi si aspetterà*/
    private void evocateBoss() {
        if(!evocated) {
            ((com.pizzaroof.sinfulrush.spawner.enemies.MarkovEnemySpawner) enemySpawner).changeMarkovChain(null); //ferma tutti gli spawn
            ((MarkovEnemySpawner) enemySpawner).changeExtraChain(null);
            holeFiller.setVerticalChain(null);
            holeFiller.setHorizontalChain(null);
            mustSpawnBoss = true;
            evocated = false;
            bossActive = true;
        }
    }

    /**è possibile spawnare il boss?*/
    protected boolean isPossibleToSpawnBoss() {
        return getNumGeneratedPlatforms() >= platformNeededToBoss && (boss == null || boss.getParent() == null);
    }

    /**da chiamare prima di iniziare a giocare (in creazione)*/
    protected void setBossPlatforms(int platformsFirstSpawn, int platformsReapper, int platformsRespawn) {
        this.platformsToFirstSpawn = platformsFirstSpawn;
        this.platformsToReappear = platformsReapper;
        this.platformsToRespawn = platformsRespawn;
        platformNeededToBoss = Utils.randInt(platformsToFirstSpawn - BOSS_SPAWN_DELTA, platformsToFirstSpawn + BOSS_SPAWN_DELTA);
    }


    public boolean isBossActive() {
        return bossActive;
    }

    public void setAccurateBoss(boolean b) {
        accurateBoss = b;
    }

    protected String getNextBossDirectory() {
        return com.pizzaroof.sinfulrush.Constants.ELEMENTAL_DIRECTORY;
    }

    protected String getBossIntroSoundtrack() {
        return com.pizzaroof.sinfulrush.Constants.BOSS_INTRO_SOUNDTRACK;
    }

    protected String getBossLoopSoundtrack() {
        return Constants.BOSS_LOOP_SOUNDTRACK;
    }
}
