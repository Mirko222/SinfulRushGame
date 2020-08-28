package com.pizzaroof.sinfulrush.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.HealthBar;
import com.pizzaroof.sinfulrush.actors.ScoreButton;
import com.pizzaroof.sinfulrush.actors.basics.NGImageTextButton;
import com.pizzaroof.sinfulrush.actors.basics.TextureActor;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.actors.stage.ShaderFreeGroup;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;
import com.pizzaroof.sinfulrush.actors.stage.TimescaleStage;
import com.pizzaroof.sinfulrush.language.LanguageManager;
import com.pizzaroof.sinfulrush.menus.MissionsMenu;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.missions.MissionManager;
import com.pizzaroof.sinfulrush.util.Pair;
import com.pizzaroof.sinfulrush.util.PlayerPower;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin;

import java.util.ArrayList;
import java.util.HashMap;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;

/**gameplay screen con hud*/
public class HudGameplayScreen extends AdGameplayScreen {

    private static final float TIME_TO_RESPAWN = 6.f;

    protected ImageButton pauseButton;
    protected Container<Label> pauseLabel, actualScoreLabel, highscoreLabel, diedLabel, countdownLabel, congratulationsLabel, newBestScoreLabel;
    protected TextureActor blackBg;
    protected Image blackStrip, dialogBlackImg;
    public TextButton pauseExitButton;
    public ScoreButton ingameScore;
    protected Image stickLeft, stickRight, hearthIcon;

    protected HealthBar playerBar;

    private TextButton deadExit;
    public NGImageTextButton deadContinue, deadRestart, resumeButton;
    private float deadRespawnCountdown;

    protected ShaderFreeGroup hudGroup;

    private HashMap<String, TextureRegion> weapIconsMap;

    protected Image weaponIcon;

    private TextButton.TextButtonStyle bronzeStyle, silverStyle, goldStyle, diamondStyle;

    protected HealthBar rageBar;

    /**non vogliamo premere restart o exit se uno dei due è già stato premuto*/
    private boolean restartOrExitClicked;

    private Dialog exitDialog, errorVideoDialog;

    /**messaggio dal boss al giocatore da visualizzare a schermo*/
    protected Container<Label> message;

    private ArrayList<ScheduledMessage> scheduledMessages = new ArrayList<>();
    private int messageIndex;

    /**usata nei menu di pausa e morte per impedire che si selezionino più opzioni contemporeaneamente*/
    private boolean optionSelected;

    private boolean continueButtonClicked;

    /**tabella in cui facciamo vedere le missioni attive*/
    protected Table activeMissionTable;

    public HudGameplayScreen(NGame game, boolean goingUp, String directory, int charmaps, PlayerPower powers, int exitsWithoutInterstitial) {
        super(game, goingUp, directory, charmaps, powers, exitsWithoutInterstitial);
        restartOrExitClicked = false;

        continueButtonClicked = false;

        weapIconsMap = new HashMap<>();
        messageIndex = 0;

        com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin skin = getGame().getAssetManager().get(com.pizzaroof.sinfulrush.Constants.DEFAULT_SKIN_PATH);
        hudGroup = new ShaderFreeGroup() {
            @Override
            public boolean needToSwitch() { //ci serve switchare solamente in caso di rage mode o di slowmotion
                ShaderStage stage = ((ShaderStage)HudGameplayScreen.this.getStage());
                return stage.isRageModeOn() || Math.abs(1.f - stage.getTimeMultiplier()) > com.pizzaroof.sinfulrush.Constants.EPS;
            }
        };

        bronzeStyle = skin.get("bronze", TextButton.TextButtonStyle.class);
        silverStyle = skin.get("silver", TextButton.TextButtonStyle.class);
        goldStyle = skin.get("gold", TextButton.TextButtonStyle.class);
        diamondStyle = skin.get("diamond", TextButton.TextButtonStyle.class);

        activeMissionTable = new Table();
        createMainHud(skin);
        createMessageMechanism(skin);
        createPauseMenu(skin);
        createDeadMenu(skin);


        getStage().addActor(hudGroup);
    }

    @Override
    public void updateLogic(float delta) {
        super.updateLogic(delta);

        //aggiorna posizione degli elementi che ci sono sempre
        float x = camController.getRestoredCameraX(), y = camController.getRestoredCameraY(), w = camController.getViewportWidth(), h = camController.getViewportHeight();

        updateMainHud(x, y, w, h);
        updatePauseMenu(x, y, w, h, delta);
        updateDeadMenu(x, y, w, h, delta);
        updateMessageMechanism();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if(blackBg != null) {
            blackBg.setWidth(camController.getViewportWidth() + 4 * CameraController.MAX_X_OFFSET);
            blackBg.setHeight(camController.getViewportHeight() + 4 * CameraController.MAX_Y_OFFSET); //gli offset servono per tener conto dello screenshake
            blackStrip.setSize(blackBg.getWidth(), blackStrip.getHeight());
            dialogBlackImg.setSize(blackBg.getWidth(), blackBg.getHeight());
        }
    }

