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
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.OneShotSprite;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.util.MarkovChain;
import com.pizzaroof.sinfulrush.util.PerlinNoise;
import com.pizzaroof.sinfulrush.util.Utils;

import java.util.ArrayList;

/**classe per un nemico sulla piattaforma.
 * le animazioni nel file del player sono le seguenti:
 * running | idle | dying | hurt | attack
 */
public class PlatformEnemy extends Enemy {

    /**id animazioni*/
    protected int RUNNING_ANIM, IDLE_ANIM, DYING_ANIM, HURT_ANIM, ATTACK_ANIM;

    protected final static float PADDING = 0.005f; //0.01f

    protected static final float FREEZE_DURATION = 3.f;

    /**probabilità di saltare in aria col fulmine*/
    protected static final float BLOW_UP_PROBABILITY = 0.8f;

    /**di quante unità si deve muovere al minimo quando sta aspettando il giocatore?
     * (è per evitare situazioni in cui fa passettini piccolissimi alternati a momenti idle)*/
    protected static final float MIN_MOVEMENT = 50;

    protected static final float EN_EPS = com.pizzaroof.sinfulrush.Constants.EPS * 250;

    /**velocità nemico*/
    protected float speed;

    /**piattaforma a cui appartiene il nemico*/
    protected com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform myPlatform;

    /**piattaforma precedente (ci serve conoscerla per sapere quando il giocatore si sta avvicinando)*/
    protected com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform previousPlatform;

    /**perlin noise*/
    private com.pizzaroof.sinfulrush.util.PerlinNoise noise;

    //destinazione su x (usata per movimenti sulla piattaforma quando non c'è il player)
    private float destinationX;
    //direzione verso cui deve muoversi
    private int dirx;
    //quanto si è mosso?
    private float unitsReached;
    //quanto spazio percorreremo in questa direzione prima di doverla ricambiare? (leggiamo i futuri valori di perlin noise per saperlo)
    private float unitsToRechangeDirection;

    /**generatore di numeri casuali, per scopi vari*/
    protected RandomXS128 rand;

    /**catena di markov usata per decidere come cambiare stato da idle a moving e viceversa
     * quando il giocatore non è ancora arrivato sulla piattaforma*/
    private com.pizzaroof.sinfulrush.util.MarkovChain<EnemyState> emptyPlatformChain;

    /**stato attuale*/
    private EnemyState actualState;

    /**booleano che ci indica se possiamo cambiare da idle a moving: una condizione è che possiamo
     * appena l'animazione idle supera il 50%*/
    private boolean canSwitchToMoving;

    /**è stato colpito da un fulmine?*/
    private boolean lightningHit;
    /**quanto manca prima di doverlo far saltare in aria?*/
    private float lightningDelay;

    /**possibile lista di figli: quando muore vengono inseriti loro*/
    protected ArrayList<PlatformEnemy> children;

    protected boolean mustAppearChildren;

    /**gruppo dei nemici (non serve a tutti...)*/
    protected Group enemiesGroup;

    /**vari stati che può assumere un nemico*/
    public enum EnemyState {
        IDLE, //fermo (quando sulla piattaforma non c'è ancora giocatore)
        MOVING, //si sta muovendo (quando sulla piattaforma non c'è ancora giocatore)
        ROTATING, //stato in cui sta cambiando direzione (ma si sta muovendo in sostanza): facciamo una piccola pausa e continuiamo il movimento
        MAKING_SPACE, //stato in cui facciamo spazio al giocatore che sta arrivando
        FIGHTING //lotta contro il giocatore (in generale, il giocatore è arrivato sulla piattaforma)
    }

