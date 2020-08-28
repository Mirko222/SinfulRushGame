package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.pizzaroof.sinfulrush.actors.ScoreButton;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.util.pools.Pools;

/**powerball che colpisce un bottone... usato essenzialmente per i malus che tolgono score*/
public class ButtonPowerball extends Powerball {
    private Button targetBtn;
    private float radius;
    private static final float MIN_DISTANCE = 80;

    private boolean exploded;

    public ButtonPowerball(World2D world, String effect, Vector2 spawnPoint, float speed, int power, float radius, Vector2 direction, com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor color) {
        super(world, effect, spawnPoint, speed, power, radius, direction, color);
        this.radius = radius * world.getPixelPerMeter();
        exploded = false;
    }

    public void setTargetBtn(Button btn) {
        this.targetBtn = btn;
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);

        if(!exploded) {
            if (targetBtn != null) {
                Vector2 dir = new Vector2(targetBtn.getX() + targetBtn.getWidth() * 0.5f - getX() - radius,
                        targetBtn.getY() + targetBtn.getHeight() * 0.5f - getY() - radius);
                dir.nor();
                dir.x *= speed;
                dir.y *= speed;
                body.setLinearVelocity(dir);
            }
        } else {
            if(targetBtn != null) {
                Vector2 btnPos = new Vector2((targetBtn.getX() + targetBtn.getWidth() * 0.5f) / world.getPixelPerMeter(), (targetBtn.getY() + targetBtn.getHeight() * 0.5f) / world.getPixelPerMeter());
                instantSetPosition(btnPos);
            }
        }
    }

    @Override
    public void processCollisions() {
        collidedWith.clear();
        if(targetBtn != null && getBody() != null && !exploded) {
            if (centerPosition().dst2(new Vector2(targetBtn.getX() + targetBtn.getWidth() * 0.5f, targetBtn.getY() + targetBtn.getHeight() * 0.5f)) <= MIN_DISTANCE * MIN_DISTANCE) {
                //collisione
                onCollisionWithButton();
            }
        }
    }

    @Override
    public boolean shouldBeTintedWhiteDuringRage(Pools.PEffectColor color) {
        return true;
    }

    public void onCollisionWithButton() {
        explode(null);
        exploded = true;
        if(soundManager != null)
            soundManager.enemyExplosion();
        getBody().setLinearVelocity(0, 0);
        if(targetBtn instanceof ScoreButton)
            ((ScoreButton) targetBtn).onCollisionWith(this);
    }
}
