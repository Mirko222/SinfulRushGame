package com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.actors.stage.TimescaleStage;
import com.pizzaroof.sinfulrush.attacks.FollowingPowerball;
import com.pizzaroof.sinfulrush.attacks.Powerball;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.IceBlock;
import com.pizzaroof.sinfulrush.actors.OneShotSprite;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.io.BufferedReader;
import java.io.IOException;

/**nemico volante, fuori dal contesto delle piattaforme, che lancia attacchi a distanza
 * (per danneggiare direttamente il player, o per altri scopi).
 * le animazioni sono: idle_flying | hurt | dying_falling | attack
 * Sulla quarta riga del file ci sono informazioni riguardo l'attacco:
 * potere | tempo di ricarica | shoot_particle_effect | shoot_speed | ball radius (in metri) | min max atkdist | max max atkdist | path esplosione (null per nessuna) | nome colore effetto
 * min max atkdist = (minima) massima distanza per attaccare (in metri)
 * max max atkdist = (massima) massima distanza per attaccare (in metri) (la vera massima distanza per attaccare verrà calcolata prendendo un valore a caso tra i due forniti*/
public class FlyingSniperEnemy extends Enemy {

    private static final float MIN_WIND_STRENGHT = 2.3f, MAX_WIND_STRENGHT = 3.2f;

    protected final static float FEPS = com.pizzaroof.sinfulrush.Constants.EPS * 100;

    /**minimo spazio di fluttuazione*/
    private final static float MIN_FLOATING_SPACE = 0.1f;
    /**massimo spazio di fluttuazione*/
    private final static float MAX_FLOATING_SPACE = 0.25f;

    /**massa di default per nemici volanti*/
    private final static float DEF_MASS = 0.1f;
    /**restitution di default per nemici volanti*/
    private final static float DEF_RESTITUTION = 0.2f;

    /**id delle animazioni*/
    private int FLYING_ANIM, HURT_ANIM, FALLING_ANIM, ATTACK_ANIM;

    /**è apparso nello schermo? (è un cecchino... ma se l'utente non può vederlo non vale)*/
    protected boolean appearedOnScreen;

    /**con che velocità ci muoviamo verso l'alto e verso il basso?*/
    protected float floatingSpeed;
    /**quanto spazio percorriamo al massimo mentre simuliamo di "flutturare" (metà del range sarà verso il basso e metà verso l'alto, il punto iniziale dato è quello centrale)*/
    protected float floatingSpace;
    /**direzione verso cui stiamo fluttuando*/
    protected int floatingDirection;
    /**y iniziale del body*/
    protected float initBodyY;

    /**manteniamo gli shape da usare per la morte (sia normali che flippati)*/
    protected Shape deadShapes[], deadFlippedShapes[];

    /**tempo di ricarica dell'attacco*/
    protected float attackRecTime;
    /**potere dell'attacco*/
    protected int attackPower;
    /**tempo passato dall'ultimo attacco*/
    protected float timePassedSinceLastAttack;

    /**effetto particellare per lo shoot*/
    protected String fireEffect;
    /**effetto esplosione*/
    protected String explosionEffect;
    /**colore per effetti*/
    protected com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor effectColor;

    /**speed dello sparo*/
    protected float fireSpeed;
    /**raggio della sfera lanciata in metri*/
    protected float ballRadiusM;
    /**massima distanza in metri per attaccare*/
    protected float maximumYDistanceToAttack;

    /**booleano che ci dice se abbiamo già sparato durante una certa animazione
     * (serve per non sparare più volte in una sola animazione)*/
    protected boolean alreadyShot;

    /**target del nemico*/
    protected com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor target;
    /**una destinazione di backup a cui lanciare la sfera: è utile se il bersaglio che avevamo
     * viene distrutto e non ci sono altre cose a cui sparare: invece di fare l'animazione di attacco, spariamo
     * comunque alla posizione dove stava il nemico (è una cosa inutile, ma meglio che fare l'animazione a vuoto)*/
    protected Vector2 backupTarget;