    /**@param backgroundGroup gruppo dove deve essere messo il nemico una volta morto*/
    protected PlatformEnemy(World2D world, SoundManager soundManager, Stage stage, float density, float friction, float restitution, String directory, AssetManager asset, Vector2 initPosition, Group backgroundGroup, Group effectGroup, Shape... shapes) {
        super(world, soundManager, BodyDef.BodyType.KinematicBody, density, friction, restitution, initPosition, directory, asset, stage, effectGroup, backgroundGroup,shapes);
        speed = 0.5f;
        noise = new PerlinNoise();
        setSpriterAnimation(IDLE_ANIM);
        dirx = 0;
        rand = new RandomXS128();

        emptyPlatformChain = new MarkovChain<>(com.pizzaroof.sinfulrush.Constants.ENEMY_EMPTY_PLATFORM_MARKOV); //crea catena di markov
        emptyPlatformChain.addTranslation(0, EnemyState.IDLE); //aggiungiamo traduzioni per gli stati
        emptyPlatformChain.addTranslation(1, EnemyState.MOVING);

        setState(rand.nextBoolean() ? EnemyState.IDLE : EnemyState.MOVING); //decidi se all'inizio stai in idle o moving
        setHorDirection(rand.nextBoolean() ? SpriteActor.HorDirection.RIGHT : SpriteActor.HorDirection.LEFT);

        canSwitchToMoving = false;

        lightningHit = false;
        mustAppearChildren = false;
    }


    @Override
    public void actFrameDependent(float delta) {
        //frame dependent perché si fa uso di distanze
        super.actFrameDependent(delta);
        updateLogic(delta);
    }

    protected boolean isPlayerArrived() {
        return player.getActualPlatform().samePosition(myPlatform) && player.getHp() > 0;
    }

    /**aggiorna logica... lo separiamo dall'@act perchè magari le classe figlie
     * non vogliono eseguire la logica di un platform enemy generico*/
    protected void updateLogic(float delta) {
        updateBonusStatus(delta); //aggiorna parametri relativi ai bonus

        if(getHp() > 0) { //ancora vivo
            if(!isFrozen()) { //se non è ghiacciato ci muoviamo normalmente

                //il player è morto ma noi siamo in fighting o making space: significa che il player era arrivato da noi, allora andiamo in idle o moving per ignorare il player
                resetAnimationOnPlayerDeath();

                //CHECK!!! (crash: player null su getActualPlatform)
                if(body != null) {
                    if (!isPlayerArrived()) { //il giocatore non è ancora arrivato sulla mia piattaforma... (oppure è morto... quindi facciamo come vogliamo)
                        updateWhenPlayerHasntArrived(delta);
                    } else { //il giocatore è arrivato sulla mia piattaforma... fai altro
                        setState(EnemyState.FIGHTING);
                        updateWhenPlayerIsOnPlatform(delta);
                    }
                }
            } else { //nemico ghiacciato
                if(getBody() != null)
                    getBody().setLinearVelocity(0, 0); //stiamo fermi
            }
        }

        if(!getMyPlatform().isOnStage() && !isInCameraView()) //non sono nella camera view, e la mia piattaforma non è sullo stage: devo essere rimosso
            remove(); //può succedere se per bug strani si salta una piattaforma
    }

    /**quando il player è morto possiamo modificare un po' le animazioni, se stavamo attaccando...*/
    protected void resetAnimationOnPlayerDeath() {
        if(player.getHp() <= 0 && (getActualState().equals(EnemyState.FIGHTING) || getActualState().equals(EnemyState.MAKING_SPACE)))
            setState(getActualState().equals(EnemyState.FIGHTING) ? EnemyState.IDLE : EnemyState.MOVING);
    }

    //quando sta morendo...
    @Override
    public void dying(Mission.BonusType deathType) {
        super.dying(deathType);

        if(!getBlowUpOnDeath()) //se vola via, non possiamo spawnare i piccoletti
            initChildren();
        else //quando vola via, distruggiamone i corpi e basta
            destroyChildren();

        if(!isFreezing())
            setSpriterAnimation(DYING_ANIM);
        else {
            iceBlock.explode();
            if(mustAppearChildren)
                appearChildren();
        }

        myPlatform.removeEnemy(this); //ci rimuoviamo già dalla piattaforma
        if(getBlowUpOnDeath())
            body.setType(BodyDef.BodyType.DynamicBody); //mettiamo il body dinamico dopo la morte
        else
            body.setLinearVelocity(0, 0);
        //removeBody(); //ci rimuoviamo dallo stage e dal mondo
        //remove();
        if(children == null) //senza figli andiamo in secondo piano, altrimenti restiamo qui (risolve il problema dello spawn del boss quando ci sono i figli)
            backgroundGroup.addActor(this); //passiamo in secondo piano...
    }