    @Override
    public void onPlayerDied(boolean first) {
        super.onPlayerDied(first);
        showDeadMenu(first);
        getSoundManager().stopSoundtrack();
        if(first)
            deadRespawnCountdown = TIME_TO_RESPAWN;
        else
            deadRespawnCountdown = -1;
    }

    @Override
    public void keepPlayingLevel() {
        super.keepPlayingLevel();
        showDeadMenu(false);
    }

    public void showPauseMenu() {
        blackBg.setVisible(isInPause());
        pauseLabel.setVisible(isInPause());
        resumeButton.setVisible(isInPause());
        pauseExitButton.setVisible(isInPause());
        actualScoreLabel.setVisible(isInPause());
        highscoreLabel.setVisible(isInPause());
        stickLeft.setVisible(isInPause());
        stickRight.setVisible(isInPause());
        if(isInPause()) {
            updateMissions(false);
            activeMissionTable.setVisible(true);
            createActiveMissionsTable(); //va ricreata ogni volta perché le missioni si aggiornano
        }
        else
            activeMissionTable.setVisible(false);

        if(isInPause())
            optionSelected = false;
    }

    protected void showDeadMenu(boolean first) {
        boolean dead = player.getHp() <= 0;
        blackBg.setVisible(dead);
        deadContinue.setVisible(dead && first);
        deadExit.setVisible(dead && !first);
        deadRestart.setVisible(dead && !first);
        stickLeft.setVisible(dead);
        stickRight.setVisible(dead);
        diedLabel.setVisible(dead);
        actualScoreLabel.setVisible(dead && !first);
        highscoreLabel.setVisible(dead && !first);
        countdownLabel.setVisible(dead && first);

        activeMissionTable.setVisible(false);
        if(dead && !first) {
            updateMissions(true);
            if(Utils.getScore(player) <= highscore) {
                createActiveMissionsTable();
                activeMissionTable.setVisible(true);
            }
            updateMissionPrefs();
        }

        congratulationsLabel.setVisible(dead && !first && Utils.getScore(player) > highscore);
        newBestScoreLabel.setVisible(congratulationsLabel.isVisible());

        if(dead)
            optionSelected = false;
    }

    private void updateCurrentWeaponIcon() {
        if(weaponIcon == null) weaponIcon = new Image();
        weaponIcon.setDrawable(new TextureRegionDrawable(getCurrentWeaponRegion()));
    }

    private TextureRegion getCurrentWeaponRegion() {
        if(armory.isUsingPunch()) return getWeaponRegion("pugno");
        if(armory.isUsingSword(false))
            return getWeaponRegion(armory.getFingerSword() > 1 ? "sword_x2" : "sword");
        if(armory.isUsingSword(true))
            return getWeaponRegion(armory.getFingerSword() > 1 ? "rage_x2" : "rage");
        if(armory.isUsingSceptre(false)) return getWeaponRegion("staff2");
        else if(armory.isUsingSceptre(true)) return getWeaponRegion("staff");
        return getWeaponRegion("guanto1");
    }

    private TextureRegion getWeaponRegion(String regname) {
        if(weapIconsMap == null) weapIconsMap = new HashMap<>();
        if(!weapIconsMap.containsKey(regname))
            weapIconsMap.put(regname, getGame().getAssetManager().get(com.pizzaroof.sinfulrush.Constants.HUD_ATLAS, TextureAtlas.class).findRegion(regname));
        return weapIconsMap.get(regname);
    }

    private void updateMedal() {
        int score = com.pizzaroof.sinfulrush.util.Utils.getScore(player);
        if(score >= getDiamondJumps()) ingameScore.setStyle(diamondStyle);
        else if(score >= getGoldJumps()) ingameScore.setStyle(goldStyle);
        else if(score >= getSilverJumps()) ingameScore.setStyle(silverStyle);
        else ingameScore.setStyle(bronzeStyle);
    }

    @Override
    public void onWeaponChanged() {
        updateCurrentWeaponIcon();
    }


    @Override
    public void setInPause(boolean pause) {
        if(pause) {
            resumeButton.setScale(1);
            resumeButton.getColor().a = 1;
        }
        super.setInPause(pause);
    }