    /**hashcode della piattaforma con cui siamo in collision, dopo morte*/
    protected int collidingPlatformCode;

    /**sta volando via?*/
    protected boolean flyingAway;
    /**ritardo per volare via*/
    protected float flyAwayDelay;

    /**numero di oggetti con cui è in collisione.
     * Ci interessa perché vogliamo farlo sparire solo quando è in collisione con qualcosa*/
    private int numCollidingObjects;

    /**@param backgroundGroup gruppo dove mettere le cose di background
     * @param effectGroup gruppo dove mettere gli effetti che si creano (tipo sfere di energia)*/
    public FlyingSniperEnemy(String directory, SoundManager soundManager, AssetManager asset, Stage stage, com.pizzaroof.sinfulrush.actors.physics.World2D world, Vector2 initPosition, Group backgroundGroup, Group effectGroup, Shape... shapes) {
        super(world, soundManager, BodyDef.BodyType.KinematicBody, DEF_MASS, com.pizzaroof.sinfulrush.Constants.SMALL_BUT_EFFECTIVE_FRICTION, DEF_RESTITUTION, initPosition, directory, asset, stage, effectGroup, backgroundGroup, shapes);
        setSpriterAnimation(FLYING_ANIM);
        appearedOnScreen = false;
        alreadyShot = false;
        numCollidingObjects = 0;

        RandomXS128 rand = new RandomXS128();
        collidingPlatformCode = -1;

        //read attacks stats
        try {
            BufferedReader reader = com.pizzaroof.sinfulrush.util.Utils.getInternalReader(com.pizzaroof.sinfulrush.util.Utils.enemyInfoPath(directory));
            for(int i=0; i<4; i++) reader.readLine(); //scarta prime 3 righe
            String [] strs = reader.readLine().split(" ");
            attackPower = Integer.parseInt(strs[0]); //leggi attack power
            attackRecTime = Float.parseFloat(strs[1]); //leggi tempo ricarica
            fireEffect = strs[2]; //legge nome effetto
            fireSpeed = Float.parseFloat(strs[3]); //legge velocità della sfera
            ballRadiusM = Float.parseFloat(strs[4]); //raggio della sfera
            maximumYDistanceToAttack = Float.parseFloat(strs[5]); //distanza massima a cui può attaccare (al minimo)
            float tmp = Float.parseFloat(strs[6]) - maximumYDistanceToAttack;
            maximumYDistanceToAttack += (tmp * rand.nextFloat()); //valore a caso tra quelli forniti
            explosionEffect = strs[7].equals("null") ? null : strs[7];
            effectColor = Pools.PEffectColor.valueOf(strs[8]);

            timePassedSinceLastAttack = attackRecTime+1;
        }catch(IOException e) { //non dovrebbe succedere
            e.printStackTrace();
        }

        //-----floating stuff-----
        //introduciamo un po' di casualità nelle fluttuazioni... in modo che se ce ne sono tanti sullo schermo non sembrano sincronizzati
        floatingSpace = getHeight() * 0.3f / pixelPerMeter; //uguale a 30% dell'altezza (magari si può scegliere con criteri migliori)
        floatingSpeed = rand.nextFloat() * (MAX_FLOATING_SPACE - MIN_FLOATING_SPACE) + MIN_FLOATING_SPACE; //[MIN_FLOATING_SPACE, MAX_FLOATING_SPACE] (a occhio... magari si può fare con criteri migliori)
        initBodyY = getBody().getPosition().y;
        floatingDirection = -1;

        flyingAway = false;
    }

