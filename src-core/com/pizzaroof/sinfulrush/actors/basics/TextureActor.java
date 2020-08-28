package com.pizzaroof.sinfulrush.actors.basics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;

/**è una texture: gestita però come actor, per poter essere stampata da uno stage*/
public class TextureActor extends Actor implements Disposable {
    protected Texture texture;

    public TextureActor(Texture texture) {
        this.texture = texture;
    }

    @Override
    public void act(float delta) {
    }

    @Override
    public void draw(Batch batch, float alpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