    //quando viene colpito...
    @Override
    public void hurt() {
        super.hurt();
        changeToHurtAnimation();
    }

    /**va in hurt animation se possibile*/
    protected void changeToHurtAnimation() {
        if(isFrozen()) return; //da freezato non ci passa

        if((getCurrentSpriterAnimation() != ATTACK_ANIM && getCurrentSpriterAnimation() != RUNNING_ANIM) || //quando stiamo attaccando (o ci stiamo posizionando) non possiamo essere interrotti
                (!actualState.equals(EnemyState.MAKING_SPACE) && !actualState.equals(EnemyState.FIGHTING)))
            setSpriterAnimation(HURT_ANIM);
    }

    //callback per quando termina qualche animazione
    @Override
    protected void onSpriterAnimationEnded(int id) {
        if(id == DYING_ANIM) { //finita animazione di morte
            if(instantDisappear)
                remove(); //rimuoviamo definitivamente dallo stage
            else
                disappear(); //scompariamo
        }

        if(id == HURT_ANIM) { //finita hurt animation
            if(actualState.equals(EnemyState.IDLE) || actualState.equals(EnemyState.ROTATING)) //se stavamo fermi o ruotando...
                setSpriterAnimation(IDLE_ANIM);
            if(actualState.equals(EnemyState.MOVING) || actualState.equals(EnemyState.MAKING_SPACE)) { //se ci stavamo muovendo...
                setSpriterAnimation(RUNNING_ANIM);
                followDirection();
            }
            if(actualState.equals(EnemyState.FIGHTING))
                setSpriterAnimation(IDLE_ANIM);
        }

        if(id == IDLE_ANIM && actualState.equals(EnemyState.IDLE)) //sto fermo e ho finito l'animazione... proviamo a cambiare
            tryChangingToMoving();
    }

    //chiamata mentre le animazioni spriter vanno avanti
    @Override
    protected void onSpriterAnimationExecuting(int id, int actualFrame, int totFrames) {
        if(id == IDLE_ANIM && actualState.equals(EnemyState.IDLE)) //sto fermo
            if(3*actualFrame >= 2*totFrames) { //almeno 2/3 dell'animazione
                if(canSwitchToMoving) { //esattamente a 2/3 dell'animazione proviamo a cambiare
                    tryChangingToMoving();
                    canSwitchToMoving = false; //se continuiamo a stare fermi, ci stiamo per tutta la fine dell'animazione (e poi riproveremo)
                }
            }
            else
                canSwitchToMoving = true;

        //mi sto ruotando (quindi sto fermo)
        if(id == IDLE_ANIM && actualState.equals(EnemyState.ROTATING)) {
            if(2 * actualFrame >= totFrames) { //metà d'animazione idle, poi torniamo in moving
                setState(EnemyState.MOVING); //cambiamo stato
                followDirection(); //seguiamo la direzione che avevamo trovato
            }
        }

        if(id == DYING_ANIM && actualFrame * 2 >= totFrames && !mustBlowUpOnDeath()) { //non deve saltare in aria ed è su piattaforma... togliamogli il corpo tanto non gli serve
            removeBody();
        }
    }

    //prova a passare da idle a moving
    private void tryChangingToMoving() {
        if(emptyPlatformChain.moveTranslatedState().equals(EnemyState.MOVING)) //consulta la catena...
            setState(EnemyState.MOVING);
    }

    //si muove in direzione @dirx
    private void followDirection() {
        getBody().setLinearVelocity(dirx * speed, getBody().getLinearVelocity().y);
        setHorDirection(dirx > 0 ? SpriteActor.HorDirection.RIGHT : SpriteActor.HorDirection.LEFT);
        setSpriterAnimation(RUNNING_ANIM);
    }