    @Override
    public void actFrameDependent(float delta) {
        //frame dependent perché fa controlli su distanze
        super.actFrameDependent(delta);

        updateBonusStatus(delta);

        if(getHp() > 0) { //se è vivo...
            if(isInCameraView()) {
                //fluttuazione...
                //in base alla direzione vedi qual è la destinazione
                float floatingDestination = floatingDirection * floatingSpace * 0.5f + initBodyY;
                //muoviamoci su e giu, di poco, per dare la sensazione di volo
                float eps = FEPS * ((TimescaleStage)getStage()).getTimeMultiplier();

                if (Math.abs(floatingDestination - getBody().getPosition().y) <= eps) {//arrivato alla destinazione
                    floatingDirection *= -1;
                    floatingDestination = floatingDirection * floatingSpace * 0.5f + initBodyY;
                }
                int dir = floatingDestination - getBody().getPosition().y >= 0 ? 1 : -1; //direzione verso cui muoverti
                getBody().setLinearVelocity(getBody().getLinearVelocity().x, dir * floatingSpeed); //muoviti verso la destinazione

                //ricalcola il target a cui sparare
                computeTarget();
                if (target != null && target.getBody() != null) {
                    //guardo verso il target
                    //sto guardando a destra, ma il target sta "parecchio" più a sinistra (non voglio essere troppo sensibile ai cambi di direzione)
                    if (getHorDirection().equals(SpriteActor.HorDirection.RIGHT) && target.getBody().getPosition().x * pixelPerMeter + target.getWidth() / 2 < getBody().getPosition().x * pixelPerMeter)
                        setHorDirection(SpriteActor.HorDirection.LEFT);
                    //analogo a sopra
                    if (getHorDirection().equals(SpriteActor.HorDirection.LEFT) && target.getBody().getPosition().x * pixelPerMeter - target.getWidth() / 2 > getBody().getPosition().x * pixelPerMeter)
                        setHorDirection(SpriteActor.HorDirection.RIGHT);

                    //logica relativa agli attacchi
                    float distanceFromTarget = Math.abs(getBody().getPosition().y - target.getBody().getPosition().y); //distanza solo su y
                    if (centerInView() && distanceFromTarget <= maxDistToAttack()) { //quando almeno il centro sta nella view (e stai abbastanza vicino) può attaccare
                        if (timePassedSinceLastAttack >= getNextPowerballTimeToAttack()) { //passato abbastanza tempo: può attaccare
                            backupTarget = target.getBody().getPosition().cpy(); //prima di attaccare ci salviamo un backup (in caso il target sparisca)
                            attack();
                        }
                    }
                }
            }

            if(isFrozen()) //congelato... il nemico volante muore
                takeDamageUnchecked(Constants.INFTY_HP, Mission.BonusType.ICE);

            if (getCurrentSpriterAnimation() != ATTACK_ANIM) //non sta attaccando, allora sta passando tempo...
                timePassedSinceLastAttack += delta;
        }
        else {
            float bodySpeed = getBodySpeed();
            if(body != null && bodySpeed <= com.pizzaroof.sinfulrush.Constants.EPS && numCollidingObjects > 0) //body quasi fermo (e stiamo in contatto con qualcosa, che quindi ha provocato la fermata)
                disappear();
        }

        if(!isAppearedOnScreen() && isInCameraView()) //controllo se è apparso...
            appearedOnScreen = true;

        if(isAppearedOnScreen() && !isInCameraView() && (!isFreezing() || !iceBlock.isInCameraView())) //era apparso e ora non c'è più... scomparso, togliamolo per non accumulare spazzatura
            remove();
    }

    /**attacca... (in realtà inizia ad attaccare: l'attacco vero parte in shoot() )*/
    protected void attack() {
        setSpriterAnimation(ATTACK_ANIM);
        timePassedSinceLastAttack = 0;
    }

