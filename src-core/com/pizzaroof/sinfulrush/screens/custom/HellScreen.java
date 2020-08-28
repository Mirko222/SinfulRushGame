package com.pizzaroof.sinfulrush.screens.custom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.HellBackground;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.StaticDecoration;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.pools.DecorationPool;
import com.pizzaroof.sinfulrush.language.LanguageManager;
import com.pizzaroof.sinfulrush.screens.HudGameplayScreen;
import com.pizzaroof.sinfulrush.spawner.platform.custom.HellPadSpawner;
import com.pizzaroof.sinfulrush.util.PlayerPower;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin;

import java.io.IOException;

/**screen per lo scenario dell'inferno*/
public class HellScreen extends HudGameplayScreen {

    private static final String MARKOV_PREFIX = "markov_chains/hell/";

    private static final int STARTING_OFFSET_CAM = 660;
    private static final float START_CAMERA_THRESHOLD = 800;
    private static final int SUGGESTION_SCORE = 95;

    /**dati per decorazioni goat/sign*/
    private DecorationPool goatPool, signPool;
    private static final float GOAT_WIDTH = 116, GOAT_HEIGHT = 62, SIGN_WIDTH = 68, SIGN_HEIGHT = 73;
    private static final float GOAT_PROB = 0.2f, SIGN_PROB = 0.2f;
    private static final float DECORATION_PADDING = 10;

    private static final int SECOND_SOUNDTRACK_PLATFORM = 151; //151;

    private Dialog cemeteryUnlockedDialog;

    private boolean soundtrackChanged;

    //private Container<Label> suggestion;

    public HellScreen(NGame game, String directory, int charmaps, PlayerPower powers, int exitsWithoutInterstitial) {
        super(game, false, directory, charmaps, powers, exitsWithoutInterstitial);
        camController.allowMovement(false);

        soundtrackChanged = false;

        setBossPlatforms(233, 50, 125);

        //setAccurateBoss(false); //DEBUG BOSS
        //armory.setSwordAttack(true, 2, Color.BLUE); //DEBUG BOSS
        //platformsNeededToLoad = 500; //DEBUG
    }

    private boolean mustSpawnOnlyBigPlatforms(int phase) {
        return phase == 27 || phase == 30; //27
    }

    private boolean mustRemoveHoleFilters(int phase) {
        return phase == 29;
    }

    @Override
    public void updateLogic(float delta) {
        super.updateLogic(delta);

        if(!camController.movementAllowed() && camController.getRestoredCameraY() + camController.getViewportHeight() * 0.5f - player.getY() >= START_CAMERA_THRESHOLD)
            camController.allowMovement(true);
        if(!soundtrackChanged && player.getJumpedPlatforms() >= SECOND_SOUNDTRACK_PLATFORM) {
            getSoundManager().changeSoundtrack(getSoundtrackPath(), 1.5f);
            soundtrackChanged = true;
        }

        /*if(Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            suggestion.getActor().setText(getLanguageManager().getText(Utils.getHellSuggestion(player)));
        }*/
    }

    @Override
    public void restartGame() {
        //setDestinationScreen(new HellScreen(getGame(), 0));
        getGame().setScreen(new HellScreen(getGame(), plStr, plCharmaps, ppowers, 0));
    }

    @Override
    protected void buildBackground(CameraController camController, AssetManager assetManager, Group bgFrontLayer) {
        background = new HellBackground(camController, assetManager, bgFrontLayer, layerGroups[BACKGROUND_LAYER]);
    }

    @Override
    protected void buildPlatformSpawner() {
        padSpawner = new HellPadSpawner(game.getAssetManager(), Constants.VIRTUAL_WIDTH, world2d, PATTERN_PROBABILITY);
        padSpawner.addPadAvailable(Constants.HELL_PAD_1);
        padSpawner.addPadAvailable(Constants.HELL_PAD_2);
        padSpawner.addPadAvailable(Constants.HELL_PAD_3);
        padSpawner.addPadAvailable(Constants.HELL_PAD_4);
    }

    @Override
    public void beforeBackgroundResize(int width, int height) {
        if(!background.backgroundReady()) {
            float tmp = camController.getStartingOffset();
            camController.setStartingOffset(-camController.getViewportHeight() * 0.5f + STARTING_OFFSET_CAM);
            camController.instantMoveCamera();
            camController.setStartingOffset(tmp);
        }
    }

