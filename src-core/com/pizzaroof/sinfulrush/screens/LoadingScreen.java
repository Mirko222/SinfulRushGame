package com.pizzaroof.sinfulrush.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.brashmonkey.spriter.gdx.SpriterData;
import com.brashmonkey.spriter.gdx.SpriterDataLoader;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.HealthBar;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticleEmitter;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticleEmitterLoader;
import com.pizzaroof.sinfulrush.screens.custom.TutorialScreen;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin;
import com.pizzaroof.sinfulrush.util.pools.Pools;

/**schermata di caricamento...*/
public class LoadingScreen extends AbstractScreen {

    /**tempo per quanto sta solo con il logo?*/
    private static final float ONLY_LOGO_TIME = 1.f;
    private float onlyLogoPassed, startProgress;

    //private Label progressLabel;
    private HealthBar progressBar;

    private SpriterDataLoader.SpriterDataParameter spriterDataParameter;

    private Image logo;
    private Texture logoTex;

    private boolean loaded;

    private boolean mustDoTutorial;

    //DEBUG!!
    //private long time; //lo usiamo per sapere il tempo di caricamento delle immagini

    public LoadingScreen(NGame game) {
        super(game);
        onlyLogoPassed = 0;
        startProgress = 0;
        loaded = false;

        mustDoTutorial = getPreferences().getBoolean(com.pizzaroof.sinfulrush.Constants.TUTORIAL_DIALOG_PREF, true);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        if(!loaded)
            startLoadingAssets();
        loaded = true;

        width = (int)stage.getCamera().viewportWidth;
        height = (int)stage.getCamera().viewportHeight;
        logoTex = new Texture(Gdx.files.internal(com.pizzaroof.sinfulrush.Constants.LOGO_TEXTURE), true);
        logoTex.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.MipMapLinearLinear);
        logo = new Image(logoTex);
        float lw = 455.f, lh = 509.f;
        logo.setBounds(width * 0.5f - lw * 0.5f, height * 0.5f - lh * 0.5f, 455.f, 509.f);
        logo.setOrigin(logo.getImageWidth() * 0.5f, logo.getImageHeight() * 0.5f);

        //logo.addAction(Actions.scaleTo(2, 2,1f));