    /**spara l'attacco*/
    protected void shoot() {
        Vector2 spawn = getBody().getPosition().cpy(); //per lo spawn, ci mettiamo un po' fuori dal nemico che sta sparando, in base alla direzione verso cui dobbiamo sparare
        spawn.y -= getMetersHeight() * 0.25f; //un po' più in basso del centro

        //prendi posizione_target - posizione_nemico e normalizza
        Vector2 backupDirection = backupTarget.cpy().sub(getBody().getPosition()).nor();
        Vector2 shootingDirection = (target != null && target.getBody() != null) ? target.getBody().getPosition().cpy().sub(getBody().getPosition()).nor() : backupDirection;
        spawn.x += getNextPowerballRadius() * shootingDirection.x; //ci spostiamo un po' sull'esterno

        Powerball ball = new FollowingPowerball(world, getNextPowerballEffect(), spawn, getNextPowerballSpeed(), getNextPowerballPower(), getNextPowerballRadius(), target, backupDirection, effectColor);
        ball.setSoundManager(soundManager);
        ball.setExplosionEffect(explosionEffect);

        effectGroup.addActor(ball);
    }

    /**effetto per la prossima fireball*/
    protected String getNextPowerballEffect() {
        return fireEffect;
    }

    /**velocità per la prossima powerball*/
    protected float getNextPowerballSpeed() {
        return fireSpeed;
    }

    /**raggio per la prossima powerball*/
    protected float getNextPowerballRadius() {
        return ballRadiusM;
    }

    /**potere della prossima powerball*/
    protected int getNextPowerballPower() {
        return attackPower;
    }

    /**quanto devo aspettare per sparare la prossima sfera?*/
    protected float getNextPowerballTimeToAttack() {
        return attackRecTime;
    }

    /**qual è la massima distanza a cui si può attaccare?*/
    protected float maxDistToAttack() {
        return maximumYDistanceToAttack;
    }

    //callback quando muore
    @Override
    protected void dying(Mission.BonusType deathType) {
        if(!isOnStage()) return;

        super.dying(deathType);

        if(!isFreezing()) {
            //mettiamo animazione caduta
            setSpriterAnimation(FALLING_ANIM);

            //ci spostiamo in secondo piano
            getParent().removeActor(this); //ci rimuoviamo dal gruppo attuale
            backgroundGroup.addActor(this); //e ci spostiamo su quello di background

            //usiamo un altro body (perché vogliamo usare la fisica dopo la morte: quindi il body originale è inadatto per farlo)
            Vector2 pos = body.getPosition().cpy();
            removeBodyNow(); //rimuovi body... qui sicuramente non sei nel world.step
            boolean flip = !originalDirection.equals(getHorDirection());
            buildBody(BodyDef.BodyType.DynamicBody, pos, false, false, flip ? deadFlippedShapes : deadShapes); //costruiscine uno nuovo+
            for (Shape s : deadShapes) s.dispose();
            for (Shape s : deadFlippedShapes) s.dispose();
            allowRotations(false); //non facciamo ruotare il corpo, perché se è una sfera può dare problemi con l'attrito
        }
        else {
            getParent().removeActor(this); //ci rimuoviamo dal gruppo attuale
            backgroundGroup.addActor(this); //e ci spostiamo su quello di background
            getBody().setType(BodyDef.BodyType.DynamicBody);
            allowRotations(false);
        }
    }

    //callback per i danni
    @Override
    protected void hurt() {
        super.hurt();
        if(getCurrentSpriterAnimation() != ATTACK_ANIM && !isFrozen()) //durante l'attacco non può essere interrotto (e nemmeno quando è freezato)
            setSpriterAnimation(HURT_ANIM);
    }

    @Override
    protected void disappear() {
        if(!isFreezing())
            super.disappear();
        else
            iceBlock.explode();
    }

    /**ricalcola target*/
    protected void computeTarget() {
        setTarget(player.getHp() > 0 ? player : null); //mettiamo sempre player come default
    }