    @Override
    public void buildPlayer(Platform first, String directory, int charmaps, PlayerPower powers) throws IOException {
        super.buildPlayer(first, directory, charmaps, powers);
        if(first.getX() * 2 > getStage().getCamera().viewportWidth)
            player.setHorDirection(SpriteActor.HorDirection.LEFT);
        else
            player.setHorDirection(SpriteActor.HorDirection.RIGHT);
        player.recomputeSpriterFlip();
        player.getSpriterPlayer().update();
    }

    @Override
    protected void onPlatformCreated(Platform platform) {
        super.onPlatformCreated(platform);

        if(goatPool == null) goatPool = new DecorationPool(game.getAssetManager().get(Constants.HELL_DECORATIONS), "capra", false);
        if(signPool == null) signPool = new DecorationPool(game.getAssetManager().get(Constants.HELL_DECORATIONS), "cartello", false);

        float rnd = Utils.randFloat();
        StaticDecoration decoration;

        if(rnd <= GOAT_PROB) { //mettiamo un rip sulla piattaforma
            decoration = goatPool.obtain();
            decoration.setWidth(GOAT_WIDTH);
            decoration.setHeight(GOAT_HEIGHT);
            decoration.flip(Utils.randBool());
        } else if(rnd - GOAT_PROB <= SIGN_PROB) { //mettiamo un fence sulla piattaforma
            decoration = signPool.obtain();
            decoration.setWidth(SIGN_WIDTH);
            decoration.setHeight(SIGN_HEIGHT);
            decoration.flip(Utils.randBool());
        } else
            return;

        decoration.setX(Utils.randFloat(platform.getX() + DECORATION_PADDING, platform.getX() + platform.getWidth() - decoration.getWidth() - DECORATION_PADDING));
        decoration.setY(platform.getY() + platform.getHeight() * 0.82f);
        layerGroups[PLATFORM_LAYER].addActor(decoration);
    }

