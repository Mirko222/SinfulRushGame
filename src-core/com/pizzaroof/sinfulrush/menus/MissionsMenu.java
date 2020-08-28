package com.pizzaroof.sinfulrush.menus;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.language.LanguageManager;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.util.Pair;
import com.pizzaroof.sinfulrush.util.assets.StatLabel;

import java.util.ArrayList;
import java.util.Collections;

/**menu delle missioni*/
public class MissionsMenu extends StandardMenu {

    public static final int DESCR_PAD = 65;

    private LanguageManager languageManager;

    private Table activeTable;
    private ScrollPane finishedPane;
    private Table finishedTable;

    /**missioni attive*/
    private ArrayList<Mission> actives;
    /**missioni finite (tutto il blocco è finito)*/
    private ArrayList<Mission> finished;

    private AssetManager assetManager;

    private Image whiteStrip;

    /**manteniamo riferimento alla coppia (missione, descrizione) in modo da poterla modificare se si cambia lingua*/
    private ArrayList<Pair<Mission, Container<Label>>> msnDescr;

    public MissionsMenu(NGame game, AssetManager assetManager, SoundManager soundManager, ArrayList<Mission> activeMissions, ArrayList<Mission> finishedMissions) {
        super(assetManager, LanguageManager.Text.MISSIONS, soundManager);
        this.languageManager = game.getLanguageManager();
        this.assetManager = assetManager;
        msnDescr = new ArrayList<>();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 0.5f);
        pixmap.fill();
        whiteStrip = new Image(new Texture(pixmap));
        pixmap.dispose();
        whiteStrip.setHeight(StatLabel.LINE_HEIGHT);

        actives = activeMissions;
        finished = new ArrayList<>(finishedMissions);
        Collections.reverse(finished);

        //display delle missioni

        activeTable = new Table();
        finishedTable = new Table();
        rebuildMissionsTable();

        finishedPane = new ScrollPane(finishedTable);
        finishedPane.setScrollingDisabled(true, false);


        addActor(activeTable);
        activeTable.pack();
        addActor(whiteStrip);
        addActor(finishedPane);
    }

    protected void rebuildMissionsTable() {
        activeTable.clear();
        //activeTable.setDebug(true);

        for(Mission ms : actives) {
            //descrizione della missione
            activeTable.row().expandX();
            Container<Label> descr;
            try {
                descr = new Container<>(new Label(ms.getDisplayDescription(languageManager), skin));
            }catch(Exception e) {
                descr = new Container<>(new Label("", skin));
            }
            descr.setTransform(true);
            descr.setScale(0.8f);
            descr.pack();
            msnDescr.add(new Pair<>(ms, descr));
            activeTable.add(descr).left();

            Image check = new Image(assetManager.get(Constants.CUSTOM_BUTTONS_DECORATIONS, TextureAtlas.class).findRegion(ms.isCompleted() ? "pallino_pieno" : "pallino_vuoto"));
            //activeTable.add(check).size(55).right().bottom();
            boolean multiLines = ms.getDisplayDescription(languageManager).contains("\n");
            if(multiLines)
                activeTable.add(check).size(55).right().bottom().spaceBottom(30);
            else
                activeTable.add(check).size(55).right().bottom();


            //ricompensa della missione
            activeTable.row().padBottom(20);

            Image coin = new Image(assetManager.get(Constants.COIN_ATLAS, TextureAtlas.class).findRegion(Constants.COIN_REG_NAME));
            Container<Label> reward = new Container<>(new Label("[#ffce21]"+ms.getReward()+"[]", skin));
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
            activeTable.add(hgroup).height(coin.getHeight() * coinScale).left().top();
        }

        finishedTable.clear();
        //finishedTable.setDebug(true);
        for(Mission ms : finished) {
            finishedTable.row().expandX().padBottom(10);
            Container<Label> descr;
            try {
                descr = new Container<>(new Label(ms.getDisplayDescription(languageManager), skin));
            }catch(Exception e) {
                descr = new Container<>(new Label("", skin));
            }
            descr.setTransform(true);
            descr.setScale(0.8f);
            descr.pack();
            finishedTable.add(descr).left();
            msnDescr.add(new Pair<>(ms, descr));

            Image check = new Image(assetManager.get(Constants.CUSTOM_BUTTONS_DECORATIONS, TextureAtlas.class).findRegion("pallino_pieno")); //è per forza completo

            boolean multiLines = ms.getDisplayDescription(languageManager).contains("\n");
            if(multiLines)
                finishedTable.add(check).size(55).right().bottom().spaceBottom(30);
            else
                finishedTable.add(check).size(55).right().bottom();
        }
    }

    public void setActiveAndCompletedMissions(ArrayList<Mission> active, ArrayList<Mission> completed) {
        this.actives = active;
        this.finished = completed;
    }

    @Override
    public void updateLanguages(float w, float h, LanguageManager languageManager, boolean alsoLayout) {
        /*for(Pair<Mission, Container<Label>> d : msnDescr) { //aggiorna descrizione delle missioni
            d.v2.getActor().setText(d.v1.getDisplayDescription(languageManager));
            d.v2.pack();
        }*/
        rebuildMissionsTable();

        super.updateLanguages(w, h, languageManager, alsoLayout);
    }

    @Override
    public void updateLayout(float w, float h) {
        super.updateLayout(w, h);

        activeTable.setWidth(w - 2*DESCR_PAD);

        int totH = 0;
        for(Mission m : actives) {
            if (m.getDisplayDescription(languageManager).contains("\n"))
                totH += 200;
            else
                totH += 150;
        }
        activeTable.setPosition(DESCR_PAD, leftStick.getY() - totH); //250
        whiteStrip.setWidth(activeTable.getWidth());
        if(actives.size() > 0)
            whiteStrip.setPosition(activeTable.getX(), activeTable.getY() - 20);
        else
            whiteStrip.setPosition(activeTable.getX(), activeTable.getY() - 80);

        finishedTable.setWidth(activeTable.getWidth());
        finishedPane.setWidth(activeTable.getWidth());
        float paney = 200;

        int mh = 0;
        for(Mission m : finished)
            if(m.getDisplayDescription(languageManager).contains("\n"))
                mh += 160;
            else
                mh += 80;
        finishedPane.setHeight(Math.min(whiteStrip.getY() - 20 - paney, mh));
        finishedPane.setPosition(activeTable.getX(), whiteStrip.getY() - finishedPane.getHeight() - 10);
    }
}