    /**aggiorna nemico quando il giocatore non è ancora arrivato sulla piattaforma*/
    protected void updateWhenPlayerHasntArrived(float delta) {
        if(!player.getActualPlatform().samePosition(previousPlatform) || !previousPlatform.isEmpty()) //il giocatore è ancora lontano... muoviamoci come vogliamo
            moveAlongPlatform(); //muoviti lungo la piattaforma come vuoi (giusto una cosa grafica, per non farlo sembrare troppo statico)
        else//il giocatore sta arrivando... dobbiamo andare in una zona sicura per consentirgli di atterrare
            makeSpaceForPlayer();
    }

    /**restituisce nuova destinazione partendo da un valore pseudocasuale in [0, 1]*/
    private float newPositionFromNoise(float noise) {
        float minx = computeMinXOnPlatform(); //valore a caso tra [minx, maxx] usando il noise
        return minx + noise * (computeMaxXOnPlatform() - minx);
    }

    /**calcola massima x a cui può arrivare questo nemico sulla piattaforma (considera che ci possono
     * essere altri nemici)*/
    protected float computeMaxXOnPlatform() {
        //NB: ricorda getX() punto in basso a sx, ma qui vogliamo punto centrale
        float actX = getBody().getPosition().x * pixelPerMeter;
        float maxX = myPlatform.getX() + myPlatform.getWidth() - getWidth() / 2.f; // di default è l'estremità destra della piattaforma
        for(PlatformEnemy e : myPlatform.getEnemies())
            if(e.hashCode() != hashCode() && e.getHp() > 0 && e.getX() > getX()) { //un altro nemico vivo su questa piattaforma alla mia destra
                //importante usare le coordinate del body (se non vuoi farlo, ricorda la distinzione tra width e drawingwidth)
                float mid = (e.getBody().getPosition().x * pixelPerMeter - e.getWidth()/2.f + actX + getWidth()/2.f) / 2.f; //metà spazio lo lascio a lui, metà me lo prendo
                maxX = Math.min(maxX, mid - getWidth()*(0.5f+PADDING)); //un po' più a sinistra del nemico
            }
        return maxX;
    }

    /**calcola minima x a cui può arrivare questo nemico (considerando che possono esserci altri nemici
     * sulla piattaforma)*/
    protected float computeMinXOnPlatform() {
        float actX = getBody().getPosition().x * pixelPerMeter;
        float minX = myPlatform.getX() + getWidth() / 2.f; //default estremo sinistro piattaforma
        for(PlatformEnemy e : myPlatform.getEnemies())
            if(e.hashCode() != hashCode() && e.getHp() > 0 && e.getX() < getX()) { //un altro nemico vivo su questa piattaforma alla mia sinistra
                float mid = (e.getBody().getPosition().x * pixelPerMeter + e.getWidth()/2.f + actX - getWidth()/2.f) / 2.f; //metà spazio lo lascio a lui, metà me lo prendo
                minX = Math.max(minX, mid + getWidth()*(0.5f+PADDING)); //un po' più a destra del nemico
            }
        return minX;
    }

