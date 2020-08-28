package com.pizzaroof.sinfulrush.screens.custom;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.CemeteryBackground;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.StaticDecoration;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.pools.DecorationPool;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Boss;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticleEmitter;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;
import com.pizzaroof.sinfulrush.language.LanguageManager;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.missions.MissionDataCollector;
import com.pizzaroof.sinfulrush.screens.BossGameScreen;
import com.pizzaroof.sinfulrush.screens.GameplayScreen;
import com.pizzaroof.sinfulrush.screens.HudGameplayScreen;
import com.pizzaroof.sinfulrush.spawner.platform.custom.CemeteryPadSpawner;
import com.pizzaroof.sinfulrush.util.PlayerPower;
import com.pizzaroof.sinfulrush.util.Utils;

import java.io.IOException;

/**screen per lo scenario del cimitero*/
public class CemeteryScreen extends HudGameplayScreen {

    private static final String MARKOV_PREFIX = "markov_chains/cemetery/";

    /**dopo quanti pixel si fa partire il movimento della camera?*/
    private static final float START_CAMERA_THRESHOLD = 600;
    private static final int SECOND_SOUNDTRACK_PLATFORM = 100;

    /**dati per decorazioni fence/rip*/
    private DecorationPool ripPool, fencePool;
    private static final float RIP_WIDTH = 96, RIP_HEIGHT = 92, FENCE_WIDTH = 81, FENCE_HEIGHT = 76;
    private static final float RIP_PROB = 0.2f, FENCE_PROB = 0.2f;
    private static final float DECORATION_PADDING = 10;

    private boolean soundtrackChanged, soundtrack2Changed;


    public CemeteryScreen(NGame game, String directory, int charmaps, PlayerPower power, int exitsWithoutInterstitial) {
        super(game, true, directory, charmaps, power, exitsWithoutInterstitial);
        ((ShaderStage)stage).setDarkBackground(true);
        camController.allowMovement(false);
        soundtrackChanged = false;
        soundtrack2Changed = false;

        setBossPlatforms(199, 50, 125);

        getSoundManager().setOnSoundtrackChanged(new Runnable() {
            @Override
            public void run() {
                getSoundManager().getCurrentSoundtrack().setLooping(false);
                getSoundManager().getCurrentSoundtrack().setOnCompletionListener(new Music.OnCompletionListener() {
                    @Override
                    public void onCompletion(Music music) {
                        soundtrackChanged = true;
                        getSoundManager().playSoundtrack(Constants.CEMETERY_SOUNDTRACK_LOOP);
                    }
                });
            }
        });

        //platformsNeededToLoad = 500; //DEBUG
        //setAccurateBoss(false); //DEBUG BOSS
        //armory.setSwordAttack(true, 2, Color.BLUE); //DEBUG BOSS
    }

