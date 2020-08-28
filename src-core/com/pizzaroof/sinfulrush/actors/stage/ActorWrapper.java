package com.pizzaroof.sinfulrush.actors.stage;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;


/**wrapper per trasformare un Actor in un DoubleActActor
 * utile per esempio con Label, e altri Widget di scene2d*/
public class ActorWrapper extends DoubleActActor {
    private Actor actor;

    public ActorWrapper(Actor actor) {
        this.actor = actor;
    }

    @Override
    public void actSkipTolerant(float delta) {
        actor.act(delta);
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        return actor.hit(x, y, touchable);
    }

    @Override
    public void draw(Batch batch, float alpha) {
        actor.draw(batch, alpha);
    }

    @Override
    public void setColor(Color c) {
        actor.setColor(c);
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        actor.setColor(r, g, b, a);
    }

    @Override
    public void setScale(float x, float y) {
        actor.setScale(x, y);
    }

    @Override
    public void setScaleX(float x) {
        actor.setScaleX(x);
    }

    @Override
    public void setScaleY(float y) {
        actor.setScaleY(y);
    }

    @Override
    public void setX(float x) {
        actor.setX(x);
    }

    @Override
    public void setY(float y) {
        actor.setY(y);
    }

    @Override
    public float getX() {
        return actor.getX();
    }

    @Override
    public float getY() {
        return actor.getY();
    }

    @Override
    public void setWidth(float w) {
        actor.setWidth(w);
    }

    @Override
    public void setHeight(float h) {
        actor.setHeight(h);
    }

    @Override
    public float getWidth() {
        return actor.getWidth();
    }

    @Override
    public float getHeight() {
        return actor.getHeight();
    }

    public Actor getActor() {
        return actor;
    }
}
