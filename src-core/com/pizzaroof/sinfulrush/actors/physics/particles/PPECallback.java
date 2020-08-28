package com.pizzaroof.sinfulrush.actors.physics.particles;

import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;

/**classe per ridefinire alcune callback delle particelle fisiche*/
public abstract class PPECallback {
    /**chiamata quando la particella fisica @particle entra in collisione con @actor (inizio collisione)*/
    public abstract void onCollisionWith(PhysicParticle particle, PhysicSpriteActor actor);
}
