package com.pizzaroof.sinfulrush.actors.physics.particles;

import com.badlogic.gdx.utils.Pool;
import com.pizzaroof.sinfulrush.actors.physics.World2D;

/**pool per particelle fisiche*/
public class PhysicParticlePool extends Pool<PhysicParticle> {

    /**mondo fisico*/
    private com.pizzaroof.sinfulrush.actors.physics.World2D world;

    /**raggio per la prossima particella da creare*/
    private int nextRadius;

    /**restitution della prossima particella*/
    private float nextRestitution;

    /**deve entrare in collisione con le altre particelle?*/
    private boolean selfCollision;

    /**entra in collisione con l'ambiente?*/
    private boolean environmentCollision;

    public PhysicParticlePool(World2D world, boolean selfCollision, boolean environmentCollision) {
        this.world = world;
        this.selfCollision = selfCollision;
        this.environmentCollision = environmentCollision;
    }

    /**setta raggio per la prossima piattaforma*/
    public void setNextRadius(int radius) {
        nextRadius = radius;
    }

    /**restitution per la prossima particella*/
    public void setNextRestitution(float restitution) {
        nextRestitution = restitution;
    }

    @Override
    protected PhysicParticle newObject() {
        return new PhysicParticle(world, nextRadius, nextRestitution,this, selfCollision, environmentCollision);
    }
}
