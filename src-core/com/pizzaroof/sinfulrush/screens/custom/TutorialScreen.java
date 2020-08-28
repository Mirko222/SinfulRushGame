package com.pizzaroof.sinfulrush.screens.custom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.async.ThreadUtils;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.TutorialPlayer;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.TutorialBackground;
import com.pizzaroof.sinfulrush.actors.stage.TimescaleStage;
import com.pizzaroof.sinfulrush.language.LanguageManager;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.missions.MissionDataCollector;
import com.pizzaroof.sinfulrush.screens.HudGameplayScreen;
import com.pizzaroof.sinfulrush.screens.MainMenuLoaderScreen;
import com.pizzaroof.sinfulrush.spawner.custom.TutorialHoleFiller;
import com.pizzaroof.sinfulrush.spawner.enemies.MarkovEnemySpawner;
import com.pizzaroof.sinfulrush.spawner.enemies.ScriptedEnemySpawner;
import com.pizzaroof.sinfulrush.spawner.platform.PatternPlatformSpawner;
import com.pizzaroof.sinfulrush.util.PlayerPower;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin;

import java.io.IOException;
import java.util.ArrayList;

/**tutorial... è un gameplay scriptato in sostanza*/
public class TutorialScreen extends HudGameplayScreen {

    private static final String ENEMIES_CHAIN = "markov_chains/tutorial/enemies.mkc";
    private static final String SWORD_CHAIN = "markov_chains/tutorial/sword.mkc";
    private static final String SCEPTRE_CHAIN = "markov_chains/tutorial/sceptre.mkc";
    private static final String BONUS_CHAIN = "markov_chains/tutorial/bonus.mkc";
    private static final String FRIENDS_CHAIN = "markov_chains/tutorial/friends.mkc";

    private float timePassed, timeToPass;
    private int phase;
    private Container<Label> completeLabel, congratulationLabel;

    private int tmpNumGeneratedPlatforms;

    private TextButton okBtn;

    private Dialog takeTutorialDialog;
    private boolean dialogConfirmed;

