package com.pizzaroof.sinfulrush.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pizzaroof.sinfulrush.language.LanguageManager;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.audio.SoundManager;

/**schermata generica (tutte le schermate con una funzionalità specifica ereditano da questa)*/
public abstract class AbstractScreen implements Screen {

    protected Stage stage; //stage di questa schermata
    protected com.pizzaroof.sinfulrush.NGame game; //riferimento al gioco (può tornare utile, ma meglio usarlo il meno possibile per non incasinare le cose)
    protected AssetManager assetManager; //ci teniamo un riferimento all'asset manager (in realtà è accessibile da @game però cosi è più comodo)

    protected Color clearColor;

    protected boolean screenLeft;


    /**schermata alla quale ci vogliamo spostare*/
    protected Screen destinationScreen;

    /**@param game riferimento al gioco (può essere utile)*/
    public AbstractScreen(com.pizzaroof.sinfulrush.NGame game) {
        this.game = game;
        destinationScreen = null;
        Gdx.input.setCatchBackKey(false);
        assetManager = game.getAssetManager();
        initStage();
        getSoundManager().setStage(stage);
        clearColor = Color.WHITE;
    }

    @Override
    public void show() {

    }

    /**usata per cambiare schermo a @screen, viene tenuto conto della pubblicità (se c'è, non si cambia)*/
    public void setDestinationScreen(Screen screen) {
        this.destinationScreen = screen;
    }

    /**metodo chiamato ad ogni frame, @delta è il tempo tra due frame*/
    @Override
    public void render(float delta) {

        //ad ogni frame aggiorniamo la logica e ristampiamo
        //questi due metodi sono stati separati, in modo da dare la possibilità di fare l'override di solo uno dei due
        updateLogic(delta);

        redraw();

        if(destinationScreen != null && !game.isShowingAd())
            game.setScreen(destinationScreen);
    }

    /**metodo in cui si aggiorna la logica della schermata*/
    protected void updateLogic(float delta) {
        if(stage != null)
            stage.act(delta); //aggiorna tutti gli attori
    }

    /**metodo in cui si ristampa tutto a video*/
    protected void redraw() {
        if(stage != null) {
            Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a); //pulisci schermo
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            stage.getViewport().apply(); //usa viewport di stage
            stage.draw(); //ristampa stage
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); //aggiorna viewport dello stage
    }

    @Override
    public void pause() {
        //Gdx.app.log("info", "app paused");
    }

    @Override
    public void resume() {
        //Gdx.app.log("info", "app resumed");
    }

    @Override
    public void hide() {
        this.screenLeft = true;
    }

    /**metodo per rilasciare le risorse*/
    @Override
    public void dispose() {
        stage.dispose();
    }

    /**chiamato per inizializzare lo stage della schermata (fai override se vuoi fare qualcosa di adhoc)*/
    protected void initStage() {
        stage = new Stage();
        stage.setViewport(new FitViewport(com.pizzaroof.sinfulrush.Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT)); //di default creiamo uno stage con una fit viewport (può essere cambiato nelle classi figlie)
        //stage.setViewport(new ExtendViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT));
        //stage.setViewport(new FillViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT));

        Gdx.input.setInputProcessor(stage); //lasciamo allo stage gestire gli input
    }

    public Stage getStage() {
        return stage;
    }

    public NGame getGame() {
        return game;
    }

    public LanguageManager getLanguageManager() {
        return game.getLanguageManager();
    }

    /**dimensione del mondo (in "pixel")*/
    public float getWorldWidth() {
        return stage.getCamera().viewportWidth;
    }

    /**dimensione del mondo (in "pixel")*/
    public float getWorldHeight() {
        return stage.getCamera().viewportHeight;
    }

    public SoundManager getSoundManager() {
        return game.getSoundManager();
    }

    public Preferences getPreferences() {
        return game.getPreferences();
    }

    public boolean hasLeftScreen() {
        return screenLeft;
    }

}