    @Override
    public void initEnemySpawner() {
        super.initEnemySpawner();

        addPhasePref("weak_chibis.mkc", "HORIZ_weak_chibis.mkc", null, null, 2, 0, 1.f, 11); //0
        addPhasePref("basic_chibis_and_redfriend.mkc", "HORIZ_basic_chibis_and_redfriend.mkc", "HOLE_basic_chibis.mkc", null, 2, 0, 1.f, 11); //1
        addPhasePref("basic_chibis_and_redfriend.mkc", "HORIZ_basic_chibis_and_redfriend.mkc", "HOLE_first_healers_sword.mkc", null, 2, 0, 1.f, 5); //2 -> breve transizione per dare alta priorità alla spada prima dei volanti
        addPhasePref("basic_chibis_and_redfriend.mkc", "HORIZ_basic_chibis_and_redfriend_v2.mkc", "HOLE_healers_sword.mkc", null, 2, 0, 1.f, 14); //3 -> nemici volanti
        addPhasePref("giant_skeletons.mkc", "HORIZ_giant_skeletons.mkc", "HOLE_slowtime.mkc", "HOLE_HORIZ_slowtime.mkc", 3, 1, 1.f, 14, false, true); //4 -> scheletri grandi
        addPhasePref("only_one_shot.mkc", null, "HOLE_only_sceptre.mkc", null, 1, 0, 1.1f, 6); //5 -> transizione per primo messaggio

        addPhasePref("ghosts_and_skeletons.mkc", "HORIZ_ghosts_and_skeletons.mkc", "HOLE_ghosts_skeletons.mkc", "HOLE_HORIZ_ghosts_skeletons.mkc", 2, 1, 1.f, 15); //6 -> scheletri e fantasmi grandi
        addPhasePref("only_one_shot.mkc", null, null, null, 1, 0, 1.f, 4); //7 -> transizione perché la fase precedente è impegnativa
        addPhasePref("ghosts.mkc", "HORIZ_ghosts.mkc", "HOLE_ghosts.mkc", "HOLE_HORIZ_ghosts.mkc", 3, 1, 1.f, 15, true); //8 -> fantasmi e vari bonus
        addPhasePref("only_one_shot.mkc", null, null, null, 1, 0, 1.1f, 5); //9 -> transizione per secondo messaggio

        addPhasePref("big_zombies.mkc", "HORIZ_big_zombies.mkc", "HOLE_big_zombies.mkc", null, 2, 0, 1.3f, 15); //10 -> solo zombie grossi, senza nemici volanti
        addPhasePref("all_zombies.mkc", "HORIZ_all_zombies.mkc", "HOLE_all_zombies.mkc", "HOLE_HORIZ_all_zombies.mkc", 3, 1, 1.f, 23, true, true); //11 -> tutto insieme, fino agli zombie
        addPhasePref("big_zombies.mkc", "HORIZ_big_zombies.mkc", "HOLE_big_zombies_v2.mkc", "HOLE_HORIZ_big_zombies_v2.mkc", 2, 1, 1.2f, 15); //12 -> solo zombie grossi, ma anche volanti
        addPhasePref("only_one_shot.mkc", null, null, null, 1, 0, 1.1f, 5); //13 -> transizione per terzo messaggio

        addPhasePref("necro.mkc", "HORIZ_necro.mkc", "HOLE_necro.mkc", "HOLE_HORIZ_necro.mkc", 3, 1, 1.2f, 15); //14 -> introduciamo necromanti con solo tizi grossi
        addPhasePref("all_necro.mkc", "HORIZ_all_necro.mkc", "HOLE_all_necro.mkc", "HOLE_HORIZ_all_necro.mkc", 3, 1, 1.1f, 22, true, true); //15 -> tutto insieme
        addPhasePref("only_one_shot.mkc", "all_orange_friend.mkc", null, null, 3, 0, 1.1f, 3); //16 -> transizione per boss

        addPhase(null, null, null, null, 1, 0, 1.1f, 1); //17: boss...

        //dopo boss
        addPhasePref("only_one_shot.mkc", "only_one_shot.mkc", "HOLE_sometimes_blues.mkc", null, 3, 0, 2f, 15, true); //18: fase speciale, solo scheletrini piccoli velocissima
        addPhasePref("special1.mkc", "HORIZ_special1.mkc", "HOLE_special1.mkc", "HOLE_HORIZ_special1.mkc", 2, 2, 1.2f, 13, true, true); //19 -> necromanti + fantasmi
        addPhasePref("special2.mkc", null, "HOLE_special2.mkc", "HOLE_HORIZ_special2.mkc", 1, 2, 1.2f, 13); //20 -> solo volanti + malus rari e armi inutili fastidiose
        addPhasePref("every_skeleton.mkc", "HORIZ_every_skeleton.mkc", "HOLE_no_fly.mkc", null, 3, 0, 1.5f, 15, true, true); //21
        addPhasePref("one_shot_malus.mkc", "HORIZ_one_shot_malus.mkc", null, null, 3, 0, 1.7f, 15, true); //22
        addPhasePref("special3.mkc", "HORIZ_special3.mkc", "HOLE_special3.mkc", "HOLE_HORIZ_special3.mkc", 3, 1, 1.3f, 13, true, true); //23 -> zombie grandi e qualche malus
        addPhasePref("only_necro.mkc", "only_necro.mkc", "HOLE_only_healers.mkc", "HOLE_only_healers.mkc", 2, 2, 1.2f, 13, true, true); //24 -> solo necromanti con tantissimi healers
        addPhasePref("special_chibis.mkc", "special_chibis.mkc", "HOLE_only_healers.mkc", "HOLE_only_healers.mkc", 3, 1, 2f, 10, true); //25 -> rimettiamo i chibi iniziali

        //fasi verso l'infinito, sempre uguali ma con velocità crescente
        addPhasePref("all_inf.mkc", "HORIZ_all_inf.mkc", "HOLE_all_inf.mkc", "HOLE_HORIZ_all_inf.mkc", 3, 2, 1.4f, 17, true, true);
        addPhasePref("all_inf.mkc", "HORIZ_all_inf.mkc", "HOLE_all_inf.mkc", "HOLE_HORIZ_all_inf.mkc", 3, 2, 1.5f, 17, true, true);
        addPhasePref("all_inf.mkc", "HORIZ_all_inf.mkc", "HOLE_all_inf.mkc", "HOLE_HORIZ_all_inf.mkc", 3, 2, 1.6f, 17, true, true);
        addPhasePref("all_inf.mkc", "HORIZ_all_inf.mkc", "HOLE_all_inf.mkc", "HOLE_HORIZ_all_inf.mkc", 3, 2, 1.7f, 17, true, true);
        addPhasePref("all_inf.mkc", "HORIZ_all_inf.mkc", "HOLE_all_inf.mkc", "HOLE_HORIZ_all_inf.mkc", 3, 2, 1.8f, 17, true, true);
        addPhasePref("all_inf.mkc", "HORIZ_all_inf.mkc", "HOLE_all_inf.mkc", "HOLE_HORIZ_all_inf.mkc", 3, 2, 1.9f, 17, true, true);
        addPhasePref("all_inf.mkc", "HORIZ_all_inf.mkc", "HOLE_all_inf.mkc", "HOLE_HORIZ_all_inf.mkc", 3, 2, 2.f, 48, true, true);
        //dopo 150 piattaforme di questo tipo, introduciamo una fase infinita come la precedente, ma un po' più complicata (meno vita e più malus arancioni)
        addPhasePref("all_inf_hard.mkc", "HORIZ_all_inf_hard.mkc", "HOLE_all_inf_hard.mkc", "HOLE_HORIZ_all_inf.mkc", 3, 2, 2f, 60, true, true);

        //addPhase(null, null, null, null, 1, 0, 1.1f, 1); //17: boss...



        if(isSfxOn()) addObjToLoad(Constants.PORTAL_SFX, 1, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.FRIEND_DEATH_SFX, 1, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.EXPLOSION_ENEMY_SFX, 1, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BONUS_TAKEN_SFX, 1, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.SWORD_DAMAGE_SFX, 1, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.SWORD_SWING_SFX, 1, LoadObjectType.SOUND);
        addObjToLoad(Constants.MUMMY_DIRECTORY, 1, LoadObjectType.ENEMY);
        addObjToLoad(Constants.LICH_DIRECTORY, 1, LoadObjectType.ENEMY);
        addObjToLoad(Constants.YELLOW_FRIEND_DIRECTORY, 1, LoadObjectType.ENEMY);
        addObjToLoad(Constants.FRIEND_BALL_EFFECT, 1, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.FIRE_EXPLOSION_EFFECT, 1, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.SPARKLE_ATLAS, 1, LoadObjectType.TEXTURE_ATLAS);
        addObjToLoad(Constants.SWORD_BONUS_DIRECTORY, 1, LoadObjectType.BONUS);


        addObjToLoad(Constants.HEAL_FIREBALL_EFFECT, 2, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.HEAL_EFFECT, 2, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.GHOST_HEALER_DIRECTORY,2, LoadObjectType.ENEMY);


        if(isSfxOn()) addObjToLoad(Constants.HEALTH_POTION_SFX, 3, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.WIND_SFX, 3, LoadObjectType.SOUND);
        addObjToLoad(Constants.FIREBALL_EFFECT, 3, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.FIRE_EXPLOSION_EFFECT, 3, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.VAMPIRE_DIRECTORY, 3, LoadObjectType.ENEMY);
        addObjToLoad(Constants.BONUS_ICON_TYPE_1_DIR, 3, LoadObjectType.BONUS);
        addObjToLoad(Constants.WIND_EFFECT, 3, LoadObjectType.SHEET_SPRITER_EFFECT);


        if(isSfxOn()) addObjToLoad(Constants.SLOWTIME_SFX, 4, LoadObjectType.SOUND);
        addObjToLoad(Constants.GIANT_SKELETON1, 4, LoadObjectType.ENEMY);
        addObjToLoad(Constants.GIANT_SKELETON2, 4, LoadObjectType.ENEMY);
        addObjToLoad(Constants.MINI_SKELETON1_DIRECTORY, 4, LoadObjectType.ENEMY);
        addObjToLoad(Constants.MEDIUM_HEAL_EFFECT, 4, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.LARGE_HEAL_EFFECT, 4, LoadObjectType.PARTICLE_EFFECT);
        addPhysicParticleEmitterToLoad(Constants.PHYSIC_PARTICLE_MEDIUM_BONES, 4, Constants.DEF_MIN_MEDBONES_RADIUS, Constants.DEF_MAX_MEDBONES_RADIUS, true, true, EFFECTS_LAYER);


        if(isSfxOn()) addObjToLoad(Constants.SCEPTRE_SPAWN_SFX, 5, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.SCEPTRE_EXPLOSION_SFX, 5, LoadObjectType.SOUND);
        addObjToLoad(Constants.SCEPTRE_BALL_EFFECT, 5, LoadObjectType.SHEET_SPRITER_EFFECT);


        if(isSfxOn()) addObjToLoad(Constants.THUNDER_SFX, 6, LoadObjectType.SOUND);
        addObjToLoad(Constants.GHOST1_DIRECTORY, 6, LoadObjectType.ENEMY);
        addObjToLoad(Constants.LIGHTNING_EFFECT, 6, LoadObjectType.SHEET_SPRITER_EFFECT);


        if(isMusicOn()) addObjToLoad(Constants.CEMETERY_SOUNDTRACK_INTRO2, 9, LoadObjectType.MUSIC);
        if(isMusicOn()) addObjToLoad(Constants.CEMETERY_SOUNDTRACK_LOOP2, 9, LoadObjectType.MUSIC);


        if(isSfxOn()) addObjToLoad(Constants.RAGE_SFX, 10, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.ICE_ACTIVATION_SFX, 10, LoadObjectType.SOUND);
        addPhysicParticleEmitterToLoad(Constants.PHYSIC_PARTICLE_ZOMBIEP, 10, Constants.DEF_MIN_ZOMBIEP_RADIUS, Constants.DEF_MAX_ZOMBIEP_RADIUS, true, true, EFFECTS_LAYER);
        addPhysicParticleEmitterToLoad(Constants.PHYSIC_PARTICLE_ZOMBIEP2, 10, Constants.DEF_MIN_ZOMBIEP_RADIUS, Constants.DEF_MAX_ZOMBIEP_RADIUS, true, true, EFFECTS_LAYER);
        addPhysicParticleEmitterToLoad(Constants.PHYSIC_PARTICLE_ZOMBIEP3, 10, Constants.DEF_MIN_ZOMBIEP_RADIUS, Constants.DEF_MAX_ZOMBIEP_RADIUS, true, true, EFFECTS_LAYER);
        addObjToLoad(Constants.ICE_EFFECT, 10, LoadObjectType.SHEET_SPRITER_EFFECT);
        addObjToLoad(Constants.GIANT_ZOMBIE1, 10, LoadObjectType.ENEMY);
        addObjToLoad(Constants.GIANT_ZOMBIE2, 10, LoadObjectType.ENEMY);
        addObjToLoad(Constants.GIANT_ZOMBIE3, 10, LoadObjectType.ENEMY);


        addObjToLoad(Constants.GREEN_NECROMANCER,14, LoadObjectType.ENEMY);
        addObjToLoad(Constants.RED_NECROMANCER,14, LoadObjectType.ENEMY);
        addObjToLoad(Constants.BLUE_NECROMANCER,14, LoadObjectType.ENEMY);
        addObjToLoad(Constants.BLUE_SKELETON,14, LoadObjectType.ENEMY);
        addObjToLoad(Constants.GREEN_SKELETON,14, LoadObjectType.ENEMY);
        addObjToLoad(Constants.RED_SKELETON,14, LoadObjectType.ENEMY);


        if(isMusicOn()) addObjToLoad(Constants.BOSS_INTRO_SOUNDTRACK2, 16, LoadObjectType.MUSIC);
        if(isMusicOn()) addObjToLoad(Constants.BOSS_LOOP_SOUNDTRACK2, 16, LoadObjectType.MUSIC);
        if(isSfxOn()) addObjToLoad(Constants.BIG_EXPLOSION_SFX, 16, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BIG_EXPLOSION2_SFX, 16, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BOSS_DEATH_SFX, 16, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BOSS_HURT_SFX, 16, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BOSS_ROAR_SFX, 16, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BOSS_ATTACK_SFX, 16, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.DEATH_HEAD_CUT_SFX, 16, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BOSS_BALL_EXPLOSION_SFX, 16, LoadObjectType.SOUND);
        addObjToLoad(Constants.DEATH_REAPER, 16, LoadObjectType.ENEMY);
        addPhysicParticleEmitterToLoad(Constants.PHYSIC_PARTICLE_BIG_BONES, 16, Constants.DEF_MIN_BIGBONES_RADIUS, Constants.DEF_MAX_BIGBONES_RADIUS, true, false, BOSS_LAYER);
        addObjToLoad(Constants.BOSS_FIREBALL, 16, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.BOSS_FIREBALL_EXPLOSION, 16, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.TREASURE_BALL, 16, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.TREASURE_EXPLOSION, 16, LoadObjectType.PARTICLE_EFFECT);
    }

