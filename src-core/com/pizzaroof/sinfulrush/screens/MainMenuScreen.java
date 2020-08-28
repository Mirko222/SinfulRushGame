package com.pizzaroof.sinfulrush.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.SpriterAnimActor;
import com.pizzaroof.sinfulrush.actors.basics.LoopableTextureActor;
import com.pizzaroof.sinfulrush.actors.basics.NGImageTextButton;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.HorizontalDecoration;
import com.pizzaroof.sinfulrush.language.LanguageManager;
import com.pizzaroof.sinfulrush.menus.MissionsMenu;
import com.pizzaroof.sinfulrush.menus.ShopMenu;
import com.pizzaroof.sinfulrush.screens.custom.CemeteryScreen;
import com.pizzaroof.sinfulrush.screens.custom.HellScreen;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin;
import com.pizzaroof.sinfulrush.util.assets.StatLabel;
import com.pizzaroof.sinfulrush.util.assets.VisualSelectBox;

import java.util.ArrayList;

/**schermata del menu dei livelli*/
public class MainMenuScreen extends AbstractScreen implements InputProcessor {

    public static final float DEF_ANIM_DUR = 0.2f;

    private TextButton hellButton, cemeteryButton, settingsCancel, settingsOk, comingSoon, shopButton;
    private ImageButton settingsButton, settingsClose, backButt, statsButton, missionsButton;
    private Group settingsGroup, levelsGroup, statsGroup;
    private Image blackBg, leftStick, rightStick, dialogBlackBg;
    private Container<Label> settingsLabel, musicLabel, sfxLabel, vibrationsLabel, screenshakeLabel, languageLabel;
    private Slider musicSlider, sfxSlider;
    private CheckBox vibrationsCheckbox, screenshakeCheckbox;
    private ImageTextButton playButton;

    private ShopMenu shopMenu;
    private MissionsMenu missionsMenu;

    private Button statBackBg;

    private Image hellDecoration, cemeteryDecoration;

    /**label per statistiche*/
    private com.pizzaroof.sinfulrush.util.assets.StatLabel timePlayed, enemiesKilled, friendsSaved, friendsKilled, platformsJumped, bestscoreLabel, numBossKilled;
    private ArrayList<com.pizzaroof.sinfulrush.util.assets.StatLabel> statLabels = new ArrayList<>();
    private static final int GLOBAL_STATS = 5;
    private TextButton cemeteryLocal, hellLocal;
    private Texture whitePixel;

    private VisualSelectBox<LanguageManager.Language> languageSelectBox;

    private LoopableTextureActor bg;

    private Image nameLogo;

    //nuvole animate...
    public static final float MIN_ASP_RATIO = 0.25f, MAX_ASP_RATIO = 0.35f, MIN_WIDTH = 150, MAX_WIDTH = 275, MIN_SPEED = 200, MAX_SPEED = 300, MIN_WAIT = 0.3f, MAX_WAIT = 2.5f;
    private float timeToWait, timeWaited;
    private TextureRegion cloudRegs[];
    private Group clouds;

    /**mettiamo il player che fa avanti e indietro*/
    private SpriterAnimActor player;

    /**dialog per chiedere conferma all'uscita*/
    private Dialog exitDialog;

    /**dialog per comunicare che il cimitero non è sbloccato (o che è stato appena sbloccato)*/
    private Dialog cemeteryDialog;

    private Dialog cemeteryUnlockedDialog;

    //private Dialog shopDialog;

    private boolean inRendering;

