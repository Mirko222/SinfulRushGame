package com.pizzaroof.sinfulrush.actors.basics;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class NGImageTextButton extends ImageTextButton {
    public NGImageTextButton(String text, Skin skin) {
        super(text, skin);
        Image tmp = getImage();
        getImage().remove();
        add(tmp);
    }

    public NGImageTextButton(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
        Image tmp = getImage();
        getImage().remove();
        add(tmp);
    }
}
