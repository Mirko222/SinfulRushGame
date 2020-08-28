package com.pizzaroof.sinfulrush.menus;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.TimeUtils;
import com.brashmonkey.spriter.gdx.SpriterData;
import com.brashmonkey.spriter.gdx.SpriterDataLoader;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.basics.NGImageTextButton;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.language.LanguageManager;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.screens.MainMenuLoaderScreen;
import com.pizzaroof.sinfulrush.util.PlayerPower;
import com.pizzaroof.sinfulrush.util.Utils;

import java.util.ArrayList;

/**menu per shop*/
public class ShopMenu extends StandardMenu {

    private static final int SECONDS_VIDEO_COOLDOWN = 60 * 10;

    private static final int NUM_PLAYER_SKINS = 6;

    /**scroll pane con dentro i personaggi*/
    private ScrollPane playersPane;
    private static final int PANE_HEIGHT = 1200;

    private static final int COLUMN_WIDTH = 500;

    private static final float LOCKED_DARK = 0.5f;

    private ArrayList<PlayerItem> pitems;

    private NGame game;

    protected Dialog confirmDialog, moreMoneyDialog, errorConnectiom, errorTime;

    private LanguageManager languageManager;

    private PlayerItem buyingItem;

    /**stringa iniziale sullo stato dei player. NB: non viene tenuta aggiornata*/
    private String initStringStatus;

    /**bisogna riportare lo scroll pane su quello selezionato?*/
    private boolean backToTheSelected;

    private Container<Label> myMoney;
    private Image myMoneyIcon;

    private SpriterDataLoader.SpriterDataParameter spriterDataParameter;

    private ImageTextButton videoBtn;

    private long lastVideoTime;
    /**sta aspettando tempo per riguardare il video?*/
    private boolean waitingForVideo;

    private MissionsMenu missionsMenu;

    public ShopMenu(NGame game, AssetManager assetManager, SoundManager soundManager, LanguageManager languageManager) {
        super(assetManager, LanguageManager.Text.SHOP, soundManager);
        this.game = game;
        lastVideoTime = game.getPreferences().getLong(Constants.LAST_GOLD_VIDEO_PREF, 0);
        waitingForVideo = TimeUtils.millis() - lastVideoTime <= SECONDS_VIDEO_COOLDOWN * 1000;

        this.languageManager = languageManager;
        backToTheSelected = true;
        spriterDataParameter = new SpriterDataLoader.SpriterDataParameter();

        initStringStatus = getSkinStatus();

        createItems(game);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.55f, 0.7f,.86f, 1f);
        pixmap.fill();
        Image blueBg = new Image(new Texture(pixmap));
        pixmap.dispose();

        videoBtn = new NGImageTextButton(languageManager.getText(LanguageManager.Text.WATCH_VIDEO_FOR_MONEY), skin, waitingForVideo ? "green2" : "green");
        videoBtn.setTransform(true);
        videoBtn.setScale(0.8f);

        videoBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                soundManager.click();
                backToTheSelected = false;
                long time = TimeUtils.millis() - lastVideoTime;
                if(time > SECONDS_VIDEO_COOLDOWN * 1000)
                    game.startRewardedVideo();
                else {
                    errorTime.getContentTable().clear();
                    errorTime.text(languageManager.getText(LanguageManager.Text.WAIT_TIME_VIDEO, Utils.getFormattedTimeSmall((long)Math.ceil(SECONDS_VIDEO_COOLDOWN - time / 1000.f))), skin.get("darker", Label.LabelStyle.class));
                    showDialog(errorTime);
                }
            }
        });

        addActor(videoBtn);

        myMoneyIcon = new Image(assetManager.get(Constants.COIN_ATLAS, TextureAtlas.class).findRegion(Constants.COIN_REG_NAME));
        myMoneyIcon.setSize(60, 60);
        myMoney = new Container<>(new Label(Integer.toString(game.getGold()), skin));
        myMoney.setTransform(true);
        myMoney.setScale(0.9f);
        myMoney.pack();
        addActor(myMoneyIcon);
        addActor(myMoney);

        Table playersTable = new Table();
        //playersTable.setDebug(true);

        playersTable.row().padRight(100).padLeft(100).padBottom(40).width(COLUMN_WIDTH); //immagini
        for(PlayerItem item : pitems) {
            Image img = new Image(assetManager.get(item.atlasPath, TextureAtlas.class).findRegion(item.regionName));
            if(item.status.equals(ItemStatus.LOCKED))
                img.setColor(LOCKED_DARK, LOCKED_DARK, LOCKED_DARK, 1f);
            playersTable.add(img).size(item.imageWidth, 553);
            item.playerImage = img;
        }

        playersTable.row().padRight(100).padLeft(100).width(COLUMN_WIDTH).height(80); //nome personaggi
        for(PlayerItem item : pitems) {
            Stack stack = new Stack();
            stack.add(new Image(blueBg.getDrawable()));
            stack.add(item.nameLbl);
            item.nameCell = playersTable.add(stack); //fill center
        }


        playersTable.row().padRight(100).padLeft(100).spaceBottom(30).spaceTop(10); //descrizioni
        for(PlayerItem item : pitems) {
            Table descrTable = new Table();
            //descrTable.setDebug(true);
            item.descrCell = descrTable.add(item.descrLbl).top().left();

            if(item.status.equals(ItemStatus.LOCKED)) {
                descrTable.row().padTop(10);

                Image coin = new Image(assetManager.get(Constants.COIN_ATLAS, TextureAtlas.class).findRegion(Constants.COIN_REG_NAME));
                Container<Label> price = new Container<>(new Label(Integer.toString(item.cost), skin));
                price.setTransform(true);
                price.setScale(item.descrLbl.getScaleX() / 0.6f);
                price.pack();

                HorizontalGroup hgroup = new HorizontalGroup();
                item.costGroup = hgroup;
                hgroup.setTransform(true);
                hgroup.setScale(0.6f);
                hgroup.addActor(coin);
                hgroup.addActor(price);
                hgroup.space(20);
                descrTable.add(hgroup).height(coin.getHeight() * 0.5f).left().top();
            }

            playersTable.add(descrTable).top().left();
        }

        playersTable.row().padRight(100).padLeft(100); //bottoni
        for(PlayerItem item : pitems)
            playersTable.add(item.stBtn).fill();

        playersPane = new ScrollPane(playersTable);
        addActor(playersPane);


        confirmDialog = new Dialog("", skin) {
            @Override
            public void result(Object obj) {
                if(obj.equals(Boolean.TRUE)) {  //ha confermato che vuole comprare
                    buyingItem.status = ItemStatus.UNLOCKED;
                    buyingItem.costGroup.setVisible(false); //rimuovi prezzo
                    buyingItem.playerImage.setColor(1.f, 1.f, 1.f, 1.f);

                    float w = getStage().getCamera().viewportWidth, h = getStage().getCamera().viewportHeight;
                    backToTheSelected = false;
                    game.addGold(-buyingItem.cost);
                    updateLanguages(w, h, languageManager, true);

                    updateShopPrefs();
                    updateMissionStatus();
                    selectPlayer(w, h, buyingItem);
                }
                soundManager.click();
                closeDialog();

                buyingItem = null;
            }
        };
        confirmDialog.text(languageManager.getText(LanguageManager.Text.BUY_CONFIRM), skin.get("darker", Label.LabelStyle.class));
        confirmDialog.getContentTable().padBottom(50).padTop(60).padLeft(55).padRight(55);
        confirmDialog.getButtonTable().padBottom(45);
        TextButton yesB = new TextButton(languageManager.getText(LanguageManager.Text.YES), skin);
        TextButton noB = new TextButton(languageManager.getText(LanguageManager.Text.NO), skin);
        confirmDialog.button(yesB, true);
        confirmDialog.button(noB, false);
        confirmDialog.getButtonTable().getCells().first().width(200).padRight(23);
        confirmDialog.getButtonTable().getCells().get(1).width(200).padLeft(23);
        confirmDialog.setMovable(false);
        confirmDialog.setModal(true); //non vogliamo che possa cliccare da altre parti

        errorConnectiom = new Dialog("", skin) {
            @Override
            public void result(Object obj) {
                soundManager.click();
                closeDialog();
            }
        };
        errorConnectiom.text(languageManager.getText(LanguageManager.Text.ERROR_LOADING_VIDEO), skin.get("darker", Label.LabelStyle.class));
        errorConnectiom.getContentTable().padBottom(50).padTop(60).padLeft(55).padRight(55);
        errorConnectiom.getButtonTable().padBottom(45);
        TextButton okB = new TextButton("Ok", skin);
        errorConnectiom.button(okB, true);
        errorConnectiom.getButtonTable().getCells().first().width(200);
        errorConnectiom.setMovable(false);
        errorConnectiom.setModal(true); //non vogliamo che possa cliccare da altre parti

        errorTime = new Dialog("", skin) {
            @Override
            public void result(Object obj) {
                soundManager.click();
                closeDialog();
            }
        };
        errorTime.text(languageManager.getText(LanguageManager.Text.ERROR_LOADING_VIDEO), skin.get("darker", Label.LabelStyle.class));
        errorTime.getContentTable().padBottom(50).padTop(60).padLeft(55).padRight(55);
        errorTime.getButtonTable().padBottom(45);
        TextButton okB2 = new TextButton("Ok", skin);
        errorTime.button(okB2, true);
        errorTime.getButtonTable().getCells().first().width(200);
        errorTime.setMovable(false);
        errorTime.setModal(true); //non vogliamo che possa cliccare da altre parti

        moreMoneyDialog = new Dialog("", skin) {
            @Override
            public void result(Object obj) {
                soundManager.click();
                closeDialog();
            }
        };
        moreMoneyDialog.text(languageManager.getText(LanguageManager.Text.NEED_MORE_MONEY), skin.get("darker", Label.LabelStyle.class));
        moreMoneyDialog.getContentTable().padBottom(50).padTop(60).padLeft(55).padRight(55);
        moreMoneyDialog.getButtonTable().padBottom(45);
        TextButton okB3 = new TextButton("Ok", skin);
        moreMoneyDialog.button(okB3, true);
        moreMoneyDialog.getButtonTable().getCells().first().width(200);
        moreMoneyDialog.setMovable(false);
        moreMoneyDialog.setModal(true); //non vogliamo che possa cliccare da altre parti
    }

    @Override
    public void updateLanguages(float w, float h, LanguageManager languageManager, boolean alsoLayout) {
        for(PlayerItem item : pitems) {
            item.updateDescrLbl(languageManager);
            item.updateStBtnText(languageManager);
            item.updateStBtnTouch();
        }

        confirmDialog.getContentTable().clear();
        ((TextButton)confirmDialog.getButtonTable().getCells().first().getActor()).setText(languageManager.getText(LanguageManager.Text.YES));
        ((TextButton)confirmDialog.getButtonTable().getCells().get(1).getActor()).setText(languageManager.getText(LanguageManager.Text.NO));
        confirmDialog.text(languageManager.getText(LanguageManager.Text.BUY_CONFIRM), skin.get("darker", Label.LabelStyle.class));

        errorConnectiom.getContentTable().clear();
        errorConnectiom.text(languageManager.getText(LanguageManager.Text.ERROR_LOADING_VIDEO), skin.get("darker", Label.LabelStyle.class));

        moreMoneyDialog.getContentTable().clear();
        moreMoneyDialog.text(languageManager.getText(LanguageManager.Text.NEED_MORE_MONEY), skin.get("darker", Label.LabelStyle.class));

        videoBtn.setText(languageManager.getText(LanguageManager.Text.WATCH_VIDEO_FOR_MONEY));

        super.updateLanguages(w, h, languageManager, alsoLayout);

        myMoneyIcon.setPosition(w - closeButton.getWidth() - closeButton.getX(), closeButton.getY());
        myMoney.getActor().setText(Integer.toString(game.getGold()));
        myMoney.pack();
        myMoney.setPosition(myMoneyIcon.getX()+myMoneyIcon.getWidth()+20, myMoneyIcon.getY()+5);

        videoBtn.setPosition(w*0.5f - videoBtn.getWidth()*videoBtn.getScaleX()*0.5f, title.getY() - 200); //h-520
        videoBtn.pack();
        videoBtn.setHeight(170);
    }

    public void updateLogic() {
        if(waitingForVideo && TimeUtils.millis() - lastVideoTime > SECONDS_VIDEO_COOLDOWN * 1000) {
            waitingForVideo = false;
            videoBtn.setStyle(skin.get("green", ImageTextButton.ImageTextButtonStyle.class));
        }
    }

    public void onErrorPlayingVideo() {
        showDialog(errorConnectiom);
    }

    /**singolo player da vendere*/
    public class PlayerItem {
        String path;
        int charmap;
        LanguageManager.Text description, namePl;
        ItemStatus status;
        /**quanto costa l'item?*/
        int cost;
        int id;

        int imageWidth;

        PlayerPower playerPower;

        /**path dell'atlas e nome della ragione per l'immagine nel negozio*/
        String atlasPath, regionName;

        Container<Label> descrLbl, nameLbl;

        /**cella del nome nella tabella*/
        Cell<Stack> nameCell;

        /**cella della descrizione*/
        Cell<Container<Label>> descrCell;

        /**gruppo dove ci sono i costi: va tolto se viene comprato*/
        HorizontalGroup costGroup;

        /**tasto per selezionare/comprare*/
        TextButton stBtn;

        Image playerImage;

        public PlayerItem(int id, String path, int charmap, LanguageManager.Text description, LanguageManager.Text name, ItemStatus status, int cost, String atlas, String region, int imageWidth) {
            this.id = id;
            this.imageWidth = imageWidth;
            this.path = path;
            this.charmap = charmap;
            this.description = description;
            this.status = status;
            this.cost = cost;
            this.atlasPath = atlas;
            this.regionName = region;
            this.namePl = name;

            playerPower = new PlayerPower();

            descrLbl = new Container<>(new Label("", skin));
            descrLbl.setTransform(true);
            descrLbl.setScale(0.7f);
            descrLbl.pack();

            nameLbl = new Container<>(new Label("", skin));
            //nameLbl.setTransform(true);
            //nameLbl.setScale(1.15f);
            nameLbl.pack();
            stBtn = new TextButton("", skin);
            stBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    float w = getStage().getCamera().viewportWidth, h = getStage().getCamera().viewportHeight;
                    switch(PlayerItem.this.status) {
                        case SELECTED: break;
                        case UNLOCKED:
                            soundManager.click();
                            /*backToTheSelected = true;
                            getSelectedItem().status = ItemStatus.UNLOCKED;
                            PlayerItem.this.status = ItemStatus.SELECTED;
                            updateLanguages(w, h, languageManager, true);*/
                            selectPlayer(w, h, PlayerItem.this);
                            break;
                        case LOCKED:
                            soundManager.click();
                            if(PlayerItem.this.cost <= game.getGold()) {
                                buyingItem = PlayerItem.this;
                                showDialog(confirmDialog);
                            } else {
                                showDialog(moreMoneyDialog);
                                stBtn.setChecked(true);
                            }
                            break;
                    }
                }
            });
        }

        public void updateDescrLbl(LanguageManager languageManager) {
            descrLbl.getActor().setText(languageManager.getText(description));
            descrLbl.pack();
            descrLbl.layout();
            nameLbl.getActor().setText(languageManager.getText(namePl));
            nameLbl.pack();

            nameCell.getActor().invalidate();
            //nameCell.width(nameLbl.getWidth()+15).fill().expand();

            //descrLbl.setOrigin(descrLbl.getWidth() * descrLbl.getScaleX() * 0.5f, descrLbl.getHeight() * descrLbl.getScaleY() * 0.5f);
            descrCell.width(descrLbl.getWidth() * descrLbl.getScaleX()).height(descrLbl.getHeight() * descrLbl.getScaleY());
        }

        /**ricalcola testi dei tasti*/
        public void updateStBtnText(LanguageManager languageManager) {
            switch (status) {
                case LOCKED:
                    stBtn.setText(languageManager.getText(LanguageManager.Text.BUY));
                    break;
                case UNLOCKED:
                    stBtn.setText(languageManager.getText(LanguageManager.Text.SELECT));
                    break;
                case SELECTED:
                    stBtn.setText(languageManager.getText(LanguageManager.Text.SELECTED));
                    break;
            }
        }

        /**cambia solo struttura dei tassti... magari ha senso chiamarla senza cambiare i testi*/
        public void updateStBtnTouch() {
            switch (status) {
                case UNLOCKED:
                    stBtn.setTouchable(Touchable.enabled);
                    stBtn.setChecked(false);
                    stBtn.setStyle(skin.get("toggle2", TextButton.TextButtonStyle.class));
                    break;
                case LOCKED:
                    //stBtn.setTouchable(cost <= game.getGold() ? Touchable.enabled : Touchable.disabled);
                    stBtn.setTouchable(Touchable.enabled);
                    stBtn.setChecked(cost > game.getGold());
                    stBtn.setStyle(skin.get(cost <= game.getGold() ? "blue" : "toggle3", TextButton.TextButtonStyle.class));
                    break;
                case SELECTED:
                    stBtn.setTouchable(Touchable.disabled);
                    stBtn.setChecked(true);
                    stBtn.setStyle(skin.get("toggle2", TextButton.TextButtonStyle.class));
                    break;
            }
        }

        public PlayerPower getPlayerPowers() {
            return playerPower;
        }

        public String getPlayerPath() {
            return path;
        }

        public int getCharmap() {
            return charmap;
        }
    }

    protected void selectPlayer(float w, float h, PlayerItem item) {
        backToTheSelected = true;
        getSelectedItem().status = ItemStatus.UNLOCKED;
        item.status = ItemStatus.SELECTED;
        updateLanguages(w, h, languageManager, true);
    }

    private enum ItemStatus {
        LOCKED,
        UNLOCKED,
        SELECTED //implica unlocked
    }

    private String getSkinStatus() {
        String plSkinStatus = game.getPreferences().getString(Constants.PLAYERS_SKIN_PREF, "");
        if(plSkinStatus.length() == 0)
            plSkinStatus += statusToChar(ItemStatus.SELECTED);
        for(int i=plSkinStatus.length(); i<NUM_PLAYER_SKINS; i++)
            plSkinStatus += statusToChar(ItemStatus.LOCKED);
        return plSkinStatus;
    }

    /*@Override
    public void setVisible(boolean v) {
        super.setVisible(v);
        if(!v)
            updateShopPrefs();
        //else
        //    updateLayout();
    }*/

    @Override
    public void onClosingMenu() {
        updateShopPrefs();
        String path = getSelectedItem().path;
        if(!game.getAssetManager().isLoaded(path)) {
            loadPlayer(path);
            ((MainMenuLoaderScreen)game.getScreen()).resetLoadingDone(); //deve continuare a caricare
        }
    }

    private ItemStatus charToStatus(char c) {
        if(c == 'L') return ItemStatus.LOCKED;
        if(c == 'U') return ItemStatus.UNLOCKED;
        if(c == 'S') return ItemStatus.SELECTED;
        return null;
    }

    private char statusToChar(ItemStatus status) {
        switch(status) {
            case LOCKED: return 'L';
            case SELECTED: return 'S';
            case UNLOCKED: return 'U';
        }
        return 0;
    }

    public int getSelectedIndex() {
        for(int i=0; i<pitems.size(); i++)
            if(pitems.get(i).status.equals(ItemStatus.SELECTED))
                return i;
        return 0;
    }

    public PlayerItem getSelectedItem() {
        for(PlayerItem item : pitems)
            if(item.status.equals(ItemStatus.SELECTED))
                return item;
        return null;
    }

    private void updateSkinsPref() {
        StringBuilder str = new StringBuilder();
        for(PlayerItem item : pitems)
            str.append(statusToChar(item.status));
        game.getPreferences().putString(Constants.PLAYERS_SKIN_PREF, str.toString());
    }

    public void updateShopPrefs() {
        updateSkinsPref();
        game.getPreferences().putInteger(Constants.GOLD_PREF, game.getGold());
        game.getPreferences().flush();
    }

    public void onVideoWatched() {
        game.getPreferences().putLong(Constants.LAST_GOLD_VIDEO_PREF, TimeUtils.millis());
        backToTheSelected = false;
        updateShopPrefs();
        lastVideoTime = game.getPreferences().getLong(Constants.LAST_GOLD_VIDEO_PREF, 0);
        waitingForVideo = true;
        videoBtn.setStyle(skin.get("green2", ImageTextButton.ImageTextButtonStyle.class));
    }

    public void setMissionsMenu(MissionsMenu missionsMenu) {
        this.missionsMenu = missionsMenu;
        updateMissionStatus();
    }

    @Override
    public void updateLayout(float w, float h) {
        super.updateLayout(w, h);

        playersPane.setPosition(0, 100);
        playersPane.setSize(w, PANE_HEIGHT);
        playersPane.layout();

        if(backToTheSelected) {
            int index = getSelectedIndex();
            playersPane.setScrollX(index * (COLUMN_WIDTH + 200));
        }
    }

    private void createItems(NGame game) {
        pitems = new ArrayList<>();

        //player normale
        pitems.add(new PlayerItem(0, Constants.THIEF_DIRECTORY, 0, LanguageManager.Text.PLAYER_DESCR_1, LanguageManager.Text.YOUNG_WARRIOR_PL, charToStatus(initStringStatus.charAt(0)), 0, Constants.SHOP_PLAYERS_ATLAS, "base", 407));

        //assassino //750
        PlayerItem p2 = new PlayerItem(1, Constants.THIEF_DIRECTORY, 1, LanguageManager.Text.PLAYER_DESCR_2, LanguageManager.Text.ASSASSIN_PL, charToStatus(initStringStatus.charAt(1)), 750, Constants.SHOP_PLAYERS_ATLAS, "ninja", 404);
        p2.playerPower.setPunchDamageMultiplier(1.2f);
        p2.playerPower.setSwordDamageMultiplier(1.2f);
        pitems.add(p2);

        //cavaliere //1500
        PlayerItem p3 = new PlayerItem(2, Constants.KNIGHT_DIRECTORY, -1, LanguageManager.Text.PLAYER_DESCR_3, LanguageManager.Text.KNIGHT_PL, charToStatus(initStringStatus.charAt(2)), 1500, Constants.SHOP_PLAYERS_ATLAS, "cavaliere", 328);
        p3.playerPower.setMaxHp(125);
        pitems.add(p3);

        //ladro //2000
        PlayerItem p4 = new PlayerItem(3, Constants.THIEF_DIRECTORY, -1, LanguageManager.Text.PLAYER_DESCR_4, LanguageManager.Text.THIEF_PL, charToStatus(initStringStatus.charAt(3)), 2000, Constants.SHOP_PLAYERS_ATLAS, "ladro", 422);
        p4.playerPower.setSpeedMultiplier(0.9f); //0.9
        pitems.add(p4);

        //elfo //2500
        PlayerItem p5 = new PlayerItem(4, Constants.ELF_DIRECTORY, -1, LanguageManager.Text.PLAYER_DESCR_5, LanguageManager.Text.ELF_PL, charToStatus(initStringStatus.charAt(4)), 2500, Constants.SHOP_PLAYERS_ATLAS, "elfo", 418);
        p5.playerPower.setMalusMultiplier(0.55f);
        p5.playerPower.setSceptreDamageMultiplier(1.2f);
        pitems.add(p5);

        //lupo mannaro //3500
        PlayerItem p6 = new PlayerItem(5, Constants.WEREWOLF_DIRECTORY, 1, LanguageManager.Text.PLAYER_DESCR_6, LanguageManager.Text.WEREWOLF_PL, charToStatus(initStringStatus.charAt(5)), 3500, Constants.SHOP_PLAYERS_ATLAS, "lupo", 411);
        p6.playerPower.setMaxHp(65);
        p6.playerPower.setSwordDamageMultiplier(1.7f);
        p6.playerPower.setPunchDamageMultiplier(1.7f);
        p6.playerPower.setSceptreDamageMultiplier(1.7f);
        pitems.add(p6);
    }

    private void loadPlayer(String directory) {
        game.getAssetManager().load(Utils.playerScmlPath(directory), SpriterData.class, spriterDataParameter);
    }

    public boolean isDialogOpened() {
        return errorConnectiom.getStage()!=null || errorTime.getStage()!=null || confirmDialog.getStage()!=null || moreMoneyDialog.getStage()!=null;
    }

    /**aggiorna le missioni che hanno a che fare con lo shop*/
    public void updateMissionStatus() {
        boolean characterBought = false;
        for(PlayerItem item : pitems)
            if(!item.status.equals(ItemStatus.LOCKED) && item.cost > 0)
                characterBought = true;

        if(characterBought) {
            boolean used = false;
            for (Mission m : game.getMissionManager().getAllMissions())
                if (!m.isCompleted() && m.getType().equals(Mission.MissionType.BUY_PLAYER)) { //per ora l'unica missione è compra un giocatore, quindi comprandone una abbiamo fatto, ma in generale sarà diverso
                    m.setNumReached(m.getNumToReach());
                    game.addGold(m.getReward());
                    used = true;
                }
            if (used) {
                game.getMissionManager().updateActiveMissions();
                game.getMissionManager().putMissionsOnPrefs();
                game.getPreferences().flush();
                missionsMenu.setActiveAndCompletedMissions(game.getActiveMissions(), game.getCompletedMissions());
                missionsMenu.rebuildMissionsTable();
            }
        }
    }
}