    @Override
    public void initEnemySpawner() {
        super.initEnemySpawner();

        addPhasePref("black_demons.mkc", null, null, null, 1, 0, 1, 12); //0
        addPhasePref("all_chibi_demons_and_redfriend_V3.mkc", "HORIZ_all_chibi_demons_and_redfriend_V3.mkc", null, null, 2, 0, 1, 14); //1
        addPhasePref("all_chibi_demons_and_redfriend_V3.mkc", "HORIZ_all_chibi_demons_and_redfriend_V3.mkc", "HOLE_healers_sword.mkc", null, 2, 0, 1, 16); //2
        addPhasePref("cerberus_first_V3.mkc", "HORIZ_cerberus_first_V3.mkc", "HOLE_slowtime_V2.mkc", null, 2, 0, 1.1f, 13); //3
        addPhasePref("cerberus_less_malus.mkc", "HORIZ_cerberus_less_malus.mkc", "HOLE_attacker.mkc", "HOLE_HORIZ_attacker.mkc", 2, 1, 1.1f, 13); //4

        addPhasePref("only_red_friends.mkc", null, null, null, 1, 0, 1f, 1); //5 -> pausa per mostrare scritta
        addPhasePref(null, null, null, null, 1, 0, 1f, 6); //6 -> pausa per mostrare scritta
        addPhasePref("solo_gargoyles.mkc", null, "HOLE_solo_healers.mkc", null, 1, 1, 1.2f, 4); //7 -> solo di transizione, per non dare troppo vantaggio dalle 5 piattaforme vuote
        addPhasePref("platform_gargoyles.mkc", "HORIZ_platform_gargoyles.mkc", "HOLE_gargoyle.mkc", "HOLE_HORIZ_gargoyle.mkc", 3, 2, 1.2f, 16); //8
        addPhasePref("platform_gargoyles_V2.mkc", "HORIZ_platform_gargoyles.mkc", "HOLE_gargoyle_V2.mkc", "HOLE_HORIZ_gargoyle_V2.mkc", 3, 1, 1.1f, 13); //9

        addPhasePref("only_red_friends.mkc", null, null, null, 1, 0, 1f, 1); //10 -> pausa per mostrare scritta
        addPhasePref(null, null, null, null, 1, 0, 1f, 6); //11 -> pausa per mostrare scritta
        addPhasePref("yellow_friends.mkc", "HORIZ_yellow_friends.mkc", "HOLE_yellow_friend.mkc", "HOLE_HORIZ_yellow_friend.mkc", 3, 2, 1.2f, 15); //12
        addPhasePref("yellow_friends.mkc", "HORIZ_yellow_friends.mkc", "HOLE_flying_gargoyles.mkc", "HOLE_HORIZ_flying_gargoyles.mkc", 3, 2, 1.2f, 20); //13

        addPhasePref("only_yellow_friends.mkc", null, null, null, 1, 0, 1f, 1); //14 -> pausa per mostrare scritta
        addPhasePref(null, null, null, null, 1, 0, 1f, 6); //15 -> pausa per mostrare scritta

        addPhasePref("golems.mkc", "HORIZ_golems.mkc", "HOLE_ice.mkc", null, 3, 0, 1.05f, 15, true, true); //16
        addPhasePref("golems_with_everybody.mkc", "HORIZ_golems_with_everybody.mkc", "HOLE_bluesceptre.mkc", "HOLE_HORIZ_bluesceptre.mkc", 3, 1, 1.1f, 20, true, true); //17

        addPhasePref("only_yellow_friends.mkc", null, null, null, 1, 0, 1f, 1); //18 -> pausa per mostrare scritta
        addPhasePref(null, null, null, null, 1, 0, 1f, 6); //19 -> pausa per mostrare scritta
        addPhasePref("giant_demons.mkc", "HORIZ_giant_demons.mkc", "HOLE_bluesceptre_V2.mkc", "HOLE_HORIZ_bluesceptre_V2.mkc", 3, 2, 1.1f, 30); //20

        addPhasePref("only_orange_friends.mkc", null, null, null, 1, 0, 1f, 1); //21 -> pausa per mostrare scritta
        addPhasePref(null, null, null, null, 1, 0, 1f, 3); //22: primo boss


        //------ dopo boss-----
        addPhasePref("everything.mkc", "HORIZ_everything.mkc", "HOLE_bluesceptre_V2.mkc", "HOLE_HORIZ_bluesceptre_V2.mkc", 3, 2, 1.1f, 20); //23 -> fase con tutto per introdurre fasi particolari
        addPhasePref("special_really_fast.mkc", "HORIZ_special_really_fast.mkc", "HOLE_really_fast.mkc", null, 3, 0, 1.6f, 25, true); //24, molto veloce con tanta probabilità ai chibi (fase speciale)
        addPhasePref("special_really_fast.mkc", "HORIZ_special_really_fast.mkc", "HOLE_really_fast_healers.mkc", "HOLE_HORIZ_really_fast_healers.mkc", 3, 1, 1.45f, 25, true); //25, molto veloce con tanta probabilità ai chibi e anche con healers (fase speciale)
        addPhasePref("only_orange_friends.mkc", "only_orange_friends.mkc", null, null, 3, 0, 1.4f, 3, true); //26, giusto per terminare la fase 25 diversamente

        addPhasePref("only_big_guys.mkc", "HORIZ_only_big_guys.mkc", "HOLE_only_big_guys.mkc", null, 2, 1, 1.3f, 25); //27, solo nemici grandi, facciamo in modo che spawnino solo piattaforme grandi
        addPhasePref("everything_special_malus.mkc", "HORIZ_special_malus.mkc", "HOLE_bluesceptre_V2.mkc", "HOLE_HORIZ_bluesceptre_V2.mkc", 3, 2, 1f, 25, true, true); //28 -> fase speciale: piattaforme piene con 1 nemico centrale e 2 malus ai lati (o duale)

        addPhasePref(null, null, "HOLE_only_sceptre.mkc", null, 1, 1, 1, 2); //29 -> diamo scettro al giocatore
        addPhasePref("solo_violet_demon.mkc", "solo_violet_demon.mkc", "HOLE_solo_healers.mkc", null, 2, 2, 1.1f, 13); //30 -> solo healers e demoni viola
        addPhasePref("sometimes_malus.mkc", null, "HOLE_solo_attackers.mkc", null, 1, 2, 1f, 12); //31 -> solo angeli attaccanti e ogni tanto malus


        //da qui mettiamo tutto bilanciato con velocità che aumenta fino a 2, e poi continua all'infinito
        addPhasePref("everything.mkc", "HORIZ_everything.mkc", "HOLE_bluesceptre_V2.mkc", "HOLE_HORIZ_bluesceptre_V2.mkc", 3, 2, 1.5f, 25, true, true); //32
        addPhasePref("everything.mkc", "HORIZ_everything.mkc", "HOLE_bluesceptre_V2.mkc", "HOLE_HORIZ_bluesceptre_V2.mkc", 3, 2, 1.6f, 25, true, true); //33
        addPhasePref("everything.mkc", "HORIZ_everything.mkc", "HOLE_bluesceptre_V2.mkc", "HOLE_HORIZ_bluesceptre_V2.mkc", 3, 2, 1.7f, 25, true, true); //34
        addPhasePref("everything.mkc", "HORIZ_everything.mkc", "HOLE_bluesceptre_V2.mkc", "HOLE_HORIZ_bluesceptre_V2.mkc", 3, 2, 1.8f, 25, true, true); //35
        addPhasePref("everything.mkc", "HORIZ_everything.mkc", "HOLE_bluesceptre_V2.mkc", "HOLE_HORIZ_bluesceptre_V2.mkc", 3, 2, 1.9f, 25, true, true); //36
        addPhasePref("everything.mkc", "HORIZ_everything.mkc", "HOLE_bluesceptre_V2.mkc", "HOLE_HORIZ_bluesceptre_V2.mkc", 3, 2, 2f, 150, true, true); //37
        addPhasePref("everything_orange_malus.mkc", "HORIZ_everything_orange_malus.mkc", "HOLE_bluesceptre_V2.mkc", "HOLE_HORIZ_bluesceptre_V2.mkc", 3, 2, 2f, 25, true, true); //38


        if(isSfxOn()) addObjToLoad(Constants.PORTAL_SFX, 1, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.FRIEND_DEATH_SFX, 1, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.EXPLOSION_ENEMY_SFX, 1, LoadObjectType.SOUND);
        addObjToLoad(Constants.HELL_METEORS, 1, LoadObjectType.TEXTURE_ATLAS);
        addObjToLoad(Constants.DEVIL_CHIBI_DIRECTORY, 1, LoadObjectType.ENEMY);
        addObjToLoad(Constants.HELL_KNIGHT_CHIBI_DIRECTORY, 1, LoadObjectType.ENEMY);
        addObjToLoad(Constants.YELLOW_FRIEND_DIRECTORY, 1, LoadObjectType.ENEMY);
        addObjToLoad(Constants.FRIEND_BALL_EFFECT, 1, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.FIRE_EXPLOSION_EFFECT, 1, LoadObjectType.PARTICLE_EFFECT);

        if(isSfxOn()) addObjToLoad(Constants.BONUS_TAKEN_SFX, 2, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.SWORD_DAMAGE_SFX, 2, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.SWORD_SWING_SFX, 2, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.HEALTH_POTION_SFX, 2, LoadObjectType.SOUND);
        addObjToLoad(Constants.HEAL_FIREBALL_EFFECT, 2, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.HEAL_EFFECT, 2, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.SPARKLE_ATLAS, 2, LoadObjectType.TEXTURE_ATLAS);
        addObjToLoad(Constants.FALLEN_ANGEL_1_DIRECTORY,2, LoadObjectType.ENEMY);
        addObjToLoad(Constants.SWORD_BONUS_DIRECTORY, 2, LoadObjectType.BONUS);
        addObjToLoad(Constants.BONUS_ICON_TYPE_1_DIR, 2, LoadObjectType.BONUS);
        addPhysicParticleEmitterToLoad(Constants.PHYSIC_PARTICLE_FEATHER, 2, Constants.DEF_MIN_FEATHER_RADIUS, Constants.DEF_MAX_FEATHER_RADIUS, false, false, EFFECTS_LAYER);

        if(isSfxOn()) addObjToLoad(Constants.SLOWTIME_SFX, 3, LoadObjectType.SOUND);
        addObjToLoad(Constants.CERBERUS_DIRECTORY, 3, LoadObjectType.ENEMY);
        addObjToLoad(Constants.MEDIUM_HEAL_EFFECT, 3, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.LARGE_HEAL_EFFECT, 3, LoadObjectType.PARTICLE_EFFECT);

        if(isSfxOn()) addObjToLoad(Constants.SCEPTRE_SPAWN_SFX, 4, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.SCEPTRE_EXPLOSION_SFX, 4, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.WIND_SFX, 4, LoadObjectType.SOUND);
        addObjToLoad(Constants.WIND_EFFECT, 4, LoadObjectType.SHEET_SPRITER_EFFECT);
        addObjToLoad(Constants.SCEPTRE_BALL_EFFECT, 4, LoadObjectType.SHEET_SPRITER_EFFECT);
        addObjToLoad(Constants.FIREBALL_EFFECT, 4, LoadObjectType.PARTICLE_EFFECT);

        if(isSfxOn()) addObjToLoad(Constants.THUNDER_SFX, 7, LoadObjectType.SOUND);
        addObjToLoad(Constants.GARGOYLE_1_DIRECTORY, 7, LoadObjectType.ENEMY);
        addObjToLoad(Constants.LIGHTNING_EFFECT, 7, LoadObjectType.SHEET_SPRITER_EFFECT);

        if(isSfxOn()) addObjToLoad(Constants.RAGE_SFX, 12, LoadObjectType.SOUND);

        if(isSfxOn()) addObjToLoad(Constants.ICE_ACTIVATION_SFX, 16, LoadObjectType.SOUND);
        if(isMusicOn()) addObjToLoad(Constants.HELL_SOUNDTRACK2, 16, LoadObjectType.MUSIC);
        addObjToLoad(Constants.LAVA_GOLEM_DIRECTORY, 16, LoadObjectType.ENEMY);
        addObjToLoad(Constants.LAVA_GOLEM_3_DIRECTORY, 16, LoadObjectType.ENEMY);
        addPhysicParticleEmitterToLoad(Constants.PHYSIC_PARTICLE_LAVA_ROCKS, 16, Constants.DEF_MIN_LAVAROCKS_RADIUS, Constants.DEF_MAX_LAVAROCKS_RADIUS, true, true, EFFECTS_LAYER);
        addPhysicParticleEmitterToLoad(Constants.PHYSIC_PARTICLE_LAVA_ROCKS_2, 16, Constants.DEF_MIN_LAVAROCKS2_RADIUS, Constants.DEF_MAX_LAVAROCKS2_RADIUS, true, true, EFFECTS_LAYER);
        addPhysicParticleEmitterToLoad(Constants.PHYSIC_PARTICLE_LAVA_ROCKS_3, 16, Constants.DEF_MIN_LAVAROCKS3_RADIUS, Constants.DEF_MAX_LAVAROCKS3_RADIUS, true, true, EFFECTS_LAYER);
        addObjToLoad(Constants.ICE_EFFECT, 16, LoadObjectType.SHEET_SPRITER_EFFECT);
        addObjToLoad(Constants.EXPLOSION_EFFECT, 16, LoadObjectType.SHEET_SPRITER_EFFECT);

        addObjToLoad(Constants.BLUE_DEMON_DIRECTORY, 20, LoadObjectType.ENEMY);
        addObjToLoad(Constants.RED_DEMON_DIRECTORY, 20, LoadObjectType.ENEMY);
        addObjToLoad(Constants.PURPLE_DEMON_DIRECTORY, 20, LoadObjectType.ENEMY);

        if(isMusicOn()) addObjToLoad(Constants.BOSS_LOOP_SOUNDTRACK, 21, LoadObjectType.MUSIC);
        if(isMusicOn()) addObjToLoad(Constants.BOSS_INTRO_SOUNDTRACK, 21, LoadObjectType.MUSIC);
        if(isSfxOn()) addObjToLoad(Constants.BIG_EXPLOSION_SFX, 21, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BIG_EXPLOSION2_SFX, 21, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BOSS_DEATH_SFX, 21, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BOSS_HURT_SFX, 21, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BOSS_ROAR_SFX, 21, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BOSS_ATTACK_SFX, 21, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BOSS_BALL_EXPLOSION_SFX, 21, LoadObjectType.SOUND);
        addObjToLoad(Constants.ELEMENTAL_DIRECTORY, 21, LoadObjectType.ENEMY);
        addPhysicParticleEmitterToLoad(Constants.PHYSIC_PARTICLE_ELEM_ROCKS, 21, Constants.DEF_MIN_ELEM_ROCKS_RADIUS, Constants.DEF_MAX_ELEM_ROCKS_RADIUS, true, false, BOSS_LAYER);
        addObjToLoad(Constants.BOSS_FIREBALL, 21, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.BOSS_FIREBALL_EXPLOSION, 21, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.TREASURE_BALL, 21, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.TREASURE_EXPLOSION, 21, LoadObjectType.PARTICLE_EFFECT);

        setPhase(0);
    }