    protected boolean mustRemoveHoleFilters(int phase) {
        return phase == 18 || phase == 20; //18, 20
    }

    protected boolean mustSpawnOnlyBigPlatforms(int phase) {
        return phase == 6 || phase == 10 || phase == 12 || phase == 14 || phase == 19 || phase == 24; //6, 10, 12, 14, 19, 24
    }

    @Override
    public void initParticlePool() {
        super.initParticlePool();
        PhysicParticleEmitter emitter = assetManager.get(Constants.PHYSIC_PARTICLE_BONES, PhysicParticleEmitter.class);
        emitter.setWorld(world2d, true, true);
        emitter.setParticleGroup(layerGroups[EFFECTS_LAYER]);
        /*PhysicParticleEmitter emitter2 = assetManager.get(Constants.PHYSIC_PARTICLE_ZOMBIEP, PhysicParticleEmitter.class);
        emitter2.setWorld(world2d, true, true);
        emitter2.setParticleGroup(layerGroups[EFFECTS_LAYER]);*/
    }

    @Override
    protected void createMessageMechanism(Skin skin) {
        super.createMessageMechanism(skin);

        addScheduledMessage(LanguageManager.Text.CEMETERY_MY_REIGN, 3.f, 56);
        addScheduledMessage(LanguageManager.Text.CEMETERY_GO_BACK, 3.f, 96);
        addScheduledMessage(LanguageManager.Text.CEMETERY_LAST_WARNING, 3.f, 154);
        addScheduledMessage(LanguageManager.Text.CEMETERY_ON_SPAWN, 3.f, 196);
    }

