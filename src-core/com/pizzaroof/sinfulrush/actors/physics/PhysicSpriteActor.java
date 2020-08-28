package com.pizzaroof.sinfulrush.actors.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.pizzaroof.sinfulrush.actors.SpriterAnimActor;

/**sprite actor soggetto a fisica*/
public class PhysicSpriteActor extends SpriterAnimActor {

    /**mondo a cui appartiene*/
    protected World2D world;

    /**body fisico*/
    protected Body body;

    /**ci salviamo pixel per metro (si, sta anche dentro @world, ma li usiamo spesso quindi è comodo averli a parte)*/
    protected float pixelPerMeter;

    /**bit che indicano la categoria dell'oggetto*/
    protected short categoryBits;
    /**bit che indicano le categorie con cui l'oggetto collide*/
    protected short maskBits;

    protected float density, friction, restitution;

    /**
     @param world mondo in cui l'entità fisica vive
     @param bodyType tipo del body: {static, kinematic, dynamic}
     @param shapes forme del body (square, circle, convex polygon, etc.) (può averne più di una perchè può essere complesso)
     @param density densità del body (mass / volume)
     @param friction intensità della forza applicata "strisciando" con il body (è una sorta di attrito)
     @param restitution coefficiente di restituzione (velocity_after_hit / velocity_before_hit): in sostanza è un indicatore di quanta velocità si perde (alta restitution -> alta capacità di "rimbalzare") i valori hanno senso in [0, 1]
     @param initPosition posizione iniziale nel mondo fisico (in metri)
     @param isSensor se vero, l'oggetto non ha effetto sugli scontri fisici (ma può essere comunque usato per verificare collisioni)
     @param categoryBits bitmask che indica la categoria dell'oggetto
     @param maskBits bitmask che indica le categorie che collidono con l'oggetto
     @param disposeShapes booleano che indica se vogliamo gli shape disposti qui o no (metti falso se vuoi riutilizzarli)*/
    public PhysicSpriteActor(World2D world, BodyDef.BodyType bodyType, float density, float friction, float restitution, Vector2 initPosition, boolean isSensor, short categoryBits, short maskBits, boolean disposeShapes, Shape...shapes) {
        super();
        this.world = world;
        this.pixelPerMeter = world.getPixelPerMeter();
        this.categoryBits = categoryBits;
        this.maskBits = maskBits;
        this.density = density;
        this.friction = friction;
        this.restitution = restitution;

        //crea body vero e proprio
        buildBody(bodyType, initPosition, isSensor, disposeShapes, shapes);
        recomputePosition(); //ricalcola posizione per lo sprite dal body

        if(spriterPlayer != null) { //aggiorna spriter player in modo che la posizione venga realmente ricalcolata
            spriterPlayer.speed = 0; //con velocità 0 altrimenti mandiamo troppo avanti l'animazione
            spriterPlayer.update();
        }
    }

    /**shape disposti qua*/
    public PhysicSpriteActor(World2D world, BodyDef.BodyType bodyType, float density, float friction, float restitution, Vector2 initPosition, boolean isSensor, short categoryBits, short maskBits, Shape...shapes) {
        this(world, bodyType, density, friction, restitution, initPosition, isSensor, categoryBits, maskBits, true, shapes);
    }

    /**le bitmask vengono messe in modo che tutti collidono con tutti*/
    public PhysicSpriteActor(World2D world, BodyDef.BodyType bodyType, float density, float friction, float restitution, Vector2 initPosition, boolean isSensor, Shape...shapes) {
        this(world, bodyType, density, friction, restitution, initPosition, isSensor, (short)-1, (short)-1, shapes);
    }

    /**non è un sensore, e tutti collidono con tutti*/
    public PhysicSpriteActor(World2D world, BodyDef.BodyType bodyType, float density, float friction, float restitution, Vector2 initPosition, Shape...shapes) {
        this(world, bodyType, density, friction, restitution, initPosition, false, (short)-1, (short)-1, shapes);
    }

    /**posizione iniziale (0, 0), non è un sensore, tutti collidono con tutti*/
    public PhysicSpriteActor(World2D world, BodyDef.BodyType bodyType, float density, float friction, float restitution, Shape...shapes){
        this(world, bodyType, density, friction, restitution, Vector2.Zero, shapes);
    }

    /**@param r true: il corpo può ruotare, se false no*/
    public void allowRotations(boolean r) {
        body.setFixedRotation(!r);
    }

    @Override
    public void actFrameDependent(float delta) {
        //è frame dependent solo perché vogliamo aggiornare la posizione dopo il mondo fisico, e quindi
        //se la mettessimo in skipTolerant, verrebbe eseguito prima
        super.actFrameDependent(delta);
        recomputePosition(); //ricalcola posizione
    }

    /**ricalcola posizione dello sprite basandosi sul body*/
    public void recomputePosition() {
        if(body != null) {
            Vector2 pos = body.getPosition(); //posizione del centro del body (nel mondo, in metri)
            pos.x *= pixelPerMeter;
            pos.y *= pixelPerMeter;
            setPositionFromCenter(pos.x, pos.y); //traduci in posizione per lo sprite (in pixel e serve l'angolo in basso a sx)
        }
    }