    @Override
    public void onEnteringPhase(int num) {
        if(mustSpawnOnlyBigPlatforms(num-1)) { //fase dopo quella in cui spawnano tante piattaforme
            padSpawner.addPadAvailable(Constants.HELL_PAD_1);
            padSpawner.addPadAvailable(Constants.HELL_PAD_2);
            padSpawner.addPadAvailable(Constants.HELL_PAD_4);
        }

        if(mustSpawnOnlyBigPlatforms(num)) {
            padSpawner.removePadAvailable(Constants.HELL_PAD_1);
            padSpawner.removePadAvailable(Constants.HELL_PAD_2);
            padSpawner.removePadAvailable(Constants.HELL_PAD_4);
        }

        holeFiller.setApplyFilters(!mustRemoveHoleFilters(num));
    }

    @Override
    protected void createMessageMechanism(Skin skin) {
        super.createMessageMechanism(skin);

        addScheduledMessage(LanguageManager.Text.HELL_RUN_AWAY, 3.f, 69); //69
        addScheduledMessage(LanguageManager.Text.HELL_NOT_THAT_BAD, 3.f, 109);
        addScheduledMessage(LanguageManager.Text.HELL_GOLEMS_CREATED, 3.f, 151);
        addScheduledMessage(LanguageManager.Text.HELL_ANNOYING, 3.f, 193);
        addScheduledMessage(LanguageManager.Text.HELL_REAL_SHOW, 3.f, 230);
    }