    //sto eseguendo animazione
    @Override
    protected void onSpriterAnimationExecuting(int id, int actualFrame, int totFrames) {
        if(id == ATTACK_ANIM && 100 * actualFrame >= totFrames * 65 && !alreadyShot) { //al 65% dell'animazione di attacco, chiamiamo shoot
            shoot();
            alreadyShot = true; //sparato durante questa animazione...
        }
    }

    @Override
    protected void onSpriterAnimationEnded(int id) {
        if(id == HURT_ANIM)
            setSpriterAnimation(FLYING_ANIM);
        if(id == ATTACK_ANIM) { //finita animazione d'attacco.. torna a idle
            setSpriterAnimation(FLYING_ANIM);
            alreadyShot = false; //ricordati che alla prossima animazione ancora puoi sparare
        }
        if(id == FALLING_ANIM && instantDisappear)
            remove();
    }

    @Override
    public void onCollisionWith(com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor actor) {
        numCollidingObjects++;

        if(getHp() <= 0) { //sono morto...
            //se entro in collisione con un nemico che sta su una piattaforma su cui ci sono più nemici vivi
            //allora devo scomparire, perché il mio corpo potrebbe interferire col loro movimento
            //NB: controlliamo di aver toccato il nemico per primo: se abbiamo toccato prima piattaforma e non siamo stati
            //rimossi, allora non diamo problemi
            if(actor instanceof PlatformEnemy && ((PlatformEnemy)actor).getMyPlatform() != null && ((PlatformEnemy)actor).getMyPlatform().hashCode() != collidingPlatformCode
                    && ((PlatformEnemy)actor).getMyPlatform().numLivingEnemies() > 1)
                disappear();

            //entro in collisione con una piattaforma
            if(actor instanceof com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform) {
                collidingPlatformCode = actor.hashCode();

                //se cado su una piattaforma con più di un nemico vivo, e ho sia nemici a destra che sinistra
                //allora mi rimuovo, perché altrimenti darei fastidio sicuramente
                int livingRight = ((com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform)actor).numLivingEnemies(getBody().getPosition().x * pixelPerMeter);
                int living = ((Platform)actor).numLivingEnemies();
                if(livingRight > 0 && living-livingRight > 0)
                    disappear();
            }
        }
    }

    @Override
    public void onCollisionEnded(com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor actor) {
        numCollidingObjects--;
    }

    /**è già apparso sullo schermo? */
    protected boolean isAppearedOnScreen() {
        return appearedOnScreen;
    }

    /**usato per settare gli id giusti*/
    @Override
    protected void onSpriterAnimationAdded(int id, int num) {
        //sistema id animazioni mentre carichi
        switch(num) {
            case 0:
                FLYING_ANIM = id;
                setSpriterAnimationMode(id, Animation.PlayMode.LOOP);
            break;
            case 1: HURT_ANIM = id; break;
            case 2: FALLING_ANIM = id; break;
            case 3: ATTACK_ANIM = id; break;
        }
    }

    public void setDeadShapes(Shape shapes[]) {
        deadShapes = shapes;
    }

    public void setDeadFlippedShapes(Shape shapes[]) {
        deadFlippedShapes = shapes;
    }

    /**imposta il target a cui sparare*/
    public void setTarget(PhysicSpriteActor target) {
        this.target = target;
    }

    @Override
    public void reset() {
        super.reset();
        flyingAway = false;
    }

    @Override
    public void resetY(float maxy) {
        super.resetY(maxy);
        initBodyY -= (maxy / world.getPixelPerMeter());
    }

    /**fa volare via il nemico, dopo @delay secondi*/
    public void flyAway(float delay) {
        if(!flyingAway) {
            flyingAway = true;
            flyAwayDelay = delay;
        }
    }

