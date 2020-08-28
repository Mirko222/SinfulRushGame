package com.pizzaroof.sinfulrush.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.async.ThreadUtils;
import com.brashmonkey.spriter.gdx.SpriterData;
import com.brashmonkey.spriter.gdx.SpriterDataLoader;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticleEmitter;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticleEmitterLoader;
import com.pizzaroof.sinfulrush.util.Utils;

/**come main menu, ma in più carica assets extra, facendo sembrare il caricamento
 * iniziale più veloce*/
public class MainMenuLoaderScreen extends MainMenuScreen {

    private SpriterDataLoader.SpriterDataParameter spriterDataParameter;
    private boolean loadingDone;

    public MainMenuLoaderScreen(NGame game, boolean first, int exitsWithoutInterstitial) {
        super(game, first, exitsWithoutInterstitial);
        spriterDataParameter = new SpriterDataLoader.SpriterDataParameter();
        loadAssets();
        loadingDone = false;
    }

    @Override
    public void updateLogic(float delta) {
        super.updateLogic(delta);
        loadingDone = loadingDone || assetManager.update(); //non vogliamo chiamare sempre update, perché anche se è tutto caricato fa un po' di lavoro...
    }

    private void loadAssets() {
        //assets comuni
        assetManager.load(com.pizzaroof.sinfulrush.Constants.PUNCH_HIT_SFX, Sound.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.PUNCH_DAMAGE_SFX, Sound.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.ENEMY_DEATH_SFX, Sound.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.ENEMY_DEATH2_SFX, Sound.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.PLAYER_HURT_SFX, Sound.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.PLAYER_DEATH_SFX, Sound.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.CLOCK_TICK_SFX, Sound.class);
        //assetManager.load(Constants.JUMP_SFX, Sound.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.PHYSIC_PARTICLE_ATLAS, TextureAtlas.class);
        loadPhysicParticleEmitter(com.pizzaroof.sinfulrush.Constants.PHYSIC_PARTICLE_BLOOD, com.pizzaroof.sinfulrush.Constants.DEF_MIN_BLOOD_RADIUS, com.pizzaroof.sinfulrush.Constants.DEF_MAX_BLOOD_RADIUS);
        loadSpriterEffect(com.pizzaroof.sinfulrush.Constants.DISAPPEARING_SMOKE);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.PUNCH_ATLAS, TextureAtlas.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.HUD_ATLAS, TextureAtlas.class);

        //assets inferno
        assetManager.load(com.pizzaroof.sinfulrush.Constants.HELL_SOUNDTRACK, Music.class);
        loadPadAtlas(com.pizzaroof.sinfulrush.Constants.HELL_PAD_1);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.HELL_GRADIENT_BG, Texture.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.HELL_DECORATIONS, TextureAtlas.class);

        //assets cimitero
        if(Utils.isCemeteryUnlocked(getPreferences())) { //se non è sbloccato, sicuramente dovrò andare all'inferno
            assetManager.load(com.pizzaroof.sinfulrush.Constants.CEMETERY_SOUNDTRACK_INTRO, Music.class);
            assetManager.load(com.pizzaroof.sinfulrush.Constants.CEMETERY_SOUNDTRACK_LOOP, Music.class);
            assetManager.load(com.pizzaroof.sinfulrush.Constants.CEMETERY_GRADIENT_BG, Texture.class);
            loadPhysicParticleEmitter(com.pizzaroof.sinfulrush.Constants.PHYSIC_PARTICLE_BONES, com.pizzaroof.sinfulrush.Constants.DEF_MIN_BONES_RADIUS, com.pizzaroof.sinfulrush.Constants.DEF_MAX_BONES_RADIUS);
            //loadPhysicParticleEmitter(Constants.PHYSIC_PARTICLE_ZOMBIEP, Constants.DEF_MIN_ZOMBIEP_RADIUS, Constants.DEF_MAX_ZOMBIEP_RADIUS);
            loadPadAtlas(com.pizzaroof.sinfulrush.Constants.CEMETERY_PAD_1);
            assetManager.load(com.pizzaroof.sinfulrush.Constants.CEMETERY_FIRST_GRADIENT_BG, Texture.class);
        }

        //loadEnemy(Constants.DEMON_DARKNESS_1_DIRECTORY);
        //loadEnemy(Constants.DEMON_DARKNESS_2_DIRECTORY);
    }

    /**chiamata quando si sta andando allo scenario dell'inferno*/
    @Override
    protected void goingToHellScreen(String playerDir) {
        while(!assetManager.isLoaded(Constants.HELL_DECORATIONS)
            || !assetManager.isLoaded(Utils.playerScmlPath(playerDir))) { //lo possiamo fare se mettiamo prima tutto inferno, poi tutto cimitero, ma considera che quelle del cimitero verranno caricate nel gameplay
            assetManager.update();
            ThreadUtils.yield();
        }

        //assetManager.finishLoading();
    }

    /**chiamata quando si sta andando allo scenario del cimitero*/
    @Override
    protected void goingToCemeteryScreen(String playerDir) {

        while(!assetManager.isLoaded(Constants.CEMETERY_FIRST_GRADIENT_BG) ||
                !assetManager.isLoaded(Utils.playerScmlPath(playerDir))) {
            assetManager.update();
            ThreadUtils.yield();
        }
        //assetManager.finishLoading();
    }

    /*@Override
    protected void goingToTutorialScreen() {
        String pad = Utils.padAtlasPath(Constants.TUTORIAL_PAD_1);
        while(!assetManager.isLoaded(Constants.HUD_ATLAS) || !assetManager.isLoaded(pad)) {
            assetManager.update();
            ThreadUtils.yield();
        }
    }*/

    private void loadPhysicParticleEmitter(String file, int minRadius, int maxRadius) {
        PhysicParticleEmitterLoader.PhysicParticleEmitterParameter parameter = new PhysicParticleEmitterLoader.PhysicParticleEmitterParameter();
        parameter.minParticleRadius = minRadius;
        parameter.maxParticleRadius = maxRadius;
        assetManager.load(file, PhysicParticleEmitter.class, parameter);
    }

    private void loadSpriterEffect(String directory) {
        assetManager.load(com.pizzaroof.sinfulrush.util.Utils.sheetEffectScmlPath(directory), SpriterData.class, spriterDataParameter);
    }

    private void loadPadAtlas(String pad) {
        assetManager.load(com.pizzaroof.sinfulrush.util.Utils.padAtlasPath(pad), TextureAtlas.class);
    }

    //carica un nemico
    private void loadEnemy(String directory) {
        assetManager.load(Utils.enemyScmlPath(directory), SpriterData.class, spriterDataParameter);
    }

    public void resetLoadingDone() {
        this.loadingDone = false;
    }
}