    /**muoviti lungo la piattaforma: viene chiamato quando il giocatore è ancora lontano*/
    protected void moveAlongPlatform() {
        float actx = getBody().getPosition().x * pixelPerMeter; //posizione attuale in pixel

        if (getCurrentSpriterAnimation() != HURT_ANIM) { //non siamo stati colpiti (oppure dobbiamo andare in una posizione sicura)

            if(computeMaxXOnPlatform() - computeMinXOnPlatform() < MIN_MOVEMENT) { //non ho abbastanza spazio per muovermi
                setState(EnemyState.IDLE); //fermati e basta
                getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);
                return;
            }

            //mi sto muovendo e ho raggiunto la destinazione
            if (actualState.equals(EnemyState.MOVING) && actx * dirx >= destinationX * dirx) {
                if (((unitsReached >= MIN_MOVEMENT && unitsToRechangeDirection-unitsReached >= MIN_MOVEMENT) || unitsToRechangeDirection < MIN_MOVEMENT) &&
                        emptyPlatformChain.moveTranslatedState().equals(EnemyState.IDLE)) { //decido se fermarmi o continuare a muovermi
                    //per fermarci: deve uscire il valore adatto dalla catena di markov, non dobbiamo aver camminato troppo poco, e non deve mancare troppo poco al cambio di direzione (altrimenti ci rifermiamo subito)
                    setState(EnemyState.IDLE); //mi fermo
                }
                else { //continuo ad andare
                    //ricalcoliamo nuova destinazione
                    destinationX = newPositionFromNoise(noise.noise());  //myPlatform.getX() + getWidth() / 2.f + noise.noise() * (myPlatform.getWidth() - getWidth());

                    int olddir = dirx;
                    boolean justRotating = false; //abbiamo appena iniziato a girarci?

                    //ci muoviamo verso la destinazione
                    dirx = destinationX > actx ? 1 : -1;
                    if (olddir == dirx || olddir == 0) { //stiamo seguendo la direzione... muoviamoci normalmente
                        followDirection();
                    } else { //dobbiamo cambiare direzione... dobbiamo ruotare allora
                        setState(EnemyState.ROTATING);
                        justRotating = true;
                    }

                    //abbiamo appena scelto una nuova direzione: salviamoci quanto spazio percorriamo prima di cambiarla di nuovo
                    //ci serve, cosi non ci fermiamo quando stiamo molto vicini a raggiungere il punto di rotazione
                    if(olddir == 0 || justRotating) {
                        //NB: questa simulazione viene fatta solo appena si cambia direzione, quindi ha complessità lineare

                        float t = noise.getActualTime(); //iniziamo dal tempo attuale
                        while(dirx * noise.noise01(t) < dirx * noise.noise01(noise.incrementTime(t))) //simuliamo l'aumento del tempo finché non cambiamo direzione
                            t = noise.incrementTime(t);
                        //calcoliamo quanto spazio percorriamo prima di ricambiare
                        unitsToRechangeDirection = Math.abs(actx - newPositionFromNoise(noise.noise01(t)));
                    }

                    unitsReached += Math.abs(destinationX - actx); //sommo subito alle unità raggiunte

                    //NB: non mettiamo destinationX subito al punto in cui si cambierà direzione, perché vogliamo che nei vari step intermedi ci si possa fermare per poi andare avanti
                    //NB2: nonostante questi controlli è ancora possibile avere situazioni in cui si fanno passi < MIN_MOVEMENT (cioè quando perlin noise decide di fare sequenze che vanno in una
                    //certa direzione per meno di MIN_MOVEMENT: si potrebbero scartare, ma sono abbastanza rare che forse non ne vale la pena
                }
            }
            else {
                if(actualState.equals(EnemyState.MOVING))
                    followDirection(); //devo ancora arrivare... segui la direzione (non dovrebbe servire, ma lo facciamo in caso il nostro stato fisico sia stato perturbato da oggetti esterni)
                else
                    getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);
            }
        } else //siamo stati colpiti: stai fermo e fai animazione hurt
            getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);
    }

    /**x sicura per questo nemico: quando è da solo e sta arrivando il player, dove va?
     * @param playerLandingX x a cui atterra il player
     * @param leftSide playerLandingX è a sinistra della piattaforma? (o a destra?)*/
    protected float getMySafeX(float playerLandingX, boolean leftSide) {
        return playerLandingX + (getWidth() + player.getWidth() * .5f) * (leftSide ? 1 : -1);
    }

    /**fai spazio affinchè il giocatore riesca ad arrivare: chiamato quando il giocatore è sulla piattaforma precedente*/
    protected void makeSpaceForPlayer() {
        float actx = getBody().getPosition().x * pixelPerMeter; //posizione attuale in pixel (il centro del body in realtà)

        float playerLandingX = player.getDestinationForTheJump(previousPlatform, myPlatform).x; //a che x atterrerà il giocatore?
        boolean leftSide = playerLandingX < myPlatform.getX() + myPlatform.getWidth()*0.5f; //il giocatore atterrerà sul lato sinistro?
        float safeX = getMySafeX(playerLandingX, leftSide); //x a cui andare per stare al sicuro (più internamente rispetto all'estremità)

        int dir = leftSide ? 1 : -1; //in base a quanti altri nemici ci sono, cerco di fargli spazio
        for(PlatformEnemy e : myPlatform.getEnemies())
            if(e.hashCode() != hashCode() && e.getHp() > 0 && e.getX() * dir < getX() * dir)
                safeX += dir * e.getWidth();
        //controlla di non andare oltre quello che puoi... (perchè magari vai fuori piattaforma, o perché colpisci altro nemico
        safeX = leftSide ? Math.min(safeX, computeMaxXOnPlatform()) : Math.max(safeX, computeMinXOnPlatform());

        float eps = EN_EPS * ((TimescaleStage)getStage()).getTimeMultiplier();
        if(Math.abs(safeX - actx) > eps * pixelPerMeter) { //non siamo ancora al sicuro... dobbiamo andare verso la x sicura
            //destinationX = safeX;
            int oldDir = getHorDirection().equals(SpriteActor.HorDirection.RIGHT) ? 1 : -1; //dove stavo guardando prima?
            dirx = safeX < actx ? -1 : 1; //direzione per andare in x sicura
            setSpriterAnimation(RUNNING_ANIM);
            setState(EnemyState.MAKING_SPACE);
            followDirection();

            //--- condizione speciale per risolvere un piccolo glitch grafico ----
            int nextDir = playerLandingX >= safeX ? 1 : -1; //direzione verso cui dovrà guardare quando avrà raggiunto il punto sicuro
            if (oldDir != dirx && oldDir == nextDir && Math.abs(safeX - actx) - eps*pixelPerMeter < MIN_MOVEMENT) //dovremmo cambiare direzione per pochissimo spazio... lasciamola direttamente cosi ed evitiamo un brutto effetto grafico
                setHorDirection(nextDir > 0 ? SpriteActor.HorDirection.RIGHT : SpriteActor.HorDirection.LEFT);
        }
        else //siamo già al sicuro... aspettiamo il giocatore
            if(!actualState.equals(EnemyState.FIGHTING)) {
                setState(EnemyState.FIGHTING);
                setSpriterAnimation(IDLE_ANIM);
                getBody().setLinearVelocity(0, 0);
                setHorDirection(playerLandingX >= actx ? SpriteActor.HorDirection.RIGHT : SpriteActor.HorDirection.LEFT); //guarda verso dove atterrerà il giocatore
            }
    }

    /**chiamata per aggiornare la logica di quando il giocatore è arrivato sulla piattaforma*/
    protected void updateWhenPlayerIsOnPlatform(float delta) {
        //dipende dal tipo di giocatore...
    }

    @Override
    public void onCollisionWith(PhysicSpriteActor actor) {
        if(actor instanceof PlatformEnemy) { //collisione con altro platform enemy
            //scontrato con un altro platform enemy... ricalcoliamo la destinazione per non spostarlo troppo
            if(!actualState.equals(EnemyState.MAKING_SPACE) && !actualState.equals(EnemyState.FIGHTING)) //quando si fa spazio o si combatte, non si ricalcola
                destinationX = newPositionFromNoise(noise.noise01(noise.getActualTime())); //usiamo noise di prima, ma ricalcoliamo
        }
    }

    //cambia stato del nemico
    public void setState(EnemyState state) {
        if(actualState == null || !actualState.equals(state)) { //se già stiamo in questo stato non facciamo niente
            this.actualState = state; //cambia stato
            //fai qualcosa relativa al nuovo stato
            switch (actualState) {
                case MOVING:
                    setSpriterAnimation(RUNNING_ANIM);
                    emptyPlatformChain.setInitialState(1); //modifica lo stato della catena di markov
                    break;
                case IDLE:
                    setSpriterAnimation(IDLE_ANIM);
                    emptyPlatformChain.setInitialState(0); //modifica stato catena di markov (serve nel caso lo stato viene settato a mano)
                    getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);
                    unitsReached = 0;
                    break;
                case ROTATING:
                    setSpriterAnimation(IDLE_ANIM);
                    getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);
                    unitsReached = 0;
                    break;
            }
        }
    }

    public EnemyState getActualState() {
        return actualState;
    }

    @Override
    protected void onSpriterAnimationAdded(int id, int num) {
        //in base a quale animazione abbiamo aggiunto, ci salviamo l'id nel tipo corrispondente
        //(i platform enemy sono sempre fatti con questo formato...)
        switch(num) {
            case 0: //di default le animazioni sono normal... aggiusta quelle che devi
                RUNNING_ANIM = id;
                setSpriterAnimationMode(id, Animation.PlayMode.LOOP);
                break;
            case 1:
                IDLE_ANIM = id;
                setSpriterAnimationMode(id, Animation.PlayMode.LOOP);
                break;
            case 2: DYING_ANIM = id; break;
            case 3: HURT_ANIM = id; break;
            case 4: ATTACK_ANIM = id; break;
        }
    }

    /**restituisce la piattaforma su cui si trova*/
    public com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform getMyPlatform() {
        return myPlatform;
    }

    @Override
    public void reset() {
        super.reset();
        myPlatform = null;
        previousPlatform = null;
        dirx = 0;
        setState(rand.nextBoolean() ? EnemyState.IDLE : EnemyState.MOVING); //decidi se all'inizio stai in idle o moving
        setHorDirection(rand.nextBoolean() ? SpriteActor.HorDirection.RIGHT : SpriteActor.HorDirection.LEFT);
        canSwitchToMoving = false;

        lightningHit = false;
    }

    /**esegue aggiornamento relativi ai bonus... per esempio se è stato colpito da un fulmine*/
    protected void updateBonusStatus(float delta) {
        if(lightningHit && getHp() > 0) { //colpito dal fulmine
            lightningDelay -= delta;
            if(lightningDelay <= 0) //fulmine si attiva
                lightningExplosion();
        }
    }

    /**da chiamare quando viene colpito da un fulmine... deve saltare in aria dopo poco*/
    public void hitByLightning(float delay) {
        if(!lightningHit) { //non possiamo essere colpiti due volte... aspetta un po' di delay ed esploderai
            lightningHit = true;
            lightningDelay = delay; //prima di esplodere aspetta un po'
        }
    }

    /**esegue vera esplosione del fulmine, dopo il ritarod*/
    protected void lightningExplosion() {
        mustBlowUpOnDeath(com.pizzaroof.sinfulrush.util.Utils.randFloat() <= BLOW_UP_PROBABILITY);
        takeDamageUnchecked(Constants.INFTY_HP, Mission.BonusType.LIGHTNING);

        com.pizzaroof.sinfulrush.actors.OneShotSprite lightning = new OneShotSprite(assetManager.get(com.pizzaroof.sinfulrush.util.Utils.sheetEffectScmlPath(com.pizzaroof.sinfulrush.Constants.LIGHTNING_EFFECT)),
                                                        getStage().getBatch(), 0, 1.f);
        lightning.setOriginalWidth(com.pizzaroof.sinfulrush.Constants.LIGHTNING_ORIGINAL_WIDTH);
        float w = Utils.randInt(100, 130);
        lightning.setDrawingWidth(w);
        lightning.setDrawingHeight(w / com.pizzaroof.sinfulrush.Constants.LIGHTNING_ORIGINAL_WIDTH * Constants.LIGHTNING_ORIGINAL_HEIGHT);
        float py = getMyPlatform().getY() + getMyPlatform().getDrawingHeight() * 0.8f;
        lightning.setPositionFromCenter(getX() + getDrawingWidth()/2.f, py);
        effectGroup.addActor(lightning);
    }

    @Override
    protected float getFreezeDuration() {
        return FREEZE_DURATION;
    }

    public void setEnemiesGroup(Group enemiesGroup) {
        this.enemiesGroup = enemiesGroup;
    }

    protected float getYForChild(Enemy child) {
        float y = getMyPlatform().getY() + getMyPlatform().getHeight() * 0.8f + child.getHeight() * 0.5f;
        return y / world.getPixelPerMeter();
    }

    @Override
    public void onDisappearing() {
        super.onDisappearing();
        if(mustAppearChildren)
            appearChildren();
    }

    @Override
    public boolean remove() {
        boolean r = super.remove();
        if(mustAppearChildren)
            appearChildren();
        return r;
    }

    public void setPreviousPlatform(com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform platform) {
        previousPlatform = platform;
    }

    public void setMyPlatform(Platform platform) {
        myPlatform = platform;
    }

    public void initChildren() {
        if(children != null) {
            for (PlatformEnemy e : children) {
                e.setCameraController(getCameraController());
                myPlatform.addEnemy(e);
                e.setMyPlatform(myPlatform);
                e.setPlayer(player);
                e.setPreviousPlatform(previousPlatform);
                e.setMissionDataCollector(missionDataCollector);
                e.setEnemyCallback(callback);
            }
            mustAppearChildren = true;
        }
    }

    protected void appearChildren() {
        if(children != null) { //se ci sono figli da spawnare li spawniamo
            mustAppearChildren = false;

            for(PlatformEnemy e : children)
                e.getBody().setActive(true);

            float mul = getHorDirection().equals(originalDirection) ? 1 : -1;
            float offx = deadCenterOffsets.x / world.getPixelPerMeter() * mul; //offset dovuto a morte
            float stx = getX() / world.getPixelPerMeter() + (getHorDirection().equals(originalDirection) ? 0 : getDrawingWidth() / world.getPixelPerMeter()); //starting x

            switch(children.size()) {
                case 0: return;

                case 1: //solo 1: nemico al centro
                    if(isFrozen())
                        children.get(0).instantSetPosition(new Vector2(getBody().getPosition().x, getYForChild(children.get(0))));
                    else
                        children.get(0).instantSetPosition(new Vector2(stx + offx, getYForChild(children.get(0))));
                    enemiesGroup.addActor(children.get(0));
                    break;

                case 2: //2 nemici: uno un po' a sinistra e uno un po' a destra
                    if(isFrozen()) { //quando è ghiacciato, non viene fatta l'animazione di morte, quindi considerare l'offset di morte è inutile
                        float x0 = getBody().getPosition().x - children.get(0).getWidth() * 0.6f / world.getPixelPerMeter();
                        float x1 = getBody().getPosition().x + children.get(1).getWidth() * 0.6f / world.getPixelPerMeter();
                        children.get(0).instantSetPosition(new Vector2(x0, getYForChild(children.get(0))));
                        children.get(1).instantSetPosition(new Vector2(x1, getYForChild(children.get(1))));
                        enemiesGroup.addActor(children.get(0));
                        enemiesGroup.addActor(children.get(1));

                    } else {
                        float x0 = stx + offx - children.get(0).getWidth() * 0.6f / world.getPixelPerMeter(); //un po' a sinistra
                        float x1 = stx + offx + children.get(1).getWidth() * 0.6f / world.getPixelPerMeter(); //un po' a destra
                        addChildIfInPlatform(children.get(0), x0);
                        addChildIfInPlatform(children.get(1), x1);
                    }
                    break;

                default: //TODO
            }
        }
    }

    private void addChildIfInPlatform(PlatformEnemy child, float x) {
        if(x * world.getPixelPerMeter() > getMyPlatform().getX() && x * world.getPixelPerMeter() < getMyPlatform().getX() + getMyPlatform().getWidth()) { //il centro è nella piattaforma
            child.instantSetPosition(new Vector2(x, getYForChild(child)));
            enemiesGroup.addActor(child);
        } else {
            myPlatform.removeEnemy(child);
            child.removeBody();
        }
    }

    /**setta i figli di questo nemico*/
    public void setChildren(ArrayList<PlatformEnemy> children) {
        this.children = children;
        for(PlatformEnemy e :children)
            e.getBody().setActive(false);
    }

    public ArrayList<PlatformEnemy> getChildren() {
        return children;
    }

    public void destroyChildren() {
        if(children != null) {
            for (PlatformEnemy e : children)
                e.removeBody();
            children.clear();
        }
    }
}