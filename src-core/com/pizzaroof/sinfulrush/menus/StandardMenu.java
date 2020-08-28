package com.pizzaroof.sinfulrush.menus;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.language.LanguageManager;
import com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin;

/**menu da mettere sopra ad altre schermate (tipo statistiche, ecc)*/
public class StandardMenu extends Group {
    /**layer nero*/
    protected Image blackBg, dialogBlackBg;

    protected FreeTypeSkin skin;

    /**stick da mettere a sx e dx del nome menu*/
    protected Image leftStick, rightStick;

    protected ImageButton closeButton;

    protected Container<Label> title;
    protected LanguageManager.Text titleText;

    protected SoundManager soundManager;

    public StandardMenu(AssetManager assetManager, float stickW, float stickH, LanguageManager.Text titleText, SoundManager soundManager) {
        this.titleText = titleText;
        this.soundManager = soundManager;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.8f);
        pixmap.fill();
        blackBg = new Image(new Texture(pixmap));
        pixmap.setColor(0, 0, 0, 0.5f);
        pixmap.fill();
        dialogBlackBg = new Image(new Texture(pixmap));
        pixmap.dispose();

        skin = assetManager.get(Constants.DEFAULT_SKIN_PATH, FreeTypeSkin.class);
        leftStick = new Image(assetManager.get(Constants.DEFAULT_SKIN_ATLAS, TextureAtlas.class).findRegion("bar"));
        leftStick.setWidth(-stickW);
        leftStick.setHeight(stickH);
        rightStick = new Image(leftStick.getDrawable());
        rightStick.setWidth(-leftStick.getWidth());
        rightStick.setHeight(leftStick.getHeight());

        title = new Container<>(new Label("", skin));
        title.setTransform(true);
        title.setScale(1.5f);

        closeButton = new ImageButton(skin, "close");
        closeButton.setSize(100, 100);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                soundManager.click();
                onClosingMenu();
                StandardMenu.this.setVisible(false);
            }
        });

        addActor(blackBg);
        addActor(leftStick);
        addActor(rightStick);
        addActor(title);
        addActor(closeButton);
    }

    public StandardMenu(AssetManager assetManager, LanguageManager.Text titleText, SoundManager soundManager) {
        this(assetManager, 296, 30, titleText, soundManager);
    }

    public void updateLanguages(float w, float h, LanguageManager languageManager, boolean alsoLayout) {
        title.getActor().setText(languageManager.getText(titleText));
        title.pack();

        if(alsoLayout)
            updateLayout(w, h);
    }

    /**aggiorna dimensioni/posizioni con nuova larghezza/altezza*/
    public void updateLayout(float w, float h) {
        blackBg.setSize(w, h);
        dialogBlackBg.setSize(w, h);

        float setlabW = title.getActor().getWidth() * title.getScaleX();
        title.setPosition(w * 0.5f - setlabW*0.5f,h * 0.85f - title.getActor().getHeight()*0.5f);

        leftStick.setPosition(w * 0.05f + rightStick.getWidth(), title.getY() + rightStick.getHeight());
        rightStick.setPosition(w * 0.95f - rightStick.getWidth(), leftStick.getY());

        closeButton.setPosition(w - closeButton.getWidth() - 43,h - 120);
    }

    public void updateLayout() {
        float w = getStage().getCamera().viewportWidth, h = getStage().getCamera().viewportHeight;
        updateLayout(w, h);
    }

    protected void showDialog(Dialog dialog) {
        getStage().addActor(dialogBlackBg);
        dialog.show(getStage());
    }

    protected void closeDialog() {
        dialogBlackBg.remove();
    }

    /*@Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if(!b)
            onClosingMenu();
    }*/

    public void onClosingMenu() {
        closeDialog();
    }
}