    /**restituisce il body*/
    public Body getBody() {
        return body;
    }

    /**restituisce larghezza in metri (getWidth() è la larghezza in pixel)*/
    public float getMetersWidth() {
        return getWidth() / pixelPerMeter;
    }

    /**restituisce l'altezza in metri (getHeight() è l'altezza in pixel)*/
    public float getMetersHeight() {
        return getHeight() / pixelPerMeter;
    }

    /**rimuove sprite dallo stage e dal mondo fisico (in realtà verrà rimosso alla successiva iterazione)*/
    @Override
    public boolean remove() {
        if(isOnStage()) { //se non è sullo stage non può essere rimosso
            boolean r = super.remove(); //rimuovi dallo stage
            if(body != null) //il corpo potrebbe già essere stato rimosso per qualche motivo
                world.addBodyToRemove(body); //indica che va rimosso dal mondo
            return r;
        }
        return false;
    }

    /**come remove(), ma il body viene rimosso esattamente quando si chiama la funzione, quindi DEVE ESSERE CHIAMATA FUORI DA world.step*/
    public void removeNow() {
        if(isOnStage()) {
            super.remove();
            removeBodyNow();
        }
    }

    /**distrugge il body fisico sulla chiamata (solo il body fisico), ASSOLUTAMENTE DA NON CHIAMARE IN world.step*/
    public void removeBodyNow() {
        if(body != null) {
            world.getBox2DWorld().destroyBody(body);
            body = null;
        }
    }

    public void removeBody() {
        if(body != null)
            world.addBodyToRemove(body);
    }

    /**metodo per creare effettivamente il body*/
    protected void buildBody(BodyDef.BodyType bodyType, Vector2 initPosition, boolean isSensor, boolean disposeShapes, Shape...shapes) {
        BodyDef bdef = new BodyDef(); //body definition
        bdef.type = bodyType;
        bdef.position.set(initPosition);
        body = world.getBox2DWorld().createBody(bdef); //crea body con box2d world (aggiunge direttamente il body al world)
        for(Shape shape : shapes) { //creiamo tutte le fixture... una per ogni shape
            FixtureDef fdef = new FixtureDef(); //definiamo proprietà del body
            fdef.shape = shape;
            fdef.density = density;
            fdef.friction = friction;
            fdef.restitution = restitution;
            fdef.isSensor = isSensor;
            fdef.filter.categoryBits = categoryBits;
            fdef.filter.maskBits = maskBits;
            body.createFixture(fdef);
        }

        body.setUserData(this); //associamo tutto l'attore al corpo (quando entra in collisione, dal corpo possiamo riottenere tutto lo sprite)
        if(disposeShapes) //abbiamo scelto di disporre gli shape qui
            for(Shape shape : shapes)
                shape.dispose(); //disponiamo subito gli shape (non serve farlo da altre parti; ma gli shape non si possono riciclare)
    }

    /**callback per quando entra in collisione con @actor*/
    public void onCollisionWith(PhysicSpriteActor actor) {
    }

    /**callback per quando termina la collisione con @actor*/
    public void onCollisionEnded(PhysicSpriteActor actor) {
    }

    /**mette il body a null (probabilmente usato per non rischiare strani bug da World2D)*/
    public void nullifyBody() {
        body = null;
    }

    public Vector2 getVelocity() {
        return body.getLinearVelocity();
    }

    public float getBodySpeed() {
        return body.getLinearVelocity().dst(Vector2.Zero);
    }

    /**hashcode dello sprite (creato usando quello che viene associato al body)*/
    @Override
    public int hashCode() {
        if(body == null)
            return -1;
        return body.hashCode();
    }

    @Override
    public void resetY(float maxy) {
        if(body != null) {
            Vector2 wpos = new Vector2(getBody().getPosition().x, getBody().getPosition().y - maxy / world.getPixelPerMeter());
            //Vector2 vel = getBody().getLinearVelocity().cpy();
            body.setTransform(wpos, body.getAngle());
            //body.setLinearVelocity(vel);
            recomputePosition();
        } else {
            //non abbiamo più il body, ma siamo ancora sullo stage... a questo punto resettiamo la y dell'actor
            setY(getY() - maxy);
        }
    }

    @Override
    public void instantSetPosition(Vector2 position) {
        body.setTransform(position, getBody().getAngle());
        recomputePosition();
        recomputeSpriterScale();
    }

    @Override
    public void reset() {
        super.reset();
        body.setTransform(0, 0,0);
        body.setLinearVelocity(0, 0);
        body.setActive(false);
    }

    /**inizializza oggetto preso dalla pool.
     * @param initPosition posizione per body (quindi in metri)*/
    public void init(Vector2 initPosition) {
        body.setTransform(initPosition, 0);
        body.setActive(true);
        recomputePosition();
    }

    @Override
    public void removeAndFree() {
        super.remove(); //non vogliamo rimuovere anche il body...
        if(myPool != null)
            myPool.free(this);
    }
}

