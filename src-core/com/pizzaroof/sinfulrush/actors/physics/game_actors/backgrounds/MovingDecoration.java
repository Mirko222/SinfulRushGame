package com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class MovingDecoration extends StaticDecoration {

    private Vector2 direction;
    private float speed;

    public MovingDecoration(TextureRegion texture, boolean visible) {
        super(texture, visible);
    }

    public MovingDecoration(Array<TextureRegion> regions, float duration, Animation.PlayMode playmode, boolean visible) {
        super(regions, duration, playmode, visible);
    }

    public void setDirection(Vector2 direction) {
        this.direction = direction;
        setRotation(direction.angle());
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);
        setX(getX() + direction.x * speed * delta);
        setY(getY() + direction.y * speed * delta);
    }
}
