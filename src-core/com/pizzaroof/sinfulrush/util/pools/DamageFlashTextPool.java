package com.pizzaroof.sinfulrush.util.pools;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Pool;
import com.pizzaroof.sinfulrush.actors.DamageFlashText;
import com.pizzaroof.sinfulrush.actors.FlashText;

class DamageFlashTextPool extends Pool<FlashText> {

    private Skin skin;

    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    @Override
    protected DamageFlashText newObject() {
        return new DamageFlashText(skin, this);
    }
}
