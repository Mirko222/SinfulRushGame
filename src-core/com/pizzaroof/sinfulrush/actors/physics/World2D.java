package com.pizzaroof.sinfulrush.actors.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.pizzaroof.sinfulrush.actors.stage.DoubleActActor;

import java.util.HashSet;

/**mondo fisico*/
public class World2D extends DoubleActActor implements Disposable {
    public static final float FIXED_DELTA_TIME = 1.0f / 60.0f; //time rate fisso a 60fps
    public static final float MAX_DELTA_TIME = 0.25f; //massimo tempo tra due iterazioni (evita grossi problemi di lag)
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 2;

    protected World world; //world box2d

    protected float timeAccumulator; //tempo tra uno step fisico e l'altro

    protected float pixelPerMeter; //quanti pixel in un metro? NB: gli oggetti fisici vanno misurati in METRI, ma deve esistere una conversione metro/pixel

    /**set dei bodies da rimuovere*/
    protected HashSet<Body> bodiesToRemove;

    /**inizializza con vettore gravità e numero di pixel per metro*/
    public World2D(float pixelPerMeter, Vector2 gravity) {
        Box2D.init(); //init box2d

        world = new World(gravity, true); //true per permettere agli oggetti di "dormire" (salva tempo cpu)

        timeAccumulator = 0;
        this.pixelPerMeter = pixelPerMeter;
        bodiesToRemove = new HashSet<>();

        world.setContactListener(contactListener);
    }

    @Override
    public void actFrameDependent(float delta) {
        removeBodies(); //rimuovi bodies
        variableRatePhysicsStep(delta); //aggiorna la fisica
    }

    /**simulate physics for deltaTime seconds*/
    protected void variableRatePhysicsStep(float deltaTime) {
        deltaTime = Math.min(deltaTime, MAX_DELTA_TIME); //tempo tra gli ultimi 2 frame (non troppo, altrimenti scatta)

        timeAccumulator += deltaTime; //accumula il tempo da simulare

        while(timeAccumulator >= FIXED_DELTA_TIME) { //simula tante volte fixed_delta_time invece di simulare tutto l'accumulatore
            fixedPhysicsStep();
            timeAccumulator -= FIXED_DELTA_TIME;
        }
        //può rimanere qualcosa dentro time accumulator da simulare... (se fixed_delta_time è piccolo, il ritardo sarà piccolo)
    }

    /**simula fisica per un intervallo fissato*/
    protected void fixedPhysicsStep() {
        world.step(FIXED_DELTA_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
    }

    public void addBodyToRemove(Body body) {
        bodiesToRemove.add(body);
    }

    /**rimuove bodies di @bodiesToRemove*/
    protected void removeBodies() {
        for(Body body : bodiesToRemove)  //itera
            if(body != null) {
                com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor p = (com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor) body.getUserData();
                world.destroyBody(body); //distruggi box2d body
                p.nullifyBody(); //metti a null il body nello sprite actor (a livello pratico non serve, ma aiuta a individuare bug)
            }
        bodiesToRemove.clear();
    }

    public float getPixelPerMeter() {
        return pixelPerMeter;
    }

    /**pulisce world da ogni forza e body (in maniera distruttiva)*/
    public void clear() {
        world.clearForces();
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        for(Body b : bodies)
            world.destroyBody(b);
        bodiesToRemove.clear();
    }

    /**instanza box2d del mondo*/
    public World getBox2DWorld() {
        return world;
    }

    /**vettore gravità*/
    public Vector2 getGravity() {
        return world.getGravity();
    }

    /**contact listener: gestiamo qui tutte le collisioni tra oggetti fisici*/
    protected ContactListener contactListener = new ContactListener() {
        @Override
        public void beginContact(Contact contact) {
            com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor actor1 = (com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor)contact.getFixtureA().getBody().getUserData(); //prendi gli sprite che sono entrati in collisione
            com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor actor2 = (com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor)contact.getFixtureB().getBody().getUserData();
            if(actor1 == null || actor2 == null)
                return;

            /*if(actor1 instanceof Player || actor1 instanceof Powerball || actor1 instanceof Enemy) //un po' di ottimizzazione... sappiamo chi sono gli elementi che gestiscono veramente le collisioni
                actor1.onCollisionWith(actor2); //delega alle callback
            if(actor2 instanceof Player || actor2 instanceof Powerball || actor2 instanceof Enemy) //un po' di ottimizzazione... sappiamo chi sono gli elementi che gestiscono veramente le collisioni
                actor2.onCollisionWith(actor1);*/
            actor1.onCollisionWith(actor2);
            actor2.onCollisionWith(actor1);
        }

        @Override
        public void endContact(Contact contact) {
            com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor actor1 = (com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor)contact.getFixtureA().getBody().getUserData(); //prendi gli sprite che non sono più in collisione
            com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor actor2 = (PhysicSpriteActor)contact.getFixtureB().getBody().getUserData();
            if(actor1 == null || actor2 == null)
                return;

            actor1.onCollisionEnded(actor2);
            actor2.onCollisionEnded(actor1);
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    };

    @Override
    public void dispose() {
        world.dispose();
    }
}