    private void createMainHud(com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin skin) {
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0.8f);
        px.fill();
        blackBg = new TextureActor(new Texture(px));
        blackStrip = new Image(new Texture(px));
        px.setColor(0, 0, 0, 0.5f);
        px.fill();
        dialogBlackImg = new Image(new Texture(px));
        px.dispose();
        blackStrip.setHeight(0);

        TextureAtlas atlas = assetManager.get(com.pizzaroof.sinfulrush.Constants.HEALTH_BAR_ATLAS, TextureAtlas.class);
        playerBar = new HealthBar(atlas.findRegion(com.pizzaroof.sinfulrush.Constants.HEALTH_BAR_IN_CENTER_NAME), atlas.findRegion(com.pizzaroof.sinfulrush.Constants.HEALTH_BAR_IN_BORDER_NAME),
                atlas.findRegion(com.pizzaroof.sinfulrush.Constants.HEALTH_BAR_OUT_CENTER_NAME), atlas.findRegion(com.pizzaroof.sinfulrush.Constants.HEALTH_BAR_OUT_BORDER_NAME));
        playerBar.setColor(Color.RED);
        playerBar.setWidth(500);
        playerBar.setHeight(60);
        playerBar.setRealColor(playerBar.getColor());
        hearthIcon = new Image(assetManager.get(com.pizzaroof.sinfulrush.Constants.HUD_ATLAS, TextureAtlas.class).findRegion("cuore"));
        hearthIcon.setSize(100, 100);

        //weaponIcon = new Image();
        weaponIcon.setSize(100, 100);

        ingameScore = new ScoreButton("", skin);
        ingameScore.setTransform(true);
        ingameScore.setSize(100, 100);
        ingameScore.setTouchable(Touchable.disabled);

        rageBar = new HealthBar(atlas.findRegion("center_in1"), atlas.findRegion("border_in1"),
                atlas.findRegion("center_out1"), atlas.findRegion("border_out1"));
        rageBar.setColor(Color.WHITE);
        rageBar.setRealColor(rageBar.getColor());
        rageBar.setSize(100, 20);
        rageBar.setHp(0);
        rageBar.setLerpWeight(10);
        rageBar.setFast0(true);

        hudGroup.addActor(playerBar);
        hudGroup.addActor(hearthIcon);
        hudGroup.addActor(ingameScore);
        hudGroup.addActor(weaponIcon);
        hudGroup.addActor(rageBar);