    protected Boss createBossInstance(BossGameScreen screen) {
        Boss boss = super.createBossInstance(screen);
        boss.setExplosionType(1);
        return boss;
    }

    @Override
    public void beforeBackgroundResize(int width, int height) {
        if(!background.backgroundReady()) {
            float tmp = camController.getStartingOffset();
            camController.setStartingOffset(camController.getViewportHeight() * 0.5f);
            camController.instantMoveCamera();
            camController.setStartingOffset(tmp);
        }
    }

    @Override
    public void onEnteringPhase(int num) {
        if(mustSpawnOnlyBigPlatforms(num-1)) { //fase dopo quella in cui spawnano tante piattaforme
            ((CemeteryPadSpawner)padSpawner).addPadAvailable(Constants.CEMETERY_PAD_1, Constants.CEMETERY_PAD_1_COVER); //inseriamo tutte le piattaforme
            ((CemeteryPadSpawner)padSpawner).addPadAvailable(Constants.CEMETERY_PAD_3, Constants.CEMETERY_PAD_3_COVER);
            ((CemeteryPadSpawner)padSpawner).addPadAvailable(Constants.CEMETERY_PAD_4, Constants.CEMETERY_PAD_4_COVER);
        }

        if(mustSpawnOnlyBigPlatforms(num)) {
            padSpawner.removePadAvailable(Constants.CEMETERY_PAD_1);
            padSpawner.removePadAvailable(Constants.CEMETERY_PAD_3);
            padSpawner.removePadAvailable(Constants.CEMETERY_PAD_4);
        }

        holeFiller.setApplyFilters(!mustRemoveHoleFilters(num));
    }