    public TutorialScreen(NGame game, int exitsWithoutInterstitial) {
        super(game, true, Constants.THIEF_DIRECTORY, 0, new PlayerPower(), exitsWithoutInterstitial);
        canPause = false;
        hudGroup.removeActor(pauseButton);
        adOn = false;

        clearColor = new Color(118.f / 255.f, 196.f / 255.f, 232.f / 255.f, 1);
        message = new Container<Label>() {
            @Override
            public void act(float delta) {
                if(((TimescaleStage)getStage()).isSkipTolerantAct() || phase == 9)
                    super.act(delta);
            }
        };
        message.setTransform(true);
        FreeTypeSkin skin = assetManager.get(Constants.DEFAULT_SKIN_PATH, FreeTypeSkin.class);
        message.setActor(new Label("", skin));
        message.getActor().setAlignment(Align.center);
        hudGroup.addActorAfter(blackStrip, message);

        timePassed = 0;
        timeToPass = -1;
        phase = 0;
        tmpNumGeneratedPlatforms = -1;

        highscoreLabel.getActor().setText("");
        actualScoreLabel.getActor().setText("");

        okBtn = new TextButton("   Ok   ", skin);
        okBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getSoundManager().click();
                setDestinationScreen(new MainMenuLoaderScreen(game, true, exitsWithoutInterstitial));
            }
        });
        hudGroup.addActor(okBtn);
        okBtn.setVisible(false);
        hudGroup.addActor(stickLeft);
        hudGroup.addActor(stickRight);
        stickLeft.setVisible(false);
        stickRight.setVisible(false);

        completeLabel = new Container<>(new Label(getLanguageManager().getText(LanguageManager.Text.TUTORIAL_COMPLETE_TEXT), skin));
        hudGroup.addActor(completeLabel);
        completeLabel.setTransform(true);
        completeLabel.setScale(1.5f);
        completeLabel.setVisible(false);
        congratulationLabel = new Container<>(new Label(getLanguageManager().getText(LanguageManager.Text.GENERAL_CONGRATULATIONS), skin, "Score"));
        hudGroup.addActor(congratulationLabel);
        congratulationLabel.setTransform(true);
        congratulationLabel.setScale(1.5f);
        congratulationLabel.setVisible(false);


        dialogConfirmed = false;
        takeTutorialDialog = new Dialog("", skin) {
            @Override
            public void result(Object obj) {
                if(!dialogConfirmed) {
                    getSoundManager().click();

                    scheduleTimer(2.5f);
                    changeMessageText(getLanguageManager().getText(LanguageManager.Text.TUTORIAL_GAME_OBJ));
                    dialogConfirmed = true;
                    player.takeDamage(50, null);
                    closeDialog(takeTutorialDialog);
                    rageBar.setVisible(true);
                    playerBar.setVisible(true);
                    ingameScore.setVisible(true);
                    weaponIcon.setVisible(true);
                    hearthIcon.setVisible(true);

                    getSoundManager().playSoundtrack(Constants.MENU_SOUNDTRACK);
                }
            }
        };
        takeTutorialDialog.text(getLanguageManager().getText(LanguageManager.Text.TUTORIAL_START));
        takeTutorialDialog.button("  Ok  ");
        takeTutorialDialog.setMovable(false);
        takeTutorialDialog.setModal(true);
        takeTutorialDialog.getContentTable().padBottom(50).padTop(60).padLeft(55).padRight(55);
        takeTutorialDialog.getButtonTable().padBottom(45);
        rageBar.setVisible(false);
        playerBar.setVisible(false);
        ingameScore.setVisible(false);
        hearthIcon.setVisible(false);
        weaponIcon.setVisible(false);
        ((Label)takeTutorialDialog.getContentTable().getCells().first().getActor()).setAlignment(Align.center);
        showDialog(takeTutorialDialog);


        addObjToLoad(Constants.OGRE_DIRECTORY, 1, LoadObjectType.ENEMY);
        if(isSfxOn()) addObjToLoad(Constants.SWORD_DAMAGE_SFX, 1, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.SWORD_SWING_SFX, 1, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.BONUS_TAKEN_SFX, 1, LoadObjectType.SOUND);
        addObjToLoad(Constants.SWORD_BONUS_DIRECTORY, 1, LoadObjectType.BONUS);
        addObjToLoad(Constants.SPARKLE_ATLAS, 1, LoadObjectType.TEXTURE_ATLAS);
        if(isSfxOn()) addObjToLoad(Constants.SCEPTRE_SPAWN_SFX, 1, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.SCEPTRE_EXPLOSION_SFX, 1, LoadObjectType.SOUND);
        addObjToLoad(Constants.SCEPTRE_BALL_EFFECT, 1, LoadObjectType.SHEET_SPRITER_EFFECT);
        addObjToLoad(Constants.BONUS_ICON_TYPE_1_DIR, 1, LoadObjectType.BONUS);
        if(isSfxOn()) addObjToLoad(Constants.HEALTH_POTION_SFX, 1, LoadObjectType.SOUND);
        addObjToLoad(Constants.HEAL_EFFECT, 1, LoadObjectType.PARTICLE_EFFECT);
        if(isSfxOn()) addObjToLoad(Constants.PORTAL_SFX, 1, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.EXPLOSION_ENEMY_SFX, 1, LoadObjectType.SOUND);
        if(isSfxOn()) addObjToLoad(Constants.FRIEND_DEATH_SFX, 1, LoadObjectType.SOUND);
        addObjToLoad(Constants.YELLOW_FRIEND_DIRECTORY, 1, LoadObjectType.ENEMY);
        addObjToLoad(Constants.FRIEND_BALL_EFFECT, 1, LoadObjectType.PARTICLE_EFFECT);
        addObjToLoad(Constants.FIRE_EXPLOSION_EFFECT, 1, LoadObjectType.PARTICLE_EFFECT);
    }

    @Override
    protected void initSoundtrack() {
    }

    @Override
    public void resize(int w, int h) {
        super.resize(w, h);
        if (blackStrip != null)
            blackStrip.setWidth(camController.getViewportWidth() + 2 * CameraController.MAX_X_OFFSET);
    }

    @Override
    public void updateLogic(float delta) {
        if(!dialogConfirmed) {
            updateDialog(takeTutorialDialog);
            return;
        }

        if(phase < 9)
            super.updateLogic(delta);

        if( (armory.isUsingSword(false) && phase == 3) || (armory.isUsingSceptre(false) && phase == 5)) { //spada (o scettro) presa
            holeFiller.setVerticalChain(null);
            ((MarkovEnemySpawner)enemySpawner).changeMarkovChain(ENEMIES_CHAIN);
            changeMessageText(getLanguageManager().getText(phase == 3 ? LanguageManager.Text.TUTORIAL_USE_SWORD : LanguageManager.Text.TUTORIAL_USE_SCEPTRE));
            tmpNumGeneratedPlatforms = getNumGeneratedPlatforms();
            phase++;
        }

        switch (phase) {
            case 2:
                if(tmpNumGeneratedPlatforms < 0 && enemiesGroup.getChildren().size == 0) { //ucciso tutti i nemici col tap
                    changeMessageText(getLanguageManager().getText(LanguageManager.Text.TUTORIAL_TAKE_SWORD));
                    holeFiller.setVerticalChain(SWORD_CHAIN);
                    waitUntilLoaded(Constants.SPARKLE_ATLAS);
                    phase++;
                }
                break;
            case 4:
                if(tmpNumGeneratedPlatforms < 0 && enemiesGroup.getChildren().size == 0) { //uccisi tutti con spada
                    changeMessageText(getLanguageManager().getText(LanguageManager.Text.TUTORIAL_TAKE_SCEPTRE));
                    holeFiller.setVerticalChain(SCEPTRE_CHAIN);
                    waitUntilLoaded(Constants.SCEPTRE_BALL_EFFECT);
                    phase++;
                }
                break;
            case 6:
                if(tmpNumGeneratedPlatforms < 0 && enemiesGroup.getChildren().size == 0) { //fatte uccisioni con scettro
                    changeMessageText(getLanguageManager().getText(LanguageManager.Text.TUTORIAL_TAKE_BONUS));
                    holeFiller.setVerticalChain(BONUS_CHAIN);
                    waitUntilLoaded(Constants.HEAL_EFFECT);
                    phase++;
                }
                break;

            case 9: //tutorial finito

                getPreferences().putBoolean(Constants.TUTORIAL_DIALOG_PREF, false);
                getPreferences().flush();

                float x = camController.getRestoredCameraX(), y = camController.getRestoredCameraY(), w = camController.getViewportWidth(), h = camController.getViewportHeight();
                okBtn.setPosition(x - okBtn.getWidth()*0.5f, y - h * 0.4f);
                message.setPosition(message.getX(), y);
                congratulationLabel.setPosition(x, y + congratulationLabel.getActor().getHeight()*1.5f);
                completeLabel.setPosition(x, y + 0.35f * h);
                blackBg.setPosition(x - w * 0.5f - CameraController.MAX_X_OFFSET,y - h * 0.5f - CameraController.MAX_Y_OFFSET);
                stickRight.setWidth(200);
                stickLeft.setWidth(-200);
                stickRight.setPosition(completeLabel.getX() + 250,completeLabel.getY() - stickRight.getHeight() * 0.5f);
                stickLeft.setPosition(completeLabel.getX() - 250, stickRight.getY());
                okBtn.act(delta);
                message.act(delta);
                //updateMessageMechanism();
                break;
        }

        if(tmpNumGeneratedPlatforms > 0 && getNumGeneratedPlatforms() - tmpNumGeneratedPlatforms >= 3) {
            tmpNumGeneratedPlatforms = -1;
            ((MarkovEnemySpawner) enemySpawner).changeMarkovChain(null);
        }

        if(!isInPause()) {
            if(timeToPass > 0 && timePassed < timeToPass) {
                timePassed += delta;
                if(timePassed >= timeToPass)
                    onTimerEnded();
            }
        }
    }


    @Override
    /**crea il pad spawner*/
    protected void buildPlatformSpawner() {
        padSpawner = new PatternPlatformSpawner(goingUp, assetManager, Constants.VIRTUAL_WIDTH, world2d, PATTERN_PROBABILITY);
        padSpawner.addPadAvailable(Constants.TUTORIAL_PAD_1); //inseriamo tutte le piattaforme
        padSpawner.addPadAvailable(Constants.TUTORIAL_PAD_2);
    }

    @Override
    public boolean isPossibleToSpawnBoss() {
        return false;
    }

    private void changeMessageText(String msg) {
        message.addAction(Actions.sequence(Actions.fadeOut(0.4f, Interpolation.fade),
                                            Actions.run(new Runnable() {
                                                @Override
                                                public void run() {
                                                    message.getActor().setText(msg);
                                                    message.getActor().pack();
                                                    blackStrip.addAction(Actions.sizeTo(blackStrip.getWidth(), message.getActor().getHeight()+60, 0.4f, Interpolation.fade));
                                                }
                                            }),
                                            Actions.fadeIn(0.4f, Interpolation.fade)));
    }

    @Override
    protected void updateMessageMechanism() {
        float x = camController.getRestoredCameraX(), y = camController.getRestoredCameraY(), w = camController.getViewportWidth(), h = camController.getViewportHeight();
        message.setPosition(x, y + h * 0.3f);
        blackStrip.setPosition(-CameraController.MAX_X_OFFSET, message.getY() - blackStrip.getHeight()*0.5f);
    }

    private void onTimerEnded() {
        timePassed = 0;
        timeToPass = -1;

        phase++;

        switch (phase) {
            case 1:
                waitUntilLoaded(Utils.enemyScmlPath(Constants.OGRE_DIRECTORY));
                tmpNumGeneratedPlatforms = getNumGeneratedPlatforms();
                ((MarkovEnemySpawner)enemySpawner).changeMarkovChain(ENEMIES_CHAIN);
                scheduleTimer(2.5f);
                break;

            case 2:
                changeMessageText(getLanguageManager().getText(LanguageManager.Text.TUTORIAL_TAP));
                break;
        }
    }

    @Override
    protected float getMinimumYToFillWithPlatforms() {
        int dir = goingUp ? 1 : -1;
        return dir * getStage().getCamera().viewportHeight * 0.5f + getStage().getCamera().position.y; //aggiungiamo piattaforme finchè non superiamo quest'altezza (riempiamo più di tutto lo schermo)
    }

    private void scheduleTimer(float time) {
        timeToPass = time;
        timePassed = 0;
    }

    @Override
    public void buildBackground(CameraController cameraController, AssetManager assetManager, Group bgFrontLayer) {
        background = new TutorialBackground(cameraController, assetManager, layerGroups[BACKGROUND_LAYER]);
    }

    @Override
    public void updateActualScoreLabelText() { //non c'è score nel tutorial
    }

    @Override
    public void initEnemySpawner() {
        chains = new ArrayList<>();
        enemySpawner = new ScriptedEnemySpawner(0, getSoundManager(), player);
        //((ScriptedEnemySpawner)enemySpawner).addScriptedEnemy(5, Constants.DEMON_DARKNESS_1_DIRECTORY);
    }

    private void waitUntilLoaded(String asset) {
        while(!assetManager.isLoaded(asset) && !assetManager.update()) {
            ThreadUtils.yield();
        }
    }

    public void onPlayerHeal() {
        if(phase == 7) { //presa cura
            holeFiller.setVerticalChain(null);
            ((MarkovEnemySpawner)enemySpawner).changeMarkovChain(FRIENDS_CHAIN);
            changeMessageText(getLanguageManager().getText(LanguageManager.Text.TUTORIAL_FRIENDS));
            player.changeSpeed(1.2f); //facciamo player più veloce, cosi ci mette di meno per arrivare dagli amici
            phase++;
        }
    }

    public void onSavedFriend() {
        if(phase == 8) {
            message.remove();
            hudGroup.addActor(message);
            message.getActor().setText(getLanguageManager().getText(LanguageManager.Text.TUTORIAL_FINISHED));
            okBtn.setVisible(true);
            blackBg.setVisible(true);
            completeLabel.setVisible(true);
            congratulationLabel.setVisible(true);
            stickRight.setVisible(true);
            stickLeft.setVisible(true);
            blackStrip.setVisible(false);
            Gdx.input.setInputProcessor(stage);
            phase++;
        }
    }

    @Override
    public void initHoleFiller() {
        holeFiller = new TutorialHoleFiller(world2d, stage, assetManager, getSoundManager(), layerGroups, player, enemiesGroup, bonusGroup, camController, armory, getPreferences());
    }

    @Override
    public void updatePreferences(boolean reset) {
    }

    protected boolean backToPlayMenuWhenExit() {
        return true;
    }

    @Override
    protected void createMissionDataCollector() {
        missionDataCollector = null;
    }

    @Override
    protected void buildPlayer(Platform firstPlatform, String directory, int charmaps, PlayerPower power) throws IOException {
        float hplayer = Float.parseFloat(Utils.getInternalReader(Utils.playerInfoPath(Constants.THIEF_DIRECTORY)).readLine().split(" ")[1]);
        Vector2 pInitPos = new Vector2((firstPlatform.getX() + firstPlatform.getWidth()/2) / Constants.PIXELS_PER_METER, //x iniziale: centro piattaforma
                (firstPlatform.getY() + firstPlatform.getHeight() + hplayer/2) / Constants.PIXELS_PER_METER); //y iniziale: sopra piattaforma
        player = TutorialPlayer.createPlayer(directory, this, world2d, 1, pInitPos,1f, assetManager, stage,
                getPreferences().getBoolean(Constants.VIBRATIONS_PREFS, true), getSoundManager(), layerGroups[EFFECTS_LAYER], getSoundtrackPath());
        player.setCharacterMaps(charmaps);
    }


    @Override
    public String getSoundtrackPath() {
        return Constants.MENU_SOUNDTRACK;
    }
}