    @Override
    public String getSoundtrackPath() {
        if(player != null && player.getJumpedPlatforms() >= SECOND_SOUNDTRACK_PLATFORM)
            return Constants.HELL_SOUNDTRACK2;
        return super.getSoundtrackPath();
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
            addScheduledMessage(LanguageManager.Text.HELL_BOSS_DEATH, 3.f, player.getJumpedPlatforms()); //il boss viene dopo tutti gli altri testi...
    }


    @Override
    protected void buildCameraController() {
        camController = new CameraController(player, getStage().getCamera(), 0, goingUp,
                getPreferences().getBoolean(Constants.SCREENSHAKE_PREFS, true)) {

            @Override
            public Vector2 getDownUpBoundings() {
                upDownBoundings.set(getRestoredCameraY() - getViewportHeight()*0.5f + ((HellBackground)background).getLavaHeight(),
                                        getRestoredCameraY() + getViewportHeight()*0.5f);
                return upDownBoundings;
            }
        };
    }

    @Override
    protected void showDeadMenu(boolean first) {
        super.showDeadMenu(first);
        /*if(suggestion == null) {
            suggestion = new Container<>(new Label("", assetManager.get(Constants.DEFAULT_SKIN_PATH, FreeTypeSkin.class)));
            suggestion.setTransform(true);
            //suggestion.setScale(1.5f);
            suggestion.getActor().setAlignment(Align.center);
            hudGroup.addActor(suggestion);
        }

        suggestion.setVisible(!first && player.getHp() <= 0 && Utils.getScore(player) <= Math.min(SUGGESTION_SCORE, highscore));
        if(suggestion.isVisible()) {
            suggestion.getActor().setText(getLanguageManager().getText(Utils.getHellSuggestion(player)));
        }*/

        //cimitero sbloccato ma ancora non comunicato
        if(!first && player.getHp() <= 0 && Utils.isCemeteryUnlocked(Utils.getScore(player)) && getPreferences().getBoolean(Constants.CEMETERY_UNLOCKED_DIALOG_PREF, true)) {
            Skin skin = assetManager.get(Constants.DEFAULT_SKIN_PATH, FreeTypeSkin.class);
            cemeteryUnlockedDialog = new Dialog("", skin) {
                @Override
                public void result(Object obj) {
                    getSoundManager().click();
                    dialogBlackImg.remove();
                }
            };

            cemeteryUnlockedDialog.text(getLanguageManager().getText(LanguageManager.Text.GENERAL_CONGRATULATIONS), skin.get("Score", Label.LabelStyle.class));
            cemeteryUnlockedDialog.getContentTable().row();
            cemeteryUnlockedDialog.text(getLanguageManager().getText(LanguageManager.Text.CEMETERY_UNLOCKED));
            cemeteryUnlockedDialog.button("  Ok  ");
            cemeteryUnlockedDialog.setMovable(false);
            cemeteryUnlockedDialog.setModal(true);
            showDialog(cemeteryUnlockedDialog);
            getPreferences().putBoolean(Constants.CEMETERY_UNLOCKED_DIALOG_PREF, false);
            getPreferences().flush();
        }
    }

    @Override
    public void updateDeadMenu(float x, float y, float w, float h, float delta) {
        super.updateDeadMenu(x, y, w, h, delta);
        //if(player.getHp() <= 0)
        //    suggestion.setPosition(x, y + 0.13f * h);
        if(cemeteryUnlockedDialog != null && cemeteryUnlockedDialog.getStage() != null)
            updateDialog(cemeteryUnlockedDialog);
    }

}
