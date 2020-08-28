package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.math.Vector2;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.stage.TimescaleStage;

/**powerball in grado di gestire veramente il timescale*/
public class TimescaleWisePowerball extends Powerball {

    public TimescaleWisePowerball(World2D world, float radius) {
        super(world, radius);
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);
        float tm = isOnStage() ? ((TimescaleStage) getStage()).getTimeMultiplier() : 1;
        if(getBody() != null && getBodySpeed() > com.pizzaroof.sinfulrush.Constants.EPS && !doesTimescale() && Math.abs(tm - 1) > com.pizzaroof.sinfulrush.Constants.EPS) {
            Vector2 center = centerPosition();
            center.x += (direction.x * speed * delta * world.getPixelPerMeter() / tm);
            center.y += (direction.y * speed * delta * world.getPixelPerMeter() / tm);
            setPositionFromCenter(center);
        }
    }

    @Override
    public void recomputePosition() {
        float tm = isOnStage() ? ((TimescaleStage) getStage()).getTimeMultiplier() : 1;
        if (doesTimescale() || Math.abs(tm - 1) <= Constants.EPS)
            super.recomputePosition();
    }
}