    @Override
    public void updateLogic(float delta) {
        super.updateLogic(delta);

        if(!camController.movementAllowed() && player.getY() - camController.getRestoredCameraY() + camController.getViewportHeight() * 0.5f >= START_CAMERA_THRESHOLD)
            camController.allowMovement(true);
        if(!soundtrack2Changed && player.getJumpedPlatforms() >= SECOND_SOUNDTRACK_PLATFORM) {
            getSoundManager().changeSoundtrack(Constants.CEMETERY_SOUNDTRACK_INTRO2, 1.5f);
            getSoundManager().setOnSoundtrackChanged(new Runnable() {
                @Override
                public void run() {
                    getSoundManager().getCurrentSoundtrack().setLooping(false);
                    getSoundManager().getCurrentSoundtrack().setOnCompletionListener(new Music.OnCompletionListener() {
                        @Override
                        public void onCompletion(Music music) {
                            getSoundManager().playSoundtrack(Constants.CEMETERY_SOUNDTRACK_LOOP2);
                        }
                    });
                }
            });
            soundtrack2Changed = true;
        }
    }

    @Override
    public void onBossDisappeared() {
        super.onBossDisappeared();
        setAccurateBoss(false); //solo primo boss accurato
    }

    @Override
    public void onBossDeath() {
        super.onBossDeath();
        setAccurateBoss(false); //solo primo boss accurato
        if(player.getNumBossKilled() == 1) //prima volta che uccide il boss in questo game
            addScheduledMessage(LanguageManager.Text.CEMETERY_ON_DEATH, 3.f, player.getJumpedPlatforms()); //il boss viene dopo tutti gli altri testi...
    }

