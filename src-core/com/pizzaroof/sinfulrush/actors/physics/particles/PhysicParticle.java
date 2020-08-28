package com.pizzaroof.sinfulrush.actors.physics.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Pool;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.util.Utils;

/**singola particella fisica.*/
public class PhysicParticle extends com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor implements Pool.Poolable {

    /**valori di default per densità e restitution*/
    private static final float DEF_DENSITY = 0.1f;

    /**texture da stampare per la particella*/
    private TextureRegion texture;

    /**pool a cui appartiene questa particella*/
    private com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticlePool myPool;

    /**tempo per il quale può restare in vita, in secondi (NB: scorre solamente quando è a contatto con una piattaforma)
     * NB: se negativo, equivale a infinito*/
    private float timeToLive;

    /**tempo passato sulle piattaforme...*/
    private float timePassed;

    /**può ruotare?*/
    private boolean canRotate;

    /**hashcode dell'oggetto con cui si è in collisione*/
    private int objColliding;

    /**memorizziamo vero colore della particella: utile se dobbiamo cambiarlo temporaneamnete (es: rage)*/
    private Color realColor;

    /**callback per eventi particolari (che possono essere ridefiniti dall'esterno)*/
    private PPECallback callback;

    private float radius;

    /** @param radius raggio della particella, in pixel
     * @param pool pool a cui appartiene
     * @param selfCollision deve entrare in collisione con le altre particelle?
     * @param environmentCollision entra in collisione col resto del mondo?*/
    public PhysicParticle(com.pizzaroof.sinfulrush.actors.physics.World2D world, int radius, float restitution, com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticlePool pool, boolean selfCollision, boolean environmentCollision) {
        super(world, BodyDef.BodyType.DynamicBody, DEF_DENSITY, com.pizzaroof.sinfulrush.Constants.SMALL_BUT_EFFECTIVE_FRICTION, restitution, Vector2.Zero, false,
                com.pizzaroof.sinfulrush.Constants.PARTICLES_CATEGORY_BITS,
                environmentCollision ?
                (short)(com.pizzaroof.sinfulrush.util.Utils.maskToNotCollideWith(com.pizzaroof.sinfulrush.Constants.ENEMIES_CATEGORY_BITS, com.pizzaroof.sinfulrush.Constants.PLAYER_CATEGORY_BITS) ^ (selfCollision ? (short)0 : Constants.PARTICLES_CATEGORY_BITS))
                : 0,
                true, com.pizzaroof.sinfulrush.util.Utils.getCircleShape((float)radius / world.getPixelPerMeter()));
        //assumiamo che le particelle sono quadrate, con lato pari al diametro
        /*setWidth(2*radius);
        setHeight(2*radius);
        setDrawingWidth(2*radius);
        setDrawingHeight(2*radius);*/

        this.radius = radius;

        if(!environmentCollision) { //se non ha collisioni con l'environment, ruotiamole a caso, perché altrimenti non lo fanno
            getBody().setTransform(getBody().getPosition(), com.pizzaroof.sinfulrush.util.Utils.randFloat(0, (float)(2 * Math.PI)));
            getBody().setAngularVelocity(Utils.randFloat(-3, 3));
        }

        myPool = pool;
        objColliding = -1;
        timePassed = 0;
        realColor = null;
    }

    public PhysicParticle(com.pizzaroof.sinfulrush.actors.physics.World2D world, int radius, float restitution, com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticlePool pool, boolean selfCollision) {
        this(world, radius, restitution, pool, selfCollision, true);
    }

    public PhysicParticle(World2D world, int radius, float restitution, PhysicParticlePool pool) {
        this(world, radius, restitution, pool, false);
    }

    /**se vuoi che l'attrito entri veramente in gioco, devi chiamare questa*/
    public void applyFriction() {
        allowRotations(false);
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);

        recomputeColor();

        if(objColliding != -1 && timeToLive >= 0) { //è il collisione con una qualche piattaforma...
            timePassed += delta;
            if(timePassed > timeToLive) //muore...
                remove();
            else { //iniziamo a farlo svanire
                //valore in [0, 1] di quanto deve scomparire (1=scomparso)
                float fade = Interpolation.exp5.apply(timePassed / timeToLive); //possiamo usare qualsiasi interpolazione...
                getColor().a = 1 - fade;
            }
        }

        if(canRotate)
            setRotation((float)Math.toDegrees(getBody().getAngle()));

        if(!isInCameraView()) //usciti dalla view... ci rimuoviamo
            remove();
    }

    @Override
    public void onCollisionWith(com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor actor) {
        if(actor instanceof Platform) {
            objColliding = actor.hashCode();
        }

        if(callback != null) callback.onCollisionWith(this, actor);
    }

    @Override
    public void onCollisionEnded(PhysicSpriteActor actor) {
        if(actor.hashCode() == objColliding)
            objColliding = -1;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        Color tmp = batch.getColor();
        batch.setColor(getColor()); //imposta colore del batch col colore della particella
        drawFrame(texture, batch);
        batch.setColor(tmp); //rimetti colore com'era prima
    }

    protected void recomputeColor() {
        //decidiamo quale colore dare alla particella, in base al fatto se c'è rage o no
        if(isOnStage() && getStage() instanceof ShaderStage && ((ShaderStage)getStage()).isRageModeOn()) { //c'è rage, mettiamola rossa
            if(realColor == null) realColor = getColor().cpy();
            setColor(Color.RED);
        }
        else
            if(realColor != null) //niente rage: mettila normale
                setColor(realColor);
    }

    @Override
    public boolean remove() {
        if(!isOnStage()) return false;
        getParent().removeActor(this);
        if(myPool != null)
            myPool.free(this);
        return true;
    }

    /**chiamarlo per reinizializzare la particella dopo averla estratta da una pool (posizione in pixel).
     * @param timeToLive quanti secondi può vivere la particella a contatto con la piattaforma (<0 = infinito)*/
    public void init(Vector2 initPos, TextureRegion region, float timeToLive, boolean canRotate) {
        //body.setTransform(initPos, 0);
        //body.setActive(true);
        super.init(initPos);
        this.texture = region;
        this.timeToLive = timeToLive;
        this.canRotate = canRotate;

        //assumiamo particelle messe in orizzontale
        float w = 2 * radius;
        float h = w * ((float)texture.getRegionHeight() / texture.getRegionWidth());
        setWidth(w);
        setHeight(h);
        setDrawingWidth(w);
        setDrawingHeight(h);

        recomputePosition();
    }

    public void setCallback(PPECallback callback) {
        this.callback = callback;
    }

    /**chiamato quando viene chiamato free() per questo elemento*/
    @Override
    public void reset() {
        //metti il body dove non si vede e mettilo non attivo
        /*body.setTransform(0, 0, 0);
        body.setLinearVelocity(0, 0);
        body.setActive(false);*/
        super.reset();
        objColliding = -1;
        timePassed = 0;
        callback = null;
        realColor = null;
    }
}