    /**aggiorna cose relative ai bonus... es: vento*/
    protected void updateBonusStatus(float delta) {
        if(flyingAway && getHp() > 0) {
            flyAwayDelay -= delta;
            if(flyAwayDelay <= 0)
                flyAway();
        }
    }
//10s
    /**vola via immediatamente*/
    protected void flyAway() {
        takeDamageUnchecked(Constants.INFTY_HP, Mission.BonusType.WIND);

        int dir = getX() < getStage().getWidth() * 0.5f ? 1 : -1; //verso che direzione deve essere spazzato via? se stiamo nella parte sinistra dello schermo, a destra e viceversa
        float s = com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_WIND_STRENGHT, MAX_WIND_STRENGHT);
        getBody().setLinearVelocity(getBody().getLinearVelocity().x + dir * s,getBody().getLinearVelocity().y + s);

        //vento grafico, tanto per
        int anim = com.pizzaroof.sinfulrush.util.Utils.randInt(0, 1);

        com.pizzaroof.sinfulrush.actors.OneShotSprite wind = new OneShotSprite(assetManager.get(com.pizzaroof.sinfulrush.util.Utils.sheetEffectScmlPath(com.pizzaroof.sinfulrush.Constants.WIND_EFFECT)),
                                                getStage().getBatch(), anim, anim == 0 ? 0.6f : 0.75f);
        float dh = com.pizzaroof.sinfulrush.util.Utils.randFloat(140, 170);
        float dw = dh / com.pizzaroof.sinfulrush.Constants.ORIGINAL_WIND_HEIGHT * com.pizzaroof.sinfulrush.Constants.ORIGINAL_WIND_WIDTH;
        wind.setOriginalWidth(com.pizzaroof.sinfulrush.Constants.ORIGINAL_WIND_WIDTH);
        wind.setDrawingWidth(dw);
        wind.setDrawingHeight(dh);
        wind.setHorDirection(dir > 0 ? SpriteActor.HorDirection.RIGHT : SpriteActor.HorDirection.LEFT);
        wind.setPositionFromCenter(getX() + getDrawingWidth() * 0.5f /*+ dir * dw * 0.5f*/, getY() + getDrawingHeight() * 0.5f);
        effectGroup.addActor(wind);
    }

    @Override
    protected void createIceBlock() {
        iceBlock = new IceBlock(assetManager.get(com.pizzaroof.sinfulrush.util.Utils.sheetEffectScmlPath(com.pizzaroof.sinfulrush.Constants.ICE_EFFECT)), getStage().getBatch(),
                                            3, 0.3f, 4, 0.3f, 4, 0.3f,
                                                 com.pizzaroof.sinfulrush.Constants.ORIGINAL_ICE_FLYING_WIDTH, Constants.ORIGINAL_ICE_FLYING_HEIGHT, getFreezeDuration());
    }

    /**factory method*/
    public static FlyingSniperEnemy createEnemy(String directory, SoundManager soundManager, AssetManager assetManager, World2D world, Stage stage, Vector2 initPosition, Group backgroundGroup, Group effectGroup) {
        try {
            Vector2 dim = com.pizzaroof.sinfulrush.util.Utils.enemyDrawingDimensions(directory);
            Shape shapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(com.pizzaroof.sinfulrush.util.Utils.enemyShapePath(directory), dim.x, dim.y, world.getPixelPerMeter()); //shape
            Shape deadShapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(com.pizzaroof.sinfulrush.util.Utils.enemyDeadShapePath(directory), dim.x, dim.y, world.getPixelPerMeter()); //dead shape
            Shape flipDeadShapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(Utils.enemyDeadShapePath(directory), dim.x, dim.y, world.getPixelPerMeter(), true); //dead flipped shape
            FlyingSniperEnemy e = new FlyingSniperEnemy(directory, soundManager, assetManager, stage, world, initPosition, backgroundGroup, effectGroup, shapes);
            e.setDeadShapes(deadShapes);
            e.setDeadFlippedShapes(flipDeadShapes);
            return e;
        }catch(IOException e) { //non dovrebbe succedere
            e.printStackTrace();
        }
        return null;
    }
}