    @Override
    protected void buildPlatformSpawner() {
        padSpawner = new CemeteryPadSpawner(game.getAssetManager(), Constants.VIRTUAL_WIDTH, world2d);
        ((CemeteryPadSpawner)padSpawner).addPadAvailable(Constants.CEMETERY_PAD_1, Constants.CEMETERY_PAD_1_COVER); //inseriamo tutte le piattaforme
        ((CemeteryPadSpawner)padSpawner).addPadAvailable(Constants.CEMETERY_PAD_2, Constants.CEMETERY_PAD_2_COVER);
        ((CemeteryPadSpawner)padSpawner).addPadAvailable(Constants.CEMETERY_PAD_3, Constants.CEMETERY_PAD_3_COVER);
        ((CemeteryPadSpawner)padSpawner).addPadAvailable(Constants.CEMETERY_PAD_4, Constants.CEMETERY_PAD_4_COVER);
        ((CemeteryPadSpawner)padSpawner).setFrontLayer(layerGroups[GameplayScreen.COVER_PLAYER_LAYER]);
    }

    @Override
    public void restartGame() {
        //setDestinationScreen(new CemeteryScreen(getGame(), 0)); //da problemi perché questo gameplay può continuare a generare piattaforme mentre quello nuovo è già stato creato
        getGame().setScreen(new CemeteryScreen(getGame(), plStr, plCharmaps, ppowers, 0));
    }

    @Override
    public String getNextBossDirectory() {
        return Constants.DEATH_REAPER;
    }

    @Override
    protected void buildBackground(CameraController camController, AssetManager assetManager, Group bgFrontLayer) {
        background = new CemeteryBackground(camController, assetManager, bgFrontLayer, layerGroups[BACKGROUND_LAYER]);
    }