    /**@param first se vero, si comincia dal tasto play, altrimenti subito da quello dei livelli*/
    public MainMenuScreen(NGame game, boolean first, int exitsWithoutInterstitial) {
        super(game);
        inRendering = false;

        Gdx.input.setCatchBackKey(true);

        getStage().addActor(new Image()); //c'è qualche bug con stage sulle immagini del top layer... mettiamo una immagine fasulla prima della nostra immagine

        bg = new LoopableTextureActor(assetManager.get(com.pizzaroof.sinfulrush.Constants.MENU_BACKGROUND));
        bg.setWidth(com.pizzaroof.sinfulrush.Constants.VIRTUAL_WIDTH);
        bg.setHeight(com.pizzaroof.sinfulrush.Constants.VIRTUAL_HEIGHT);
        getStage().addActor(bg);

        clouds = new Group();
        getStage().addActor(clouds);

        com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin skin = assetManager.get(com.pizzaroof.sinfulrush.Constants.DEFAULT_SKIN_PATH);

        playButton = new NGImageTextButton(getLanguageManager().getText(LanguageManager.Text.PLAY), skin);
        playButton.setTransform(true);
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getSoundManager().click();
                playToLevelsMenu();
            }
        });
        playButton.setVisible(first);
        playButton.setOrigin(playButton.getWidth()*0.5f, playButton.getHeight()*0.5f);

        shopButton = new TextButton(getLanguageManager().getText(LanguageManager.Text.SHOP), skin, "blue");
        shopButton.setTransform(true);
        shopButton.setVisible(first);
        shopButton.setSize(playButton.getWidth(), playButton.getHeight());
        shopButton.setOrigin(shopButton.getWidth() * 0.5f, shopButton.getHeight()*0.5f);
        shopButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(playButton.hasActions()) return;
                getSoundManager().click();
                shopMenu.setVisible(true);
                //showDialog(shopDialog);
            }
        });

        statsButton = new ImageButton(skin, "stats");
        statsButton.setTransform(true);
        statsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getSoundManager().click();
                showStatsMenu();
            }
        });
        statsButton.setVisible(true);
        statsButton.setSize(100, 100);
        statsButton.setOrigin(statsButton.getWidth()*0.5f, statsButton.getHeight()*0.5f);

        missionsButton = new ImageButton(skin, "missions");
        missionsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getSoundManager().click();
                missionsMenu.setVisible(true);
            }
        });
        missionsButton.setVisible(true);
        missionsButton.setSize(100, 100);
        missionsButton.setOrigin(missionsButton.getWidth()*0.5f, missionsButton.getHeight()*0.5f);

        /*tutorialButton = new TextButton("Tutorial", skin);
        tutorialButton.setTransform(true);
        tutorialButton.setSize(playButton.getWidth(), playButton.getHeight());
        tutorialButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getSoundManager().click();
                tutorialButton.addAction(Actions.sequence(
                        Utils.disappearingAnimation(DEF_ANIM_DUR),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                takeTutorial(exitsWithoutInterstitial);
                            }
                        })
                ));
            }
        });
        tutorialButton.setVisible(first);
        tutorialButton.setOrigin(tutorialButton.getWidth()*0.5f, tutorialButton.getHeight()*0.5f);*/

        hellButton = new TextButton(com.pizzaroof.sinfulrush.Constants.HELL_NAME, skin, "hell_btn");
        hellButton.setTransform(true);
        hellButton.setWidth(515);
        hellButton.setOrigin(hellButton.getWidth()*0.5f, hellButton.getHeight()*0.5f);
        hellButton.addListener(new ChangeListener() { //aggiungi listener al tasto
            @Override
            public void changed(ChangeEvent event, Actor actor) { //cosa fare quando viene cliccato?
                //andiamo alla schermata di gameplay
                getSoundManager().click();
                hellButton.addAction(Actions.sequence(
                        com.pizzaroof.sinfulrush.util.Utils.disappearingAnimation(DEF_ANIM_DUR),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                ShopMenu.PlayerItem item = shopMenu.getSelectedItem();
                                goingToHellScreen(item.getPlayerPath());
                                setDestinationScreen(new HellScreen(game, item.getPlayerPath(), item.getCharmap(), item.getPlayerPowers(), exitsWithoutInterstitial));
                                //setDestinationScreen(new HellScreen(game, Constants.THIEF_DIRECTORY, -1, new PlayerPower(), exitsWithoutInterstitial));
                            }
                        })
                ));
                hellDecoration.addAction(com.pizzaroof.sinfulrush.util.Utils.disappearingAnimation(DEF_ANIM_DUR));
            }
        });
        hellDecoration = new Image(assetManager.get(com.pizzaroof.sinfulrush.Constants.CUSTOM_BUTTONS_DECORATIONS, TextureAtlas.class).findRegion("hell"));
        hellDecoration.setSize(417, 124);
        hellDecoration.setOrigin(hellDecoration.getWidth() * 0.5f, - hellButton.getHeight()*0.5f);

        boolean cemUnlocked = com.pizzaroof.sinfulrush.util.Utils.isCemeteryUnlocked(getPreferences());
        cemeteryButton = new TextButton(com.pizzaroof.sinfulrush.Constants.CEMETERY_NAME, skin, cemUnlocked ? "cemetery_btn" : "def_locked");
        cemeteryButton.setTransform(true);
        cemeteryButton.setWidth(515);
        cemeteryButton.setOrigin(cemeteryButton.getWidth()*0.5f, cemeteryButton.getHeight()*0.5f);
        cemeteryButton.addListener(new ChangeListener() { //aggiungi listener al tasto
            @Override
            public void changed(ChangeEvent event, Actor actor) { //cosa fare quando viene cliccato?
                //andiamo alla schermata di gameplay
                getSoundManager().click();
                if(cemUnlocked) {
                    cemeteryButton.addAction(Actions.sequence(
                            com.pizzaroof.sinfulrush.util.Utils.disappearingAnimation(DEF_ANIM_DUR),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    ShopMenu.PlayerItem item = shopMenu.getSelectedItem();
                                    goingToCemeteryScreen(item.getPlayerPath());
                                    setDestinationScreen(new CemeteryScreen(game, item.getPlayerPath(), item.getCharmap(), item.getPlayerPowers(), exitsWithoutInterstitial));
                                    //setDestinationScreen(new CemeteryScreen(game, Constants.THIEF_DIRECTORY, -1, new PlayerPower(), exitsWithoutInterstitial));
                                }
                            })
                    ));
                    cemeteryDecoration.addAction(com.pizzaroof.sinfulrush.util.Utils.disappearingAnimation(DEF_ANIM_DUR));
                }
                else
                    showDialog(cemeteryDialog);
            }
        });
        cemeteryDecoration = new Image(assetManager.get(com.pizzaroof.sinfulrush.Constants.CUSTOM_BUTTONS_DECORATIONS, TextureAtlas.class).findRegion(cemUnlocked ? "cemetery" : "cemetery_black"));
        cemeteryDecoration.setSize(448, 124);
        cemeteryDecoration.setOrigin(cemeteryDecoration.getWidth() * 0.5f, - cemeteryButton.getHeight()*0.5f);
        //cemeteryButton.setDisabled(!isCemeteryUnlocked());

        comingSoon = new TextButton("Coming soon...", skin, "def_locked");
        comingSoon.setTransform(true);
        comingSoon.setWidth(515);
        comingSoon.setOrigin(comingSoon.getWidth()*0.5f, comingSoon.getHeight()*0.5f);
        comingSoon.setTouchable(Touchable.disabled);
        comingSoon.setDisabled(true);

        settingsButton = new ImageButton(skin, "settings");
        settingsButton.setSize(100, 100);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSettingsMenu();
                getSoundManager().click();
                setLevelLayerTouchable(Touchable.disabled);
            }
        });

        backButt = new ImageButton(skin, "back");
        backButt.setSize(100, 100);
        backButt.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(playButton.hasActions()) return;
                getSoundManager().click();
                levelsToPlayMenu();
            }
        });


        settingsGroup = new Group();
        settingsGroup.setVisible(false);
        levelsGroup = new Group();
        levelsGroup.setVisible(!first);
        levelsGroup.addActor(hellDecoration);
        levelsGroup.addActor(hellButton);
        levelsGroup.addActor(cemeteryDecoration);
        levelsGroup.addActor(cemeteryButton);
        levelsGroup.addActor(comingSoon);
        levelsGroup.addActor(backButt);

        shopMenu = new ShopMenu(game, assetManager, getSoundManager(), getLanguageManager());
        shopMenu.setVisible(false);
        missionsMenu = new MissionsMenu(game, assetManager, getSoundManager(), game.getActiveMissions(), game.getCompletedMissions());
        missionsMenu.setVisible(false);
        shopMenu.setMissionsMenu(missionsMenu);

        nameLogo = new Image(assetManager.get(com.pizzaroof.sinfulrush.Constants.NAME_TEXTURE, Texture.class));
        nameLogo.setWidth(704); nameLogo.setHeight(511);
        getStage().addActor(nameLogo);
        createPlayer();

        getStage().addActor(settingsButton);
        getStage().addActor(missionsButton);
        getStage().addActor(playButton);
        getStage().addActor(shopButton);
        //getStage().addActor(tutorialButton);
        getStage().addActor(statsButton);
        getStage().addActor(levelsGroup);
        getStage().addActor(settingsGroup);
        getStage().addActor(shopMenu);
        getStage().addActor(missionsMenu);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.8f);
        pixmap.fill();
        blackBg = new Image(new Texture(pixmap));
        pixmap.setColor(1, 1, 1, 0.3f);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.setColor(0, 0, 0, 0.5f);
        pixmap.fill();
        dialogBlackBg = new Image(new Texture(pixmap));
        pixmap.dispose();

        createSettingsLayer(skin);

        getSoundManager().playSoundtrack(com.pizzaroof.sinfulrush.Constants.MENU_SOUNDTRACK);

        cloudRegs = new TextureRegion[2];
        cloudRegs[0] = assetManager.get(com.pizzaroof.sinfulrush.Constants.HELL_DECORATIONS_BG, TextureAtlas.class).findRegion("nuvola1");
        cloudRegs[1] = assetManager.get(com.pizzaroof.sinfulrush.Constants.HELL_DECORATIONS_BG, TextureAtlas.class).findRegion("nuvola2");
        timeToWait = 0;
        timeWaited = 0;

        exitDialog = new Dialog("", skin) {
            @Override
            public void result(Object obj) {
                if(obj.equals(Boolean.TRUE)) //ha confermato che vuole uscire
                    Gdx.app.exit();
                else {
                    getSoundManager().click();
                    dialogBlackBg.remove();
                }
            }
        };
        exitDialog.text(getLanguageManager().getText(LanguageManager.Text.EXIT_CONFIRMATION), skin.get("darker", Label.LabelStyle.class));
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

        if(!com.pizzaroof.sinfulrush.util.Utils.isCemeteryUnlocked(getPreferences())) {
            cemeteryDialog = new Dialog("", skin) {
                @Override
                public void result(Object obj) {
                    getSoundManager().click();
                    dialogBlackBg.remove();
                }
            };
            cemeteryDialog.text(getLanguageManager().getText(LanguageManager.Text.HOW_UNLOCK_CEMETERY));
            cemeteryDialog.button("  Ok  ");
            cemeteryDialog.setMovable(false);
            cemeteryDialog.setModal(true);
            cemeteryDialog.getContentTable().padBottom(50).padTop(60).padLeft(55).padRight(55);
            cemeteryDialog.getButtonTable().padBottom(45);
        }

        /*shopDialog = new Dialog("", skin) {
            @Override
            public void result(Object res) {
                getSoundManager().click();
                dialogBlackBg.remove();
            }
        };
        shopDialog.getContentTable().padBottom(50).padTop(60).padLeft(55).padRight(55);
        shopDialog.getButtonTable().padBottom(45);
        shopDialog.setMovable(false);
        shopDialog.setModal(true);
        shopDialog.text(getLanguageManager().getText(com.pizzaroof.sinfulrush.language.LanguageManager.Text.SHOP_DIALOG));
        shopDialog.button("  Ok  ");*/

        createStatsLayer(skin);

        createMinorDialogs(skin, exitsWithoutInterstitial);

        recomputeWidgetLanguages();
    }

    /*private void takeTutorial(int exitsWithoutInterstitial) {
        goingToTutorialScreen();
        setDestinationScreen(new TutorialScreen(game, exitsWithoutInterstitial));
    }*/

    @Override
    public void hide() {
        super.hide();
        dispose();
    }

    private void setLevelLayerTouchable(Touchable touchable) {
        hellButton.setTouchable(touchable);
        cemeteryButton.setTouchable(touchable);
        settingsButton.setTouchable(touchable);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        recomputeUiPositions();
    }

    private void recomputeUiPositions() {
        float w = getStage().getCamera().viewportWidth, h = getStage().getCamera().viewportHeight;

        shopMenu.updateLanguages(w, h, getLanguageManager(), true);
        missionsMenu.updateLanguages(w, h, getLanguageManager(), true);

        bg.setMinX(0);
        bg.setMaxX(w);
        bg.setOffsetX(w * 0.5f - bg.getWidth() * 0.5f);

        //tutorialButton.setPosition(w * 0.5f - tutorialButton.getWidth() * 0.5f, h * 0.205f);
        shopButton.setPosition(w * 0.5f - shopButton.getWidth() * 0.5f, h * 0.205f);
        playButton.setPosition(w * 0.5f - playButton.getWidth() * 0.5f, shopButton.getY() + shopButton.getHeight() + playButton.getHeight() * 0.2f);

        //statsButton.setPosition(w * 0.5f - statsButton.getWidth() * 0.5f, tutorialButton.getY() + tutorialButton.getHeight() + statsButton.getHeight()*0.2f);


        settingsButton.setPosition(w - settingsButton.getWidth() - 43,h - 120);
        statsButton.setPosition(settingsButton.getX() - statsButton.getWidth() - 43, settingsButton.getY());
        missionsButton.setPosition(statsButton.getX() - missionsButton.getWidth() - 43, settingsButton.getY());
        cemeteryButton.setPosition( w * 0.5f - cemeteryButton.getWidth() * 0.5f, h * 0.5f - cemeteryButton.getHeight() * 0.5f);
        cemeteryDecoration.setPosition(w*0.5f - cemeteryDecoration.getWidth()*0.5f, cemeteryButton.getY() + cemeteryButton.getHeight() - 3);
        hellButton.setPosition(w * 0.5f - hellButton.getWidth() * 0.5f, cemeteryButton.getY() + cemeteryButton.getHeight() + 200);
        hellDecoration.setPosition(w * 0.5f - hellDecoration.getWidth()*0.5f, hellButton.getY() + hellButton.getHeight() - 3);
        comingSoon.setPosition(w * 0.5f - comingSoon.getWidth() * 0.5f, cemeteryButton.getY() - comingSoon.getHeight() - 200);
        backButt.setPosition(w - settingsButton.getWidth() - settingsButton.getX(), settingsButton.getY());

        blackBg.setSize(w, h);
        dialogBlackBg.setSize(w, h);
        settingsClose.setPosition(settingsButton.getX(), settingsButton.getY());
        float setlabW = settingsLabel.getActor().getWidth() * settingsLabel.getScaleX();
        settingsLabel.setPosition(w * 0.5f - setlabW*0.5f,h * 0.85f - settingsLabel.getActor().getHeight()*0.5f);
        float stickSpace = 30;
        rightStick.setWidth(Math.min(296, w * 0.95f - settingsLabel.getX() - setlabW - stickSpace));
        leftStick.setWidth(-rightStick.getWidth());
        leftStick.setPosition(w * 0.05f + rightStick.getWidth(), settingsLabel.getY() + rightStick.getHeight());
        rightStick.setPosition(w * 0.95f - rightStick.getWidth(), leftStick.getY());


        float space = 50;
        float len = settingsCancel.getWidth() + settingsOk.getWidth() + space;
        settingsCancel.setPosition(w * 0.5f - len * 0.5f, h * 0.08f);
        settingsOk.setPosition(settingsCancel.getX() + settingsCancel.getWidth() + space, settingsCancel.getY());

        languageLabel.setPosition(w * 0.1f, h * 0.65f - languageLabel.getActor().getHeight()*0.5f);
        languageSelectBox.setPosition(w * 0.9f - languageSelectBox.getWidth(), languageLabel.getY());
        musicLabel.setPosition(w * 0.5f - musicLabel.getActor().getWidth()*0.5f, languageSelectBox.getY() - musicLabel.getActor().getHeight()*1.5f - 30); //h*0.65f
        musicSlider.setWidth(w * 0.8f);
        musicSlider.setPosition(w * 0.5f - musicSlider.getWidth() * 0.5f, musicLabel.getY() - musicSlider.getHeight() * 2f + musicLabel.getActor().getHeight()*0.5f);
        sfxLabel.setPosition(w * 0.5f - sfxLabel.getActor().getWidth()*0.5f, musicSlider.getY() - sfxLabel.getActor().getHeight()*1.5f - 10);
        sfxSlider.setWidth(w * 0.8f);
        sfxSlider.setPosition(w * 0.5f - sfxSlider.getWidth() * 0.5f, sfxLabel.getY() - sfxSlider.getHeight() * 2f + sfxLabel.getActor().getHeight()*0.5f);

        vibrationsLabel.setPosition(sfxSlider.getX(), sfxSlider.getY() - vibrationsLabel.getActor().getHeight() * 2f);
        screenshakeLabel.setPosition(sfxSlider.getX() + screenshakeLabel.getActor().getWidth() * 0.5f, vibrationsLabel.getY() - screenshakeLabel.getActor().getHeight() * 1.8f + vibrationsLabel.getActor().getHeight()*0.5f);
        vibrationsCheckbox.setPosition(sfxSlider.getX() + sfxSlider.getWidth() - vibrationsCheckbox.getWidth(), vibrationsLabel.getY());
        screenshakeCheckbox.setPosition(sfxSlider.getX() + sfxSlider.getWidth() - screenshakeCheckbox.getWidth(),
                screenshakeLabel.getY() - screenshakeLabel.getActor().getHeight() * 0.5f);

        nameLogo.setPosition(w * 0.5f - nameLogo.getWidth()*0.5f, h*0.765f - nameLogo.getHeight()*0.5f);

        repositionStatsLayer(w, h);

        if(cemeteryUnlockedDialog != null && getPreferences().getBoolean(com.pizzaroof.sinfulrush.Constants.CEMETERY_UNLOCKED_DIALOG_PREF, true)) {
            showDialog(cemeteryUnlockedDialog);
        }
    }

    @Override
    protected void redraw() {
        if(stage != null) {
            Gdx.gl.glClearColor(118.f / 255.f, 196.f / 255.f, 232.f / 255.f, 1); //pulisci schermo con celestino
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            stage.getViewport().apply(); //usa viewport di stage
            stage.draw(); //ristampa stage
        }
    }

    @Override
    public void updateLogic(float delta) {
        inRendering = true;
        super.updateLogic(delta);
        if(stage != null) {
            createClouds(delta);
            nameLogo.setVisible(playButton.isVisible());
            nameLogo.getColor().a = playButton.getColor().a;
        }
        if(shopMenu != null && shopMenu.isVisible())
            shopMenu.updateLogic();
    }

    protected void initStage() {
        stage = new Stage();
        stage.setViewport(new ExtendViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT));
        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(this);
        mux.addProcessor(stage);
        Gdx.input.setInputProcessor(mux); //lasciamo allo stage gestire gli input
    }

    private void createSettingsLayer(Skin skin) {

        //close-ok-cancel
        settingsClose = new ImageButton(skin, "close");
        settingsClose.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //usiamo lo stesso bottone sia per settings che per stats
                if(settingsGroup.isVisible()) {
                    discardSettings();
                } else if(statsGroup.isVisible()) {
                    hideStatsMenu();
                }
                getSoundManager().click();
            }
        });
        settingsClose.setSize(settingsButton.getWidth(), settingsButton.getHeight());

        settingsCancel = new TextButton("Cancel", skin);
        settingsCancel.setTransform(true);
        settingsCancel.setOrigin(settingsCancel.getWidth()*0.5f, settingsCancel.getHeight()*0.5f);
        settingsCancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getSoundManager().click();
                settingsCancel.addAction(com.pizzaroof.sinfulrush.util.Utils.clickAction(new Runnable() {
                    @Override
                    public void run() {
                        discardSettings();
                    }
                }, DEF_ANIM_DUR));
            }
        });
        settingsOk = new TextButton("Ok", skin);
        settingsOk.setTransform(true);
        settingsOk.setSize(settingsCancel.getWidth(), settingsCancel.getHeight());
        settingsOk.setOrigin(settingsOk.getWidth()*0.5f, settingsOk.getHeight()*0.5f);
        settingsOk.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getSoundManager().click();
                settingsOk.addAction(com.pizzaroof.sinfulrush.util.Utils.clickAction(new Runnable() {
                    @Override
                    public void run() {
                        settingsGroup.setVisible(false);
                        setLevelLayerTouchable(Touchable.enabled);
                        getPreferences().putFloat(com.pizzaroof.sinfulrush.Constants.MUSIC_VOLUME_PREFS, musicSlider.getValue());
                        getPreferences().putFloat(com.pizzaroof.sinfulrush.Constants.SFX_VOLUME_PREFS, sfxSlider.getValue());
                        getPreferences().putBoolean(com.pizzaroof.sinfulrush.Constants.VIBRATIONS_PREFS, vibrationsCheckbox.isChecked());
                        getPreferences().putBoolean(com.pizzaroof.sinfulrush.Constants.SCREENSHAKE_PREFS, screenshakeCheckbox.isChecked());
                        getPreferences().putString(com.pizzaroof.sinfulrush.Constants.LANGUAGE_PREFS, languageSelectBox.getSelected().toString());
                        getPreferences().flush();

                        getLanguageManager().setLanguage(languageSelectBox.getSelected());

                        recomputeWidgetLanguages();
                    }
                }, DEF_ANIM_DUR));
            }
        });

        //settings label
        settingsLabel = new Container<>(new Label("", skin));
        settingsLabel.setTransform(true);
        settingsLabel.setScale(1.5f);
        leftStick = new Image(assetManager.get(com.pizzaroof.sinfulrush.Constants.DEFAULT_SKIN_ATLAS, TextureAtlas.class).findRegion("bar"));
        leftStick.setWidth(-296);
        leftStick.setHeight(30);
        rightStick = new Image(leftStick.getDrawable());
        rightStick.setWidth(-leftStick.getWidth());
        rightStick.setHeight(leftStick.getHeight());

        //settings veri e propri
        musicLabel = new Container<>(new Label("", skin));
        musicLabel.setTransform(true);
        sfxLabel = new Container<>(new Label("", skin));
        sfxLabel.setTransform(true);

        musicSlider = new Slider(0.f, 1.f, 0.01f, false, skin);
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                musicLabel.getActor().setText(getLanguageManager().getText(LanguageManager.Text.SETTINGS_MUSIC_VOL)+getPercentage(musicSlider.getValue()));
                getSoundManager().setMusicVolume(musicSlider.getValue());
            }
        });
        sfxSlider = new Slider(0.f, 1.f, 0.01f, false, skin);
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sfxLabel.getActor().setText(getLanguageManager().getText(LanguageManager.Text.SETTINGS_SFX_VOL)+getPercentage(sfxSlider.getValue()));
                getSoundManager().setSfxVolume(sfxSlider.getValue());
            }
        });

        vibrationsLabel = new Container<>(new Label("Vibrations:", skin));
        screenshakeLabel = new Container<>(new Label("Screenshake:", skin));
        vibrationsCheckbox = new CheckBox("", skin);
        //vibrationsCheckbox.getClickListener().setTapSquareSize(74);
        vibrationsCheckbox.setChecked(getPreferences().getBoolean(com.pizzaroof.sinfulrush.Constants.VIBRATIONS_PREFS, true));
        vibrationsCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getSoundManager().click();
            }
        });
        screenshakeCheckbox = new CheckBox("", skin);
        //screenshakeCheckbox.getClickListener().setTapSquareSize(74);
        screenshakeCheckbox.setChecked(getPreferences().getBoolean(com.pizzaroof.sinfulrush.Constants.SCREENSHAKE_PREFS, true));
        screenshakeCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getSoundManager().click();
            }
        });

        languageLabel = new Container<>(new Label("Language: ", skin));
        languageSelectBox = new VisualSelectBox<LanguageManager.Language>(skin) {
            @Override
            public String toString(LanguageManager.Language item) {
                switch (item) {
                    case EN: return "     English";
                    case IT: return "    Italiano";
                    case RO: return "   Română";
                    case MG: return " Malagasy";
                }
                return null;
            }
        };
        languageSelectBox.setItems(LanguageManager.Language.EN, LanguageManager.Language.IT, LanguageManager.Language.MG, LanguageManager.Language.RO);
        languageSelectBox.setMaxListCount(4);
        languageSelectBox.setWidth(320); //375
        languageSelectBox.setSelected(getLanguageManager().getActualLanguage());
        TextureAtlas atlas = assetManager.get(com.pizzaroof.sinfulrush.Constants.DEFAULT_SKIN_ATLAS, TextureAtlas.class);
        languageSelectBox.addImage(LanguageManager.Language.EN, atlas.findRegion("english"), 70, 56);
        languageSelectBox.addImage(LanguageManager.Language.IT, atlas.findRegion("italian"), 70, 56);
        languageSelectBox.addImage(LanguageManager.Language.RO, atlas.findRegion("romanian"), 70, 56);
        languageSelectBox.addImage(LanguageManager.Language.MG, atlas.findRegion("malagasy"), 70, 56);
        languageSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getLanguageManager().setLanguage(languageSelectBox.getSelected());
                recomputeRealTimeWidgetLanguages();
            }
        });


        settingsGroup.addActor(blackBg);
        settingsGroup.addActor(settingsClose);
        settingsGroup.addActor(settingsCancel);
        settingsGroup.addActor(settingsOk);
        settingsGroup.addActor(settingsLabel);
        settingsGroup.addActor(leftStick);
        settingsGroup.addActor(rightStick);
        settingsGroup.addActor(musicLabel);
        settingsGroup.addActor(musicSlider);
        settingsGroup.addActor(sfxLabel);
        settingsGroup.addActor(sfxSlider);
        settingsGroup.addActor(vibrationsLabel);
        settingsGroup.addActor(screenshakeLabel);
        settingsGroup.addActor(vibrationsCheckbox);
        settingsGroup.addActor(screenshakeCheckbox);
        settingsGroup.addActor(languageLabel);
        settingsGroup.addActor(languageSelectBox);
    }

    private void discardSettings() {
        setLevelLayerTouchable(Touchable.enabled);
        languageSelectBox.setSelected(getLanguageManager().getActualLanguage());
        getSoundManager().setMusicVolume(getPreferences().getFloat(com.pizzaroof.sinfulrush.Constants.MUSIC_VOLUME_PREFS, com.pizzaroof.sinfulrush.Constants.MUSIC_VOLUME_DEF));
        getSoundManager().setSfxVolume(getPreferences().getFloat(com.pizzaroof.sinfulrush.Constants.SFX_VOLUME_PREFS, com.pizzaroof.sinfulrush.Constants.SFX_VOLUME_DEF));

        LanguageManager.Language original = LanguageManager.Language.valueOf(getPreferences().getString(com.pizzaroof.sinfulrush.Constants.LANGUAGE_PREFS));
        getLanguageManager().setLanguage(original);
        languageSelectBox.setSelected(original);
        recomputeWidgetLanguages();

        settingsGroup.setVisible(false);
        languageSelectBox.hideList();
    }

    /**da valore in [0, 1] dà percentuale stampabile*/
    private String getPercentage(float val) {
        String s = Math.round(val * 100)+"%";
        while(s.length() != 4) s += " ";
        return s;
    }

    /**transizione da play a menu livelli*/
    private void playToLevelsMenu() {
        playButton.setTouchable(Touchable.disabled);
        shopButton.setTouchable(Touchable.disabled);
        //tutorialButton.setTouchable(Touchable.disabled);
        hellButton.setTouchable(Touchable.enabled);
        cemeteryButton.setTouchable(Touchable.enabled);
        levelsGroup.setVisible(true);

        playButton.clearActions();
        playButton.addAction(com.pizzaroof.sinfulrush.util.Utils.disappearingAnimation(DEF_ANIM_DUR));
        //tutorialButton.clearActions();
        //tutorialButton.addAction(Utils.disappearingAnimation(DEF_ANIM_DUR));
        shopButton.clearActions();
        shopButton.addAction(com.pizzaroof.sinfulrush.util.Utils.disappearingAnimation(DEF_ANIM_DUR));

        hellButton.clearActions();
        hellDecoration.clearActions();
        cemeteryButton.clearActions();
        cemeteryDecoration.clearActions();
        comingSoon.clearActions();
        cemeteryButton.setScale(1.2f); cemeteryButton.getColor().a = 0;
        cemeteryDecoration.setScale(1.2f); cemeteryDecoration.getColor().a = 0;
        comingSoon.setScale(1.2f); comingSoon.getColor().a = 0;
        hellButton.setScale(1.2f); hellButton.getColor().a = 0;
        hellDecoration.setScale(1.2f); hellDecoration.getColor().a = 0;
        backButt.setScale(1.2f); backButt.getColor().a = 0;

        hellButton.addAction(com.pizzaroof.sinfulrush.util.Utils.appearAnimation(DEF_ANIM_DUR));
        hellDecoration.addAction(com.pizzaroof.sinfulrush.util.Utils.appearAnimation(DEF_ANIM_DUR));
        cemeteryButton.addAction(com.pizzaroof.sinfulrush.util.Utils.appearAnimation(DEF_ANIM_DUR));
        cemeteryDecoration.addAction(com.pizzaroof.sinfulrush.util.Utils.appearAnimation(DEF_ANIM_DUR));
        comingSoon.addAction(com.pizzaroof.sinfulrush.util.Utils.appearAnimation(DEF_ANIM_DUR));
        backButt.addAction(com.pizzaroof.sinfulrush.util.Utils.appearAnimation(DEF_ANIM_DUR));
    }

    /**transizione da menu livelli a play*/
    private void levelsToPlayMenu() {
        playButton.clearActions();
        shopButton.clearActions();
        //tutorialButton.clearActions();
        hellButton.clearActions();
        hellDecoration.clearActions();
        cemeteryButton.clearActions();
        cemeteryDecoration.clearActions();
        comingSoon.clearActions();
        shopButton.clearActions();

        playButton.setTouchable(Touchable.enabled);
        shopButton.setTouchable(Touchable.enabled);
        //tutorialButton.setTouchable(Touchable.enabled);
        hellButton.setTouchable(Touchable.disabled);
        cemeteryButton.setTouchable(Touchable.disabled);

        hellButton.addAction(com.pizzaroof.sinfulrush.util.Utils.disappearingAnimation(DEF_ANIM_DUR));
        hellDecoration.addAction(com.pizzaroof.sinfulrush.util.Utils.disappearingAnimation(DEF_ANIM_DUR));
        comingSoon.addAction(com.pizzaroof.sinfulrush.util.Utils.disappearingAnimation(DEF_ANIM_DUR));
        backButt.addAction(com.pizzaroof.sinfulrush.util.Utils.disappearingAnimation(DEF_ANIM_DUR));
        cemeteryDecoration.addAction(com.pizzaroof.sinfulrush.util.Utils.disappearingAnimation(DEF_ANIM_DUR));
        cemeteryButton.addAction(Actions.sequence(com.pizzaroof.sinfulrush.util.Utils.disappearingAnimation(DEF_ANIM_DUR),
                                                Actions.run(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        levelsGroup.setVisible(false);
                                                    }
                                                })));

        playButton.getColor().a = 0;
        playButton.setScale(1.2f);
        playButton.addAction(com.pizzaroof.sinfulrush.util.Utils.appearAnimation(DEF_ANIM_DUR));
        /*tutorialButton.getColor().a = 0;
        tutorialButton.setScale(1.2f);
        tutorialButton.addAction(Utils.appearAnimation(DEF_ANIM_DUR));*/
        shopButton.getColor().a = 0;
        shopButton.setScale(1.2f);
        shopButton.addAction(com.pizzaroof.sinfulrush.util.Utils.appearAnimation(DEF_ANIM_DUR));
    }

    private void createClouds(float delta) {
        timeWaited += delta;
        if(timeWaited >= timeToWait) {
            createRandomCloud();
            timeWaited = 0;
            timeToWait = com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_WAIT, MAX_WAIT);
        }
    }

    private void createPlayer() {
        player = new SpriterAnimActor() {
            boolean moving = com.pizzaroof.sinfulrush.util.Utils.randBool(); //true: inizia fermo, false: inizia correndo

            @Override
            public void act(float delta) {
                super.act(delta);
                setPosition(getX(), getY()); //altrimenti non aggiorna azioni
                if(!hasActions()) {
                    float w = getStage().getCamera().viewportWidth;
                    if(!moving) { //alterniamo movimenti a punti fermi
                        float destX = com.pizzaroof.sinfulrush.util.Utils.randFloat(getDrawingWidth()*0.5f, w-getDrawingWidth()*0.5f);
                        if(Math.abs(destX - getX()) < com.pizzaroof.sinfulrush.Constants.PIXELS_PER_METER*0.75f) { // si muove troppo poco
                            float mul = getX() < w * 0.5f ? 1 : -1;
                            destX = getX() + mul * com.pizzaroof.sinfulrush.Constants.PIXELS_PER_METER * 0.75f;
                        }
                        addAction(Actions.moveTo(destX, getY(), Math.abs(getX() - destX) / com.pizzaroof.sinfulrush.Constants.PIXELS_PER_METER));
                        setHorDirection(getX() < destX ? HorDirection.RIGHT : HorDirection.LEFT);
                        setSpriterAnimation(1);
                    } else {
                        addAction(Actions.delay(Utils.randFloat(1.f, 2.f)));
                        setSpriterAnimation(0);
                    }
                    moving = !moving;
                }
            }
        };

        ShopMenu.PlayerItem item = shopMenu.getSelectedItem();
        player.setSpriterData(assetManager.get(Utils.playerScmlPath(item.getPlayerPath())), getStage().getBatch());
        //player.setSpriterData(assetManager.get(Utils.playerScmlPath(Constants.THIEF_DIRECTORY)), getStage().getBatch());

        player.addSpriterAnimation(1, 1.f, Animation.PlayMode.LOOP);
        player.addSpriterAnimation(0, 1.f, Animation.PlayMode.LOOP);
        player.setSpriterAnimation(1);
        player.setOriginalWidth(540);

        player.setDrawingWidth(180);
        player.setDrawingHeight(180);
        player.setPosition(Utils.randFloat(player.getDrawingWidth()*0.5f, com.pizzaroof.sinfulrush.Constants.VIRTUAL_WIDTH - player.getDrawingWidth()*0.5f), 85);
        if(Utils.randBool()) player.setHorDirection(SpriteActor.HorDirection.LEFT);

        //player.getSpriterPlayer().setCharacterMaps(-1);
        player.getSpriterPlayer().setCharacterMaps(item.getCharmap());

        getStage().addActor(player);
    }

    private void createRandomCloud() {
        boolean dir = com.pizzaroof.sinfulrush.util.Utils.randBool();
        HorizontalDecoration cloud = new HorizontalDecoration(cloudRegs[com.pizzaroof.sinfulrush.util.Utils.randInt(0, 1)], false, (dir ? 1 : -1) * com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_SPEED, MAX_SPEED));
        cloud.setWidth(com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_WIDTH, MAX_WIDTH));
        cloud.setHeight(cloud.getWidth() * com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_ASP_RATIO, MAX_ASP_RATIO));
        cloud.setY(com.pizzaroof.sinfulrush.util.Utils.randFloat(330, getStage().getCamera().viewportHeight - cloud.getHeight()));
        cloud.setX(dir ? -cloud.getWidth() : getStage().getCamera().viewportWidth);
        cloud.flip(com.pizzaroof.sinfulrush.util.Utils.randBool());
        clouds.addActor(cloud);
    }

    /**ricalcola lingue di tutti i widget*/
    private void recomputeWidgetLanguages() {
        com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin skin = assetManager.get(com.pizzaroof.sinfulrush.Constants.DEFAULT_SKIN_PATH);
        if(cemeteryDialog != null) {
            cemeteryDialog.getContentTable().clear();
            cemeteryDialog.text(getLanguageManager().getText(LanguageManager.Text.HOW_UNLOCK_CEMETERY), skin.get("darker", Label.LabelStyle.class));
            ((Label)cemeteryDialog.getContentTable().getCells().first().getActor()).setAlignment(Align.center); //align(Align.center);
        }
        exitDialog.getContentTable().clear();
        ((TextButton)exitDialog.getButtonTable().getCells().first().getActor()).setText(getLanguageManager().getText(LanguageManager.Text.YES));
        ((TextButton)exitDialog.getButtonTable().getCells().get(1).getActor()).setText(getLanguageManager().getText(LanguageManager.Text.NO));
        exitDialog.text(getLanguageManager().getText(LanguageManager.Text.EXIT_CONFIRMATION), skin.get("darker", Label.LabelStyle.class));

        //shopDialog.getContentTable().clear();
        //shopDialog.text(getLanguageManager().getText(com.pizzaroof.sinfulrush.language.LanguageManager.Text.SHOP_DIALOG), skin.get("darker", Label.LabelStyle.class));

        recomputeRealTimeWidgetLanguages();
    }

    /**ricalcola lingue che devono essere modificate subito (hanno effetti visivi immediati)*/
    private void recomputeRealTimeWidgetLanguages() {
        playButton.setText(getLanguageManager().getText(LanguageManager.Text.PLAY));
        shopButton.setText(getLanguageManager().getText(LanguageManager.Text.SHOP));

        languageLabel.getActor().setText(getLanguageManager().getText(LanguageManager.Text.SETTINGS_LANGUAGE));
        languageLabel.pack();
        sfxLabel.getActor().setText(getLanguageManager().getText(LanguageManager.Text.SETTINGS_SFX_VOL));
        sfxLabel.pack();
        musicLabel.getActor().setText(getLanguageManager().getText(LanguageManager.Text.SETTINGS_MUSIC_VOL)+getPercentage(musicSlider.getValue()));
        musicLabel.pack();
        sfxLabel.getActor().setText(getLanguageManager().getText(LanguageManager.Text.SETTINGS_SFX_VOL)+getPercentage(sfxSlider.getValue()));
        sfxLabel.pack();
        vibrationsLabel.getActor().setText(getLanguageManager().getText(LanguageManager.Text.SETTINGS_VIBRATIONS));
        vibrationsLabel.pack();

        settingsCancel.setText(getLanguageManager().getText(LanguageManager.Text.SETTINGS_CANCEL));

        settingsLabel.getActor().setText(getLanguageManager().getText(LanguageManager.Text.SETTINGS_TITLE));
        settingsLabel.pack();


        recomputeUiPositions();
    }

    /**chiamata quando si sta andando allo scenario dell'inferno*/
    protected void goingToHellScreen(String playerDir) {
    }

    /**chiamata quando si sta andando allo scenario del cimitero*/
    protected void goingToCemeteryScreen(String playerDir) {
    }

    /**layer con statistiche*/
    protected void createStatsLayer(Skin skin) {
        statsGroup = new Group();

        timePlayed = new com.pizzaroof.sinfulrush.util.assets.StatLabel(skin, whitePixel);
        enemiesKilled = new com.pizzaroof.sinfulrush.util.assets.StatLabel(skin, whitePixel);
        friendsSaved = new com.pizzaroof.sinfulrush.util.assets.StatLabel(skin, whitePixel);
        friendsKilled = new com.pizzaroof.sinfulrush.util.assets.StatLabel(skin, whitePixel);
        platformsJumped = new com.pizzaroof.sinfulrush.util.assets.StatLabel(skin, null);
        bestscoreLabel = new com.pizzaroof.sinfulrush.util.assets.StatLabel(skin, whitePixel);
        numBossKilled = new com.pizzaroof.sinfulrush.util.assets.StatLabel(skin, null);

        statLabels.add(timePlayed);
        statLabels.add(enemiesKilled);
        statLabels.add(friendsKilled);
        statLabels.add(friendsSaved);
        statLabels.add(platformsJumped);
        statLabels.add(bestscoreLabel);
        statLabels.add(numBossKilled);

        hellLocal = new TextButton(com.pizzaroof.sinfulrush.Constants.HELL_NAME, skin, "toggle");
        cemeteryLocal = new TextButton(com.pizzaroof.sinfulrush.Constants.CEMETERY_NAME, skin, "toggle");
        cemeteryLocal.setTransform(true);
        cemeteryLocal.setOrigin(cemeteryLocal.getWidth()*0.5f, cemeteryLocal.getHeight()*0.5f);
        cemeteryLocal.scaleBy(-0.2f);
        cemeteryLocal.setWidth(530);
        hellLocal.setTransform(true);
        hellLocal.setSize(cemeteryLocal.getWidth(), cemeteryLocal.getHeight());
        hellLocal.setOrigin(hellLocal.getWidth()*0.5f, hellLocal.getHeight()*0.5f);
        hellLocal.scaleBy(-0.2f);
        hellLocal.setChecked(true);
        hellLocal.setTouchable(Touchable.disabled);
        cemeteryLocal.setChecked(false);
        hellLocal.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(hellLocal.isChecked()) {
                    getSoundManager().click();
                    cemeteryLocal.setChecked(false);
                    hellLocal.setTouchable(Touchable.disabled);
                    cemeteryLocal.setTouchable(Touchable.enabled);
                    recomputeLocalStats(getStage().getCamera().viewportWidth, getStage().getCamera().viewportHeight);
                }
            }
        });
        cemeteryLocal.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(cemeteryLocal.isChecked()) {
                    getSoundManager().click();
                    hellLocal.setChecked(false);
                    cemeteryLocal.setTouchable(Touchable.disabled);
                    hellLocal.setTouchable(Touchable.enabled);
                    recomputeLocalStats(getStage().getCamera().viewportWidth, getStage().getCamera().viewportHeight);
                }
            }
        });

        statBackBg = new Button(skin);
        statBackBg.setSize(900, 170);

        statsGroup.addActor(statBackBg);
        for(com.pizzaroof.sinfulrush.util.assets.StatLabel l : statLabels)
            statsGroup.addActor(l);
        statsGroup.addActor(cemeteryLocal);
        statsGroup.addActor(hellLocal);

        getStage().addActor(statsGroup);

        statsGroup.setVisible(false);
    }

    protected void repositionStatsLayer(float w, float h) {
        float leftX = sfxSlider.getX();
        float rightX = leftX + sfxSlider.getWidth();
        float hSpace = 100;

        for(StatLabel l : statLabels)
            l.setBoundingsValues(leftX, rightX);

        statLabels.get(0).setY(settingsLabel.getY() - hSpace * 1.5f);
        for(int i=1; i<=GLOBAL_STATS-1; i++) //fino a platformsJumped
            statLabels.get(i).setY(statLabels.get(i-1).getY() - hSpace);

        hellLocal.setPosition(w * 0.5f - hellLocal.getWidth() * hellLocal.getScaleX() - 60, platformsJumped.getY() - hellLocal.getHeight() - hSpace * 1.5f);
        cemeteryLocal.setPosition(w * 0.5f - 40, hellLocal.getY());

        statBackBg.setPosition(w *0.5f - statBackBg.getWidth()*0.5f, hellLocal.getY() + hellLocal.getHeight()*0.5f - statBackBg.getHeight()*0.5f);

        recomputeLocalStats(w, h);
    }

    private void showStatsMenu() {
        //ricicliamo un sacco di cose da settings
        statsGroup.setVisible(true);
        settingsLabel.getActor().setText(getLanguageManager().getText(LanguageManager.Text.STATS));
        settingsLabel.pack();
        statsGroup.addActor(settingsLabel);
        statsGroup.addActor(leftStick);
        statsGroup.addActor(rightStick);
        statsGroup.addActorAt(0, blackBg);
        statsGroup.addActor(settingsClose);

        timePlayed.setText(getLanguageManager().getText(LanguageManager.Text.STATS_TIME_PLAYED), com.pizzaroof.sinfulrush.util.Utils.getFormattedTime(getPreferences().getLong(com.pizzaroof.sinfulrush.Constants.TIME_PLAYED_PREFS, 0)));
        enemiesKilled.setText(getLanguageManager().getText(LanguageManager.Text.STATS_ENEMIES_KILLED), Long.toString(getPreferences().getLong(com.pizzaroof.sinfulrush.Constants.ENEMIES_KILLED_PREFS, 0)));
        friendsSaved.setText(getLanguageManager().getText(LanguageManager.Text.STATS_FRIENDS_SAVED), Long.toString(getPreferences().getLong(com.pizzaroof.sinfulrush.Constants.FRIENDS_SAVED_PREFS, 0)));
        friendsKilled.setText(getLanguageManager().getText(LanguageManager.Text.STATS_FRIENDS_KILLED), Long.toString(getPreferences().getLong(com.pizzaroof.sinfulrush.Constants.FRIENDS_KILLED_PREFS, 0)));
        platformsJumped.setText(getLanguageManager().getText(LanguageManager.Text.STATS_PLATFORMS_JUMPED), Long.toString(getPreferences().getLong(com.pizzaroof.sinfulrush.Constants.PLATFORMS_JUMPED_PREFS, 0)));

        float w = getStage().getCamera().viewportWidth, h = getStage().getCamera().viewportHeight;
        repositionStatsLayer(w, h);

        recomputeUiPositions();
    }

    private void recomputeLocalStats(float w, float h) {
        float hSpace = 100;
        float firstHSpace = 150;
        String bestScore = Integer.toString(getPreferences().getInteger(hellLocal.isChecked() ? com.pizzaroof.sinfulrush.Constants.HELL_HIGHSCORE_PREFS : com.pizzaroof.sinfulrush.Constants.CEMETERY_HIGHSCORE_PREFS, 0));
        String prefix = hellLocal.isChecked() ? com.pizzaroof.sinfulrush.Constants.HELL_LOCAL_PREF : com.pizzaroof.sinfulrush.Constants.CEMETERY_LOCAL_PREF;
        String bossKilled = Long.toString(getPreferences().getLong(prefix+ com.pizzaroof.sinfulrush.Constants.BOSS_KILLED_PREFS, 0));

        bestscoreLabel.setText(getLanguageManager().getText(LanguageManager.Text.STATS_BESTSCORE), bestScore);
        numBossKilled.setText(getLanguageManager().getText(LanguageManager.Text.STATS_BOSS_KILLED), bossKilled);

        statLabels.get(GLOBAL_STATS).setY(hellLocal.getY() - firstHSpace);
        for(int i=GLOBAL_STATS+1; i<statLabels.size(); i++)
            statLabels.get(i).setY(statLabels.get(i-1).getY() - hSpace);
    }

    private void hideStatsMenu() {
        statsGroup.setVisible(false);
    }

    private void showSettingsMenu() {
        settingsLabel.getActor().setText(getLanguageManager().getText(LanguageManager.Text.SETTINGS_TITLE));
        settingsLabel.pack();
        settingsGroup.addActorAt(0, blackBg);
        settingsGroup.addActor(settingsLabel);
        settingsGroup.addActor(leftStick);
        settingsGroup.addActor(rightStick);
        settingsGroup.addActor(settingsClose);

        settingsGroup.setVisible(true);
        settingsCancel.getColor().a = 1;
        settingsOk.getColor().a = 1;
        settingsCancel.setScale(1);
        settingsOk.setVisible(true);
        settingsCancel.setVisible(true);
        settingsOk.setScale(1);
        musicSlider.setValue(getPreferences().getFloat(com.pizzaroof.sinfulrush.Constants.MUSIC_VOLUME_PREFS, com.pizzaroof.sinfulrush.Constants.MUSIC_VOLUME_DEF));
        sfxSlider.setValue(getPreferences().getFloat(com.pizzaroof.sinfulrush.Constants.SFX_VOLUME_PREFS, com.pizzaroof.sinfulrush.Constants.SFX_VOLUME_DEF));

        recomputeUiPositions();
    }

    @Override
    public boolean keyDown(int keycode) {
        if(hasLeftScreen() || !inRendering || game.isShowingAd() || (cemeteryDialog != null && cemeteryDialog.getStage() != null) ||
            (cemeteryUnlockedDialog != null && cemeteryUnlockedDialog.getStage() != null)) return true;

        if(keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
            if(playButton.hasActions()) return true; //se ci sono azioni è durante un'animazione: non facciamo casini

            if(settingsGroup.isVisible()) {
                discardSettings();
            } else if(statsGroup.isVisible()) {
                hideStatsMenu();
            } else if(missionsMenu.isVisible()) {
                missionsMenu.setVisible(false);
                missionsMenu.onClosingMenu();
            } else if(levelsGroup.isVisible()) {
                levelsToPlayMenu();
                //playButton.setVisible(true);
                //levelsGroup.setVisible(false);
            } else if(shopMenu.isVisible()) {
                if(!shopMenu.isDialogOpened()) {
                    shopMenu.setVisible(false);
                    shopMenu.onClosingMenu();
                }
            } else if(exitDialog.getStage() == null) {
                    //Gdx.app.exit();
                    showDialog(exitDialog);
            }
        }
        return false;
    }

    protected void showDialog(Dialog dialog) {
        getStage().addActor(dialogBlackBg);
        dialog.show(stage);
    }

    private void createMinorDialogs(FreeTypeSkin skin, int exitsWithoutInterstitial) {
        /*if(getPreferences().getBoolean(Constants.TUTORIAL_DIALOG_PREF, true)) { //deve uscire il dialog del tutorial
            tutorialDialog = new Dialog("", skin) {
                @Override
                public void result(Object obj) {
                    getSoundManager().click();
                    dialogBlackBg.remove();
                    getPreferences().putBoolean(Constants.TUTORIAL_DIALOG_PREF, false);
                    getPreferences().flush();
                    if(obj.equals(Boolean.TRUE))
                        takeTutorial(exitsWithoutInterstitial);
                }
            };
            tutorialDialog.setModal(true);
            tutorialDialog.setMovable(false);
            tutorialDialog.text(getLanguageManager().getText(LanguageManager.Text.DIALOG_TAKE_TUTORIAL));
            tutorialDialog.getButtonTable().row().expandX();
            tutorialDialog.button("Ok", true);
            tutorialDialog.button(getLanguageManager().getText(LanguageManager.Text.YES), true);
            tutorialDialog.button(getLanguageManager().getText(LanguageManager.Text.OF_COURSE), true);
            tutorialDialog.getButtonTable().row().colspan(3);
            tutorialDialog.button(getLanguageManager().getText(LanguageManager.Text.CHOICE_NO_TUTORIAL), false);
        }*/

        if(getPreferences().getBoolean(com.pizzaroof.sinfulrush.Constants.CEMETERY_UNLOCKED_DIALOG_PREF, true) && Utils.isCemeteryUnlocked(getPreferences())) {
            cemeteryUnlockedDialog = new Dialog("", skin) {
                @Override
                public void result(Object obj) {
                    getSoundManager().click();
                    dialogBlackBg.remove();
                    getPreferences().putBoolean(Constants.CEMETERY_UNLOCKED_DIALOG_PREF, false);
                    getPreferences().flush();
                }
            };

            cemeteryUnlockedDialog.text(getLanguageManager().getText(LanguageManager.Text.GENERAL_CONGRATULATIONS), skin.get("Score", Label.LabelStyle.class));
            cemeteryUnlockedDialog.getContentTable().row();
            cemeteryUnlockedDialog.text(getLanguageManager().getText(LanguageManager.Text.CEMETERY_UNLOCKED), skin.get("darker", Label.LabelStyle.class));
            cemeteryUnlockedDialog.button("  Ok  ");
            cemeteryUnlockedDialog.setMovable(false);
            cemeteryUnlockedDialog.setModal(true);
            cemeteryUnlockedDialog.getContentTable().padBottom(50).padTop(60).padLeft(55).padRight(55);
            cemeteryUnlockedDialog.getButtonTable().padBottom(45);
        }
    }

    public void increaseGold(int amount) {
        game.addGold(amount);
        recomputeWidgetLanguages();
        recomputeUiPositions();
        shopMenu.onVideoWatched();
    }

    public void onErrorPlayingVideo() {
        if(shopMenu.isVisible())
            shopMenu.onErrorPlayingVideo();
    }


    @Override
    public boolean keyUp(int keycode) {
        return hasLeftScreen() || !inRendering;
    }

    @Override
    public boolean keyTyped(char character) {
        return hasLeftScreen() || !inRendering;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return hasLeftScreen() || !inRendering || pointer >= 1 || game.isShowingAd(); //ignoriamo le dita oltre alla 0
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return hasLeftScreen() || !inRendering || pointer >= 1 || game.isShowingAd();
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return hasLeftScreen() || !inRendering || pointer >= 1 || game.isShowingAd();
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return hasLeftScreen() || !inRendering;
    }

    @Override
    public boolean scrolled(int amount) {
        return hasLeftScreen() || !inRendering;
    }
}
