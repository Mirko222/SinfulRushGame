package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.math.Vector2;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.stage.TimescaleStage;
import com.pizzaroof.sinfulrush.util.pools.Pools;

/**powerball che segue un bersaglio*/
public class FollowingPowerball extends Powerball {

    /**target della powerball*/
    protected PhysicSpriteActor target;

    protected boolean hitted;

    /**direzione di backup in caso il target diventi null*/
    protected Vector2 backupDirection;

    /**crea powerball
     * @param spawnPoint punto da dove inizia la traiettoria
     * @param speed velocità della powerball
     * @param effect effetto particellare della powerball
     * @param power potere della powerball*/
    public FollowingPowerball(World2D world, String effect, Vector2 spawnPoint, float speed, int power, float radius, PhysicSpriteActor target, Vector2 backupDirection, com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor color) {
        super(world, effect, spawnPoint, speed, power, radius, null, color);
        this.target = target;
        hitted = false;
        setBackupDirection(backupDirection);
        recomputeVelocity();
    }

    /**chiamare init se usi questa!!!*/
    public FollowingPowerball(World2D world, float radius) {
        super(world, radius);
    }

    @Override
    public void actSkipTolerant(float delta) {
        if(!hitted)
            recomputeVelocity(); //ricalcola velocità della powerball
        super.actSkipTolerant(delta);
    }

    /**ricalcola velocità verso il target*/
    protected void recomputeVelocity() {
        if(target != null && target.isOnStage() && target.getBody() != null) {
            Vector2 direction = target.getBody().getPosition().cpy().sub(getBody().getPosition()).nor(); //direzione verso il target
            backupDirection = direction; //aggiorna backup con ultima posizione

            if (doesTimescale())
                getBody().setLinearVelocity(direction.x * speed, direction.y * speed);
            else if (isOnStage()) {
                float timemul = ((TimescaleStage) getStage()).getTimeMultiplier();
                getBody().setLinearVelocity(direction.x * speed / timemul, direction.y * speed / timemul);
            }

            setSpriterRotation(direction.angle());
        }
        else
            getBody().setLinearVelocity(backupDirection.x * speed, backupDirection.y * speed);
    }

    /**chiamata quando entra in collisione con actor*/
    @Override
    public void onCollisionWith(PhysicSpriteActor actor) {
        if(target == null || hitted) return;

        if(actor.hashCode() == target.hashCode()) { //entrato in collisione con il target
            hitted = true;
            super.onCollisionWith(actor);
        }
    }

    public void setBackupDirection(Vector2 backupDirection) {
        this.backupDirection = backupDirection;
    }

    public void init(String effect, Vector2 spawnPoint, float speed, int power, PhysicSpriteActor target, com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor color) {
        init(effect, spawnPoint, speed, power, target, true, color);
    }

    public void init(String effect, Vector2 spawnPoint, float speed, int power, PhysicSpriteActor target, boolean useParticles, Pools.PEffectColor color) {
        super.init(effect, spawnPoint, speed, power, null, useParticles, color);
        hitted = false;
        this.target = target;
        recomputeVelocity();
        recomputePosition();
    }
}