        getStage().addActor(logo);
    }

    @Override
    public void updateLogic(float delta) {
        if(assetManager.update(5)) { //5millis  //ha finito di caricare

            assetManager.get(com.pizzaroof.sinfulrush.Constants.NAME_TEXTURE, Texture.class).setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.MipMapLinearNearest);

            if(mustDoTutorial)
                setDestinationScreen(new TutorialScreen(game,0)); //andiamo al menu principale
            else
                setDestinationScreen(new MainMenuLoaderScreen(game, true, 0)); //andiamo al menu principale

            /*
            if(!assetManager.get(Constants.SWORD_SHADER, ShaderProgram.class).isCompiled())
                Gdx.app.log("debug", "errore nello shader "+Constants.SWORD_SHADER+": "+assetManager.get(Constants.SWORD_SHADER, ShaderProgram.class).getLog());
            if(!assetManager.get(Constants.MAIN_SCREEN_SHADER, ShaderProgram.class).isCompiled())
                Gdx.app.log("debug", "errore nello shader "+Constants.MAIN_SCREEN_SHADER+": "+assetManager.get(Constants.MAIN_SCREEN_SHADER, ShaderProgram.class).getLog());
            */
            //Gdx.app.log("debug", "tempo necessario: "+((System.currentTimeMillis()-time)/1000f));

        }


        if(onlyLogoPassed < ONLY_LOGO_TIME)
            onlyLogoPassed += delta;
        if(progressBar == null && assetManager.isLoaded(com.pizzaroof.sinfulrush.Constants.DEFAULT_SKIN_PATH) && assetManager.isLoaded(com.pizzaroof.sinfulrush.Constants.HEALTH_BAR_ATLAS) && onlyLogoPassed >= ONLY_LOGO_TIME) {
            /*FreeTypeSkin skin = assetManager.get(Constants.DEFAULT_SKIN_PATH, FreeTypeSkin.class);
            progressLabel = new Label(getLanguageManager().getText(LanguageManager.Text.LOADING), skin);
            progressLabel.setColor(239.f / 255.f, 80.f / 255.f, 58.f / 255.f, 1.f);
            progressLabel.setPosition(Constants.VIRTUAL_WIDTH/2.f - progressLabel.getPrefWidth()/2, Constants.VIRTUAL_HEIGHT/4.f - progressLabel.getPrefHeight()/2);
            getStage().addActor(progressLabel); */
            TextureAtlas bar = assetManager.get(com.pizzaroof.sinfulrush.Constants.HEALTH_BAR_ATLAS);
            progressBar = new HealthBar(bar.findRegion("center_in2"), bar.findRegion("border_in2"),
                                    bar.findRegion("center_out2"), bar.findRegion("border_out2"));
            progressBar.setWidth(getStage().getCamera().viewportWidth * 0.5f);
            progressBar.setHeight(50);
            //progressBar.setCenterPosition(getStage().getCamera().viewportWidth * 0.5f, progressLabel.getY() - progressBar.getHeight());
            progressBar.setCenterPosition(getStage().getCamera().viewportWidth * 0.5f, getStage().getCamera().viewportHeight * 0.15f);
            progressBar.setColor(Color.WHITE);
            progressBar.setRealColor(progressBar.getColor());
            progressBar.setHp(0);
            getStage().addActor(progressBar);
            startProgress = assetManager.getProgress();
        }
        if(progressBar != null) {
            //imbrogliamo l'utente.. gli facciamo pensare che il caricamento parte solo dopo il logo
            float progress = ( (1.f - startProgress) <= com.pizzaroof.sinfulrush.Constants.EPS || (1.f - assetManager.getProgress()) <= com.pizzaroof.sinfulrush.Constants.EPS ) ? 1.f :
                                (assetManager.getProgress() - startProgress) / (1.f - startProgress);
            progressBar.setHp(progress < progressBar.getHp() ? 1.f : progress);
        }

        super.updateLogic(delta);
    }

    @Override
    public void hide() {
        super.hide();
        dispose();
    }

    /**carica gli asset nell'asset manager*/
    protected void startLoadingAssets() {
        //time = System.currentTimeMillis();

        //carica prima cose che devono essere fatte subito per forza
        loadSkin();
        //assetManager.finishLoading();
        assetManager.load(Constants.HEALTH_BAR_ATLAS, TextureAtlas.class);

        spriterDataParameter = new SpriterDataLoader.SpriterDataParameter();

        assetManager.load(com.pizzaroof.sinfulrush.Constants.MENU_SOUNDTRACK, Music.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.BUTTON_CLICK_SFX, Sound.class);

        //carichiamo qui tutto il resto:
        loadPlayer(Utils.getPlayerDirToLoad(game.getPreferences()));

        loadEnemy(Constants.DEMON_DARKNESS_1_DIRECTORY);
        loadEnemy(Constants.DEMON_DARKNESS_2_DIRECTORY);

        assetManager.load(Constants.SHOP_PLAYERS_ATLAS, TextureAtlas.class); //personaggi nel negozio

        assetManager.load(com.pizzaroof.sinfulrush.Constants.CUSTOM_BUTTONS_DECORATIONS, TextureAtlas.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.CEMETERY_DECORATIONS, TextureAtlas.class);
        //assetManager.load(Constants.HELL_DECORATIONS, TextureAtlas.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.HELL_DECORATIONS_BG, TextureAtlas.class);

        assetManager.load(com.pizzaroof.sinfulrush.Constants.MENU_BACKGROUND, Texture.class);
        TextureLoader.TextureParameter prm = new TextureLoader.TextureParameter();
        prm.genMipMaps = true;
        assetManager.load(com.pizzaroof.sinfulrush.Constants.NAME_TEXTURE, Texture.class, prm);

        if(mustDoTutorial) { //deve ancora fare il tutorial iniziale
            assetManager.load(com.pizzaroof.sinfulrush.Constants.PUNCH_HIT_SFX, Sound.class);
            assetManager.load(com.pizzaroof.sinfulrush.Constants.PUNCH_DAMAGE_SFX, Sound.class);
            assetManager.load(com.pizzaroof.sinfulrush.Constants.ENEMY_DEATH_SFX, Sound.class);
            assetManager.load(com.pizzaroof.sinfulrush.Constants.ENEMY_DEATH2_SFX, Sound.class);
            assetManager.load(com.pizzaroof.sinfulrush.Constants.PLAYER_HURT_SFX, Sound.class);
            assetManager.load(com.pizzaroof.sinfulrush.Constants.PLAYER_DEATH_SFX, Sound.class);
            assetManager.load(com.pizzaroof.sinfulrush.Constants.PHYSIC_PARTICLE_ATLAS, TextureAtlas.class);
            loadPhysicParticleEmitter(com.pizzaroof.sinfulrush.Constants.PHYSIC_PARTICLE_BLOOD, com.pizzaroof.sinfulrush.Constants.DEF_MIN_BLOOD_RADIUS, com.pizzaroof.sinfulrush.Constants.DEF_MAX_BLOOD_RADIUS);
            loadSpriterEffect(com.pizzaroof.sinfulrush.Constants.DISAPPEARING_SMOKE);
            assetManager.load(com.pizzaroof.sinfulrush.Constants.PUNCH_ATLAS, TextureAtlas.class);
            assetManager.load(com.pizzaroof.sinfulrush.Constants.HUD_ATLAS, TextureAtlas.class);

            assetManager.load(com.pizzaroof.sinfulrush.util.Utils.padAtlasPath(com.pizzaroof.sinfulrush.Constants.TUTORIAL_PAD_1), TextureAtlas.class);
        }

        assetManager.load(com.pizzaroof.sinfulrush.Constants.SWORD_SHADER, ShaderProgram.class);
        assetManager.load(com.pizzaroof.sinfulrush.Constants.MAIN_SCREEN_SHADER, ShaderProgram.class);

    }

    private void loadParticleEffect(String path) {
        ParticleEffectLoader.ParticleEffectParameter param = new ParticleEffectLoader.ParticleEffectParameter();
        param.atlasFile = com.pizzaroof.sinfulrush.Constants.PHYSIC_PARTICLE_ATLAS; //usiamo atlas invece di singoli file
        param.loadedCallback = new AssetLoaderParameters.LoadedCallback() {
            @Override
            public void finishedLoading(AssetManager assetManager, String fileName, Class type) {
                Pools.addEffectPool(fileName, assetManager.get(fileName));
            }
        };
        assetManager.load(path, ParticleEffect.class, param);
    }

    //giocatore organizzato in cartelle: la cartella identifica i dati
    private void loadPlayer(String directory) {
        assetManager.load(Utils.playerScmlPath(directory), SpriterData.class, spriterDataParameter);
    }

    //carica un nemico
    private void loadEnemy(String directory) {
        assetManager.load(com.pizzaroof.sinfulrush.util.Utils.enemyScmlPath(directory), SpriterData.class, spriterDataParameter);
    }

    private void loadBonus(String dir) {
        assetManager.load(com.pizzaroof.sinfulrush.util.Utils.bonusScmlPath(dir), SpriterData.class, spriterDataParameter);
    }

    //carica l'atlas delle piattaforme (dal nome di una piattaforma)
    private void loadPadAtlas(String pad) {
        assetManager.load(com.pizzaroof.sinfulrush.util.Utils.padAtlasPath(pad), TextureAtlas.class);
    }

    private void loadSkin() {
        assetManager.load(Constants.DEFAULT_SKIN_PATH, FreeTypeSkin.class);
    }

    private void loadPhysicParticleEmitter(String file, int minRadius, int maxRadius) {
        PhysicParticleEmitterLoader.PhysicParticleEmitterParameter parameter = new PhysicParticleEmitterLoader.PhysicParticleEmitterParameter();
        parameter.minParticleRadius = minRadius;
        parameter.maxParticleRadius = maxRadius;
        assetManager.load(file, PhysicParticleEmitter.class, parameter);
    }

    private void loadSpriterEffect(String directory) {
        assetManager.load(Utils.sheetEffectScmlPath(directory), SpriterData.class, spriterDataParameter);
    }

    @Override
    public void dispose() {
        super.dispose();
        if(logoTex != null)
            logoTex.dispose();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); //pulisce eventuali rimasugli (evita glitch grafici)
    }
}