    @Override
    protected String getBossIntroSoundtrack() {
        return Constants.BOSS_INTRO_SOUNDTRACK2;
    }

    @Override
    protected String getBossLoopSoundtrack() {
        return Constants.BOSS_LOOP_SOUNDTRACK2;
    }

    @Override
    public void buildPlayer(Platform first, String directory, int charmaps, PlayerPower power) throws IOException {
        super.buildPlayer(first, directory, charmaps, power);
        player.setHorDirection(SpriteActor.HorDirection.LEFT);
        player.recomputeSpriterFlip();
        player.getSpriterPlayer().update();
    }

    @Override
    protected void onPlatformCreated(Platform platform) {
        super.onPlatformCreated(platform);

        if(ripPool == null) ripPool = new DecorationPool(game.getAssetManager().get(Constants.CEMETERY_DECORATIONS), "rip", false);
        if(fencePool == null) fencePool = new DecorationPool(game.getAssetManager().get(Constants.CEMETERY_DECORATIONS), "fence", false);

        float rnd = Utils.randFloat();
        StaticDecoration decoration;

        if(rnd <= RIP_PROB) { //mettiamo un rip sulla piattaforma
            decoration = ripPool.obtain();
            decoration.setWidth(RIP_WIDTH);
            decoration.setHeight(RIP_HEIGHT);
        } else if(rnd - RIP_PROB <= FENCE_PROB) { //mettiamo un fence sulla piattaforma
            decoration = fencePool.obtain();
            decoration.setWidth(FENCE_WIDTH);
            decoration.setHeight(FENCE_HEIGHT);
            decoration.flip(Utils.randBool());
        } else
            return;

        decoration.setX(Utils.randFloat(platform.getX() + DECORATION_PADDING, platform.getX() + platform.getWidth() - decoration.getWidth() - DECORATION_PADDING));
        decoration.setY(platform.getY() + platform.getHeight() * 0.82f);
        layerGroups[PLATFORM_LAYER].addActor(decoration);
    }

    @Override
    protected String getLevelPrefix() {
        return Constants.CEMETERY_LOCAL_PREF;
    }

    @Override
    protected void createMissionDataCollector() {
        missionDataCollector = new MissionDataCollector(Mission.LevelType.CEMETERY, game);
    }

    @Override
    public String getSoundtrackPath() {
        if(soundtrack2Changed)
            return Constants.CEMETERY_SOUNDTRACK_LOOP2;
        if(soundtrackChanged)
            return Constants.CEMETERY_SOUNDTRACK_LOOP;
        return Constants.CEMETERY_SOUNDTRACK_INTRO;
    }

    @Override
    public String getHighscoreName() {
        return Constants.CEMETERY_HIGHSCORE_PREFS;
    }

    /**come addPhase, ma mette in automatico il prefisso per le catene di markov*/
    private void addPhasePref(String enemyVer, String enemyHor, String holeVert, String holeHor, int mepp, int mbph, float playerSpeed, int duration) {
        addPhasePref(enemyVer, enemyHor, holeVert, holeHor, mepp, mbph, playerSpeed, duration, false);
    }

    private void addPhasePref(String enemyVer, String enemyHor, String holeVert, String holeHor, int mepp, int mbph, float playerSpeed, int duration, boolean threeChibi) {
        addPhasePref(enemyVer, enemyHor, holeVert, holeHor, mepp, mbph, playerSpeed, duration, threeChibi, false);
    }

    private void addPhasePref(String enemyVer, String enemyHor, String holeVert, String holeHor, int mepp, int mbph, float playerSpeed, int duration, boolean threeChibi, boolean bigGuys) {
        addPhase(enemyVer != null ? MARKOV_PREFIX + enemyVer : null,
                enemyHor != null ? MARKOV_PREFIX + enemyHor : null,
                holeVert != null ? MARKOV_PREFIX + holeVert : null,
                holeHor != null ? MARKOV_PREFIX + holeHor : null, mepp, mbph, playerSpeed, duration, threeChibi, bigGuys);
    }

}
