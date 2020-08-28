package com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.pizzaroof.sinfulrush.actors.stage.DoubleActActor;
import com.pizzaroof.sinfulrush.actors.ResettableY;
import com.pizzaroof.sinfulrush.util.Utils;

/**decorazione statica, rimossa quando esce dallo schermo*/
public class StaticDecoration extends DoubleActActor implements Pool.Poolable, ResettableY {
    private TextureRegion texture;
    private Animation<TextureRegion> animation;
    private float animationTime;
    private boolean visible;

    private boolean flip, flipVert;

    private Pool<StaticDecoration> myPool;

    public StaticDecoration(TextureRegion texture, boolean visible) {
        this.texture = texture;
        this.visible = visible;
        flip = false;
        flipVert = false;
        animation = null;
        animationTime = 0;
    }

    public StaticDecoration(Array<TextureRegion> regions, float duration, Animation.PlayMode playmode, boolean visible) {
        this.animation = new Animation<>(duration / regions.size, regions, playmode);
        this.visible = visible;
        flip = false;
        flipVert = false;
        texture = null;
        animationTime = 0;
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);
        if(!visible && isInCameraView())
            visible = true;
        if(visible && !isInCameraView())
            remove();

        if(animation != null) {
            animationTime += delta;
            if(animationTime > 1000)
                animationTime -= 1000;
        }
    }

    public void flip(boolean f) {
        flip = f;
    }

    public void flipVert(boolean f) {
        flipVert = f;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        super.draw(batch, alpha);
        if(texture != null)
            drawRegion(batch, texture);
        else
            drawRegion(batch, animation.getKeyFrame(animationTime));
    }

    private void drawRegion(Batch batch, TextureRegion region) {
        float w = flip ? -getWidth() : getWidth(), h = flipVert ? -getHeight() : getHeight();
        float orx = w / 2.f, ory = h / 2.f;
        batch.draw(region, getX() + (flip ? getWidth() : 0), getY() + (flipVert ? getHeight() : 0), orx, ory, w, h, 1, 1, getRotation());
    }

    public void setMyPool(Pool<StaticDecoration> pool) {
        this.myPool = pool;
    }

    public void setAnimationDuration(float dur) {
        if(animation != null)
            animation.setFrameDuration(dur / animation.getKeyFrames().length);
    }

    @Override
    public boolean remove() {
        boolean r = super.remove();
        if(myPool != null)
            myPool.free(this);
        return r;
    }

    /**questo sprite è nella view della camera?*/
    public boolean isInCameraView() {
        if(getStage() == null) return false; //deve essere nello stage per essere nella view di una camera
        Camera camera = getStage().getCamera();

        //controlla se uno dei 4 vertici si vede nella telecamera
        return com.pizzaroof.sinfulrush.util.Utils.pointInCamera(camera, getX(), getY()) || com.pizzaroof.sinfulrush.util.Utils.pointInCamera(camera, getX()+getWidth(), getY())
                || com.pizzaroof.sinfulrush.util.Utils.pointInCamera(camera, getX(), getY()+getHeight()) || Utils.pointInCamera(camera, getX()+getWidth(), getY()+getHeight());
    }

    @Override
    public void resetY(float maxy) {
        setY(getY() - maxy);
    }

    @Override
    public void reset() {
        visible = false;
        animationTime = 0;
    }
}
