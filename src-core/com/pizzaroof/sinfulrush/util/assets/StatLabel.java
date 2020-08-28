package com.pizzaroof.sinfulrush.util.assets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;


/**label per statistiche*/
public class StatLabel extends Actor {

    public static final int LINE_HEIGHT = 5;

    private Container<Label> name, value;
    private Texture pixel;

    private float leftX, rightX;

    public StatLabel(Skin skin, Texture singlePixel) {
        name = new Container<>(new Label("", skin));
        value = new Container<>(new Label("", skin));
        this.pixel = singlePixel;
        setTransform(true);
        setScale(0.8f);
    }

    public void setBoundingsValues(float leftX, float rightX) {
        this.leftX = leftX;
        this.rightX = rightX;

        name.setX(leftX);
        value.setX(rightX - value.getActor().getWidth() * value.getScaleX());
    }

    @Override
    public void setY(float y) {
        super.setY(y);
        name.setY(y);
        value.setY(y);
    }

    public void setText(String strname, String strvalue) {
        name.getActor().setText(strname);
        value.getActor().setText(strvalue);
        name.pack();
        value.pack();

        name.setX(leftX);
        value.setX(rightX - value.getActor().getWidth() * value.getScaleX());
    }

    @Override
    public void draw(Batch batch, float alpha) {
        super.draw(batch, alpha);

        name.draw(batch, alpha);
        value.draw(batch, alpha);
        if(pixel != null)
            batch.draw(pixel, leftX, name.getY()-30/*15*/, rightX - leftX, LINE_HEIGHT);
    }

    public void setTransform(boolean v) {
        name.setTransform(v);
        value.setTransform(v);
    }

    public void setScale(float scale) {
        name.setScale(scale);
        value.setScale(scale);
    }

}