        enemySpawner.setScoreButton(ingameScore);
        enemySpawner.setHudGroup(hudGroup);
    }

    private void createPauseMenu(com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin skin) {
        pauseButton = new ImageButton(skin);
        pauseButton.setWidth(100);
        pauseButton.setHeight(100);
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(player.getHp() <= 0) return;
                setInPause(true);
                getSoundManager().click();
                getSoundManager().pauseSoundtrack();
                showPauseMenu();
                initPauseMenuPosition();

                timePlayed += (Math.max(0, TimeUtils.millis() - timeStart) / 1000L); //in pausa il tempo che passa non conta
            }
        });

        hudGroup.addActor(pauseButton);

        pauseLabel = new Container<>(new Label(getLanguageManager().getText(LanguageManager.Text.PAUSE), skin));
        pauseLabel.setTransform(true);
        pauseLabel.setScale(1.5f);

        //NB: le chiamate a update pause, durante la pausa, saranno non collegate al timescale stage!!!

        resumeButton = new NGImageTextButton(getLanguageManager().getText(LanguageManager.Text.RESUME), skin);
        com.pizzaroof.sinfulrush.util.Utils.getReadyForAnimations(resumeButton);
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(!optionSelected) {
                    optionSelected = true;
                    getSoundManager().click();
                    resumeButton.addAction(com.pizzaroof.sinfulrush.util.Utils.clickAction(new Runnable() {
                        @Override
                        public void run() {
                            setInPause(false);
                            getSoundManager().resumeSoundtrack();
                            showPauseMenu();
                            timeStart = TimeUtils.millis();
                        }
                    }, MainMenuScreen.DEF_ANIM_DUR));
                }
            }
        });
        pauseExitButton = new TextButton(getLanguageManager().getText(LanguageManager.Text.EXIT), skin, "blue");
        pauseExitButton.setSize(resumeButton.getWidth(), resumeButton.getHeight());
        com.pizzaroof.sinfulrush.util.Utils.getReadyForAnimations(pauseExitButton);
        pauseExitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(!optionSelected) {
                    optionSelected = true;
                    getSoundManager().click();
                    showExitDialog();
                }
            }
        });

        actualScoreLabel = new Container<>(new Label("", skin, "Score"));
        actualScoreLabel.setTransform(true);
        highscoreLabel = new Container<>(new Label("", skin));
        updateHighscoreText();
        highscoreLabel.setTransform(true);
        highscoreLabel.setWidth(highscoreLabel.getActor().getWidth());
        highscoreLabel.setHeight(highscoreLabel.getActor().getHeight());

        stickLeft = new Image(assetManager.get(Constants.DEFAULT_SKIN_ATLAS, TextureAtlas.class).findRegion("bar"));
        stickLeft.setWidth(-296);
        stickLeft.setHeight(30);
        stickRight = new Image(stickLeft.getDrawable());
        stickRight.setWidth(296);
        stickRight.setHeight(30);

        showPauseMenu();

        hudGroup.addActor(blackBg);
        hudGroup.addActor(pauseLabel);
        hudGroup.addActor(resumeButton);
        hudGroup.addActor(pauseExitButton);
        hudGroup.addActor(actualScoreLabel);
        hudGroup.addActor(highscoreLabel);
        hudGroup.addActor(stickLeft);
        hudGroup.addActor(stickRight);
        hudGroup.addActor(activeMissionTable);
        hudGroup.setTransform(true);

        exitDialog = new Dialog("", skin) {
            @Override
            public void result(Object obj) {
                optionSelected = false;
                if(obj.equals(Boolean.TRUE)) { //ha confermato che vuole uscire
                    updatePreferences(false);
                    if(exitsWithoutInterstitial >= 2 && adOn) {
                        //alla terza... interstitial
                        getGame().showInterstitial();
                    }
                    int nexits;
                    if(adOn)
                        nexits = (player.getHp() > 0 && exitsWithoutInterstitial < 2) ? exitsWithoutInterstitial+1 : 0;
                    else
                        nexits = exitsWithoutInterstitial;

                    game.getMissionManager().undoProgress(); //togliamo progressi fatti con le missioni
                    setDestinationScreen(new com.pizzaroof.sinfulrush.screens.MainMenuLoaderScreen(game, backToPlayMenuWhenExit(), nexits));
                } else {
                    closeDialog(exitDialog);
                    //exitDialog.hide(fadeOut(0.1f, Interpolation.fade));
                }
                getSoundManager().click();
            }
        };
        exitDialog.cancel();
        exitDialog.text(getLanguageManager().getText(LanguageManager.Text.EXIT_CONFIRMATION_GAME), skin.get("darker", Label.LabelStyle.class));
        exitDialog.getContentTable().padBottom(50).padTop(60).padLeft(55).padRight(55);
        exitDialog.getButtonTable().padBottom(45);
        TextButton yesB = new TextButton(getLanguageManager().getText(LanguageManager.Text.YES), skin);
        TextButton noB = new TextButton(getLanguageManager().getText(LanguageManager.Text.NO), skin);
        exitDialog.button(yesB, true);
        exitDialog.button(noB, false);
        exitDialog.getButtonTable().getCells().first().width(200).padRight(23);
        exitDialog.getButtonTable().getCells().get(1).width(200).padLeft(23);
        exitDialog.setMovable(false);
        exitDialog.setModal(true); //non vogliamo che possa cliccare da altre parti

    }

    private void createDeadMenu(FreeTypeSkin skin) {
        deadRestart = new NGImageTextButton(getLanguageManager().getText(LanguageManager.Text.RESTART), skin) {
            @Override
            public void act(float delta) {
                if(getStage() != null && ((TimescaleStage)getStage()).isSkipTolerantAct())
                    super.act(delta);
            }
        };
        com.pizzaroof.sinfulrush.util.Utils.getReadyForAnimations(deadRestart);
        deadRestart.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(restartOrExitClicked) return;
                restartOrExitClicked = true;
                getSoundManager().click();
                deadRestart.addAction(com.pizzaroof.sinfulrush.util.Utils.clickAction(new Runnable() {
                    @Override
                    public void run() {
                        restartGame();
                    }
                }, MainMenuScreen.DEF_ANIM_DUR));
            }
        });
        deadExit = new TextButton(getLanguageManager().getText(LanguageManager.Text.EXIT), skin, "blue") {
            @Override
            public void act(float delta) {
                if(((TimescaleStage)getStage()).isSkipTolerantAct())
                    super.act(delta);
            }
        };
        deadExit.setSize(deadRestart.getWidth(), deadRestart.getHeight());
        com.pizzaroof.sinfulrush.util.Utils.getReadyForAnimations(deadExit);
        deadExit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(restartOrExitClicked) return;
                restartOrExitClicked = true;
                getSoundManager().click();
                deadExit.addAction(com.pizzaroof.sinfulrush.util.Utils.clickAction(new Runnable() {
                    @Override
                    public void run() {
                        setDestinationScreen(new MainMenuLoaderScreen(game, false, 0));
                    }
                }, MainMenuScreen.DEF_ANIM_DUR));
            }
        });

        deadContinue = new NGImageTextButton(getLanguageManager().getText(LanguageManager.Text.WATCH_VIDEO), skin) {
            @Override
            public void act(float delta) {
                if(getStage() != null && ((TimescaleStage)getStage()).isSkipTolerantAct())
                    super.act(delta);
            }
        };
        com.pizzaroof.sinfulrush.util.Utils.getReadyForAnimations(deadContinue);
        deadContinue.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(continueButtonClicked) return; //già cliccato
                continueButtonClicked = true;

                getSoundManager().click();
                deadContinue.addAction(com.pizzaroof.sinfulrush.util.Utils.clickAction(new Runnable() {
                    @Override
                    public void run() {
                        getGame().startRewardedVideo();
                    }
                }, MainMenuScreen.DEF_ANIM_DUR));
            }
        });
        diedLabel = new Container<>(new Label("Game Over", skin));
        diedLabel.setTransform(true);
        diedLabel.setScale(1.5f);

        countdownLabel = new Container<>(new Label("", skin, "Score"));
        countdownLabel.setTransform(true);
        countdownLabel.setScale(1.5f);

        congratulationsLabel = new Container<>(new Label(getLanguageManager().getText(LanguageManager.Text.GENERAL_CONGRATULATIONS), skin, "Score"));
        congratulationsLabel.setTransform(true);
        congratulationsLabel.setScale(1.5f);
        newBestScoreLabel = new Container<>(new Label(getLanguageManager().getText(LanguageManager.Text.NEW_BESTSCORE_MESSAGE), skin));
        newBestScoreLabel.setTransform(true);

        showDeadMenu(false);
        hudGroup.addActor(deadExit);
        hudGroup.addActor(deadRestart);
        hudGroup.addActor(deadContinue);
        hudGroup.addActor(diedLabel);
        hudGroup.addActor(countdownLabel);
        hudGroup.addActor(congratulationsLabel);
        hudGroup.addActor(newBestScoreLabel);

        errorVideoDialog = new Dialog("", skin) {
            @Override
            public void result(Object obj) {
                closeDialog(errorVideoDialog);
            }
        };
        errorVideoDialog.text(getLanguageManager().getText(LanguageManager.Text.ERROR_LOADING_VIDEO), skin.get("darker", Label.LabelStyle.class));
        ((Label)errorVideoDialog.getContentTable().getCells().first().getActor()).setAlignment(Align.center);
        errorVideoDialog.button("  Ok  ", true);
        errorVideoDialog.setMovable(false);
        errorVideoDialog.setModal(true); //non vogliamo che possa cliccare da altre parti
        errorVideoDialog.getContentTable().padBottom(50).padTop(60).padLeft(55).padRight(55);
        errorVideoDialog.getButtonTable().padBottom(45);
    }

    protected void createMessageMechanism(Skin skin) {
        hudGroup.addActor(blackStrip);
        message = new Container<Label>() {
            @Override
            public void act(float delta) {
                if(((TimescaleStage)getStage()).isSkipTolerantAct())
                    super.act(delta);
            }
        };
        message.setTransform(true);
        message.setActor(new Label("", skin));
        message.getActor().setAlignment(Align.center);
        message.getColor().a = 0;
        hudGroup.addActor(message);
    }

    private void setMessage(LanguageManager.Text msg, float duration) {
        if(message.hasActions()) {
            //ancora azioni... deve prima sparire poi apparire
            message.clearActions();
            //blackStrip.clearActions();
            message.addAction(Actions.sequence(Actions.fadeOut(0.25f, Interpolation.fade), Actions.run(new Runnable() {
                @Override
                public void run() {
                    setMessageUnchecked(msg, duration);
                }
            })));
        } else {
            //deve solo apparire
            setMessageUnchecked(msg, duration);
        }
    }

    private void setMessageUnchecked(LanguageManager.Text msg, float duration) {
        String string = getLanguageManager().getText(msg);
        message.getActor().setText(string);
        message.getActor().pack();

        message.addAction(Actions.sequence(Actions.fadeIn(0.5f, Interpolation.fade), //appear
                Actions.delay(duration), //wait
                Actions.fadeOut(0.3f, Interpolation.fade))); //disappear
    }

    protected void updateMessageMechanism() {
        if(message.hasActions()) {
            message.setVisible(true);
            blackStrip.setVisible(true);
            float x = camController.getRestoredCameraX(), y = camController.getRestoredCameraY(), h = camController.getViewportHeight();
            message.setPosition(x, y + h * 0.3f);
            blackStrip.setPosition(-CameraController.MAX_X_OFFSET, message.getY() - blackStrip.getHeight() * 0.5f);
            blackStrip.setHeight( (message.getActor().getHeight() + 60) * message.getColor().a );
        }
        else {
            message.setVisible(false);
            blackStrip.setVisible(false);
        }

        //arrivati al prossimo messaggio programmato... fallo partire
        if(messageIndex < scheduledMessages.size() && player.getJumpedPlatforms() >= scheduledMessages.get(messageIndex).startPlatform) {
            setMessage(scheduledMessages.get(messageIndex).msg, scheduledMessages.get(messageIndex).duration);
            messageIndex++;
        }
    }

    //x, y, w, h sono relative alla telecaemra
    private void updateMainHud(float x, float y, float w, float h) {
        ingameScore.setText(Integer.toString(com.pizzaroof.sinfulrush.util.Utils.getScore(player)));
        float spaceHud = 43.f;
        ingameScore.setPosition(spaceHud, y + h * 0.5f - 100 + playerBar.getHeight() * 0.5f - ingameScore.getHeight() * 0.5f);
        updateMedal();
        weaponIcon.setPosition(ingameScore.getX() + ingameScore.getWidth() + spaceHud, ingameScore.getY());

        hearthIcon.setPosition(weaponIcon.getX() + weaponIcon.getWidth() + spaceHud, ingameScore.getY());
        playerBar.setPosition(hearthIcon.getX() + hearthIcon.getWidth() * 0.65f, y + h * 0.5f - 100);

        playerBar.setSmoothHp((float)player.getHp() / (float)player.getMaxHp());
        pauseButton.setPosition(x + w * 0.5f - spaceHud - pauseButton.getWidth(), hearthIcon.getY());

        rageBar.setPosition(weaponIcon.getX(), weaponIcon.getY() - rageBar.getHeight() - 5);
        float hp;
        if(armory.getRemainingToRage() <= 0)
            hp = (armory.getRageDuration() - armory.getRageTimePassed()) / armory.getRageDuration();
        else
            hp = (float)(armory.getNeededToRage() - armory.getRemainingToRage()) / (float)armory.getNeededToRage();
        rageBar.setVisible(armory.isUsingSword(true));
        rageBar.setSmoothHp(hp);
    }


    public void updatePauseMenu(float x, float y, float w, float h, float delta) {
        updatePauseMenu(x, y, w, h, delta, false);
    }

    public void updatePauseMenu(float x, float y, float w, float h, float delta, boolean forceUpdate) {
        if(isInPause() || forceUpdate) { //quando è vero, si fa una sola chiamata per frame!!!!! non dipende da time scale stage!!!!
            stickLeft.setWidth(-296);
            stickRight.setWidth(296);

            pauseLabel.setPosition(x,y + 0.35f * h);
            blackBg.setPosition(x - w * 0.5f - CameraController.MAX_X_OFFSET,y - h * 0.5f - CameraController.MAX_Y_OFFSET);

            resumeButton.setPosition(x - resumeButton.getWidth() * 0.5f,
                    pauseExitButton.getY() + pauseExitButton.getHeight() + resumeButton.getHeight()*0.2f);
            resumeButton.act(delta); //in pausa non viene chiamato l'update

            pauseExitButton.setPosition(x - pauseExitButton.getWidth()*0.5f, y - 0.4f * h);
            pauseExitButton.act(delta);

            updateActualScoreLabelText();

            highscoreLabel.setPosition(x - highscoreLabel.getActor().getWidth() * 0.5f,
                    resumeButton.getY() + resumeButton.getHeight() + highscoreLabel.getActor().getHeight() * 1.5f);

            actualScoreLabel.setPosition(x,highscoreLabel.getY() + actualScoreLabel.getActor().getHeight() * 1.5f);

            stickRight.setPosition(pauseLabel.getX() + pauseLabel.getActor().getWidth(),
                    pauseLabel.getY() - stickRight.getHeight() * 0.5f);
            stickLeft.setPosition(pauseLabel.getX() - pauseLabel.getActor().getWidth(),
                    stickRight.getY());

            activeMissionTable.setWidth(w - 2* MissionsMenu.DESCR_PAD);
            activeMissionTable.setX(MissionsMenu.DESCR_PAD);
            activeMissionTable.setY(stickLeft.getY() - MissionManager.MISSIONS_PER_GROUP * 130);

            if(exitDialog.getStage() != null) {
                updateDialog(exitDialog);
                exitDialog.act(delta);
            }
        }
    }

    public void initPauseMenuPosition() {
        float x = camController.getRestoredCameraX(), y = camController.getRestoredCameraY(), w = camController.getViewportWidth(), h = camController.getViewportHeight();
        updatePauseMenu(x, y, w, h, 0, true);
    }

    protected void updateDeadMenu(float x, float y, float w, float h, float delta) {
        if(player.getHp() <= 0) {

            if(deadRespawnCountdown > 0 && !deadContinue.hasActions()) {
                int prev = (int)deadRespawnCountdown;
                deadRespawnCountdown -= delta;
                if(((int)deadRespawnCountdown != prev && prev != (int)TIME_TO_RESPAWN)) {
                    float pitch = prev == 1 ? 1.5f : 1;//0.8f + (5.f - deadRespawnCountdown) / 5.f * 0.7f;
                    //float pitch = 0.8f + Interpolation.exp10In.apply((5.f - deadRespawnCountdown) / 5.f) * 0.7f;
                    getSoundManager().clockTick(pitch);
                }

                countdownLabel.getActor().setText(Integer.toString((int)deadRespawnCountdown));
                if(deadRespawnCountdown < 0)
                    moveToFinalDeadMenu();
            }

            stickLeft.setWidth(-236);
            stickRight.setWidth(236);

            diedLabel.setPosition(x, y + 0.35f * h);
            diedLabel.setPosition(x, y + 0.35f * h);
            blackBg.setPosition(x - w * 0.5f - CameraController.MAX_X_OFFSET, y - h * 0.5f - CameraController.MAX_Y_OFFSET);

            deadExit.setPosition(x - deadExit.getWidth()*0.5f,
                    y - h * 0.5f + deadRestart.getHeight());
            deadRestart.setPosition(x - deadRestart.getWidth()*0.5f, //+ deadExit.getWidth() + 50,
                    deadExit.getY() + deadExit.getHeight() + deadRestart.getHeight()*0.2f);
            deadContinue.setPosition(x - deadContinue.getWidth() * 0.5f, y - h * 0.5f + deadContinue.getHeight());

            stickRight.setPosition(diedLabel.getX() + diedLabel.getActor().getWidth()*0.8f,
                    diedLabel.getY() - stickRight.getHeight() * 0.5f);
            stickLeft.setPosition(diedLabel.getX() - diedLabel.getActor().getWidth()*0.8f, stickRight.getY());

            updateActualScoreLabelText();
            highscoreLabel.setPosition(x - highscoreLabel.getActor().getWidth() * 0.5f,
                    deadRestart.getY() + deadRestart.getHeight() + highscoreLabel.getActor().getHeight() * 1.5f);
            actualScoreLabel.setPosition(x,highscoreLabel.getY() + actualScoreLabel.getActor().getHeight() * 1.5f);
            countdownLabel.setPosition(x, deadContinue.getY() + deadContinue.getHeight() + countdownLabel.getActor().getHeight() * 2.f);

            congratulationsLabel.setPosition(x, y + h * 0.1f);
            newBestScoreLabel.setPosition(x, congratulationsLabel.getY() - newBestScoreLabel.getActor().getHeight() * 1.5f);

            activeMissionTable.setWidth(w - 2* MissionsMenu.DESCR_PAD);
            activeMissionTable.setX(MissionsMenu.DESCR_PAD);
            activeMissionTable.setY(stickLeft.getY() - MissionManager.MISSIONS_PER_GROUP * 130);

            if(errorVideoDialog.getStage() != null) {
                updateDialog(errorVideoDialog);
                errorVideoDialog.act(delta);
            }
        }
    }

    protected void updateActualScoreLabelText() {
        String score = Integer.toString(com.pizzaroof.sinfulrush.util.Utils.getScore(player));
        if(!actualScoreLabel.getActor().getText().toString().equals(score)) {
            updateHighscoreText();
            actualScoreLabel.getActor().setText(Integer.toString(com.pizzaroof.sinfulrush.util.Utils.getScore(player)));
            actualScoreLabel.layout();
        }
    }

    public void showExitDialog() {
        showDialog(exitDialog);
    }

    public void showErrorVideoDialog() {
        showDialog(errorVideoDialog);
    }

    protected void updateDialog(Dialog dialog) {
        float x = camController.getRestoredCameraX(), y = camController.getRestoredCameraY();
        dialog.setPosition(x - dialog.getWidth()*0.5f, y - dialog.getHeight()*0.5f);
        dialogBlackImg.setPosition(x - dialogBlackImg.getWidth()*0.5f, y - dialogBlackImg.getHeight()*0.5f);
    }

    protected void closeDialog(Dialog dialog) {
        dialog.cancel();
        dialog.remove();
        dialogBlackImg.remove();
    }

    private void moveToFinalDeadMenu() {
        getGame().showInterstitial();
        showDeadMenu(false);
    }

    @Override
    public void onErrorPlayingVideo() {
        //System.out.println("ERROR PLAYING VIDEO");
        moveToFinalDeadMenu();
        showErrorVideoDialog();
    }

    protected void showDialog(Dialog dialog) {
        if(dialog.getStage() == null) {
            hudGroup.addActor(dialogBlackImg);
            hudGroup.addActor(dialog);
            dialog.pack();
            //dialog.show(stage, fadeIn(0.2f, Interpolation.fade));
            float x = camController.getRestoredCameraX(), y = camController.getRestoredCameraY();
            dialog.setPosition(x - dialog.getWidth()*0.5f, y - dialog.getHeight()*0.5f);
        }
    }

    protected void updateHighscoreText() {
        highscoreLabel.getActor().setText("(Best score: "+Math.max(highscore, Utils.getScore(player))+")");
        highscoreLabel.layout();
    }

    public class ScheduledMessage {
        LanguageManager.Text msg;
        float duration;
        int startPlatform;
    }

    /**DEVONO ESSERE INSERITI IN ORDINE CRESCENTE DI START PLATFORM*/
    public void addScheduledMessage(LanguageManager.Text msg, float duration, int startPlatform) {
        ScheduledMessage smsg = new ScheduledMessage();
        smsg.msg = msg;
        smsg.duration = duration;
        smsg.startPlatform = startPlatform;
        scheduledMessages.add(smsg);
    }

    @Override
    protected void createBoss(BossGameScreen screen) {
        super.createBoss(screen);
        boss.setScoreButton(ingameScore);
        boss.setHudGroup(hudGroup);
    }

    private void createActiveMissionsTable() {
        activeMissionTable.clear();
        //activeTable.setDebug(true);
        FreeTypeSkin skin = assetManager.get(Constants.DEFAULT_SKIN_PATH, FreeTypeSkin.class);

        for(Mission ms : game.getMissionManager().getActiveMissions()) {
            //descrizione della missione
            activeMissionTable.row().expandX();
            Container<Label> descr;
            try {
                descr = new Container<>(new Label(ms.getDisplayDescription(getLanguageManager()), skin));
            }catch(Exception e) {
                descr = new Container<>(new Label("", skin));
            }
            descr.setTransform(true);
            descr.setScale(0.8f);
            descr.pack();
            activeMissionTable.add(descr).left();

            Image check = new Image(assetManager.get(Constants.CUSTOM_BUTTONS_DECORATIONS, TextureAtlas.class).findRegion(ms.isCompleted() ? "pallino_pieno" : "pallino_vuoto"));
            //activeTable.add(check).size(55).right().bottom();
            boolean multiLines = ms.getDisplayDescription(getLanguageManager()).contains("\n");
            if(multiLines)
                activeMissionTable.add(check).size(55).right().bottom().spaceBottom(30);
            else
                activeMissionTable.add(check).size(55).right().bottom();


            //ricompensa della missione
            activeMissionTable.row().padBottom(20);

            Image coin = new Image(assetManager.get(Constants.COIN_ATLAS, TextureAtlas.class).findRegion(Constants.COIN_REG_NAME));
            Container<Label> reward = new Container<>(new Label("[#FFCE21]"+ms.getReward()+"[]", skin));
            reward.setTransform(true);
            float coinScale = 0.4f;
            reward.setScale(0.6f / coinScale);
            reward.setOrigin(reward.getOriginX(), coin.getHeight()*coinScale*0.5f);
            reward.pack();

            HorizontalGroup hgroup = new HorizontalGroup();
            hgroup.setTransform(true);
            hgroup.setScale(coinScale);
            hgroup.addActor(coin);
            hgroup.addActor(reward);
            hgroup.space(20);
            activeMissionTable.add(hgroup).height(coin.getHeight() * coinScale).left().top();
        }

    }

    protected int getSilverJumps() {
        return 70;
    }

    protected int getGoldJumps() {
        return 150;
    }

    protected int getDiamondJumps() {
        return 250;
    }

    protected boolean backToPlayMenuWhenExit() {
        return false;
    }
}
