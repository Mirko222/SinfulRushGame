package com.pizzaroof.sinfulrush.actors.physics.game_actors;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.PlatformEnemy;
import com.pizzaroof.sinfulrush.actors.stage.TimescaleStage;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.ParticleActor;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.util.PlayerPower;
import com.pizzaroof.sinfulrush.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

/**giocatore, cioè personaggio che salta da solo sulle piattaforme*/
public class Player extends ParticleActor implements LivingEntity {

    /**velocità del giocatore (indicativa: non è un valore con significato fisico)*/
    private float speed;

    /**prossima velocità: il valore che assumerà la velocità la prossima volta che si tocca una piattaforma (cambiarla in aria può incasinare le cose per il salto)*/
    private float nextSpeed;
    /**facciamo lerp per passare da una velocità all'altra*/
    private static final float LERP_NEXT_SPEED = 0.6f;

    private Deque<Platform> platforms; //coda delle piattaforme da saltare
    private Platform actualPlatform; //piattaforma su cui si trova il personaggio

    private PlayerState state; //stato attuale del personaggio

    /**y a cui correggere la direzione*/
    private float yToCorrect;
    /**indica se abbiamo già corretto la traiettoria di salto (necessario, probabilmente, durante la caduta)*/
    private boolean corrected;

    /**id delle animazioni*/
    public int RUNNING_ID, JUMPING_START_ID, JUMPING_LOOP_ID, FALLING_ID, IDLE_ID, HURTING_ID, DYING_ID;
    public float origRunningDur, origJumpDur, origJloopDur, origFallDur, origIdleDur, origHurtDur, origDieDur;

    /**quanto padding aggiungiamo quando lavoriamo con le dimensioni del personaggio (più è alto più è safe, però più è basso più riusciamo a far fare cose difficili al personaggio)*/
    public static final float MAGIC_PADDING_MUL = .2f;

    /**varie costanti per decidere l'altezza massima a cui saltare*/
    private static final float MAGIC_HEIGHT_MUL = .7f;

    /**epsilon usata nel player*/
    private static final float PLAYER_EPS = 1000 * com.pizzaroof.sinfulrush.Constants.EPS;

    /**moltiplicatore di quanto possiamo rientare quando la cella è vuota*/
    private static final float EMPTY_PLATFORM_MUL = 0.25f;

    /**punti vita del giocatore*/
    private int hp;

    /**hp massimi del giocatore*/
    private int MAX_HP;

    /**contatore delle piattaforme saltate*/
    protected int jumpedPlatforms;

    /**jump direction iniziale per la piattaforma attuale: se cambia, abbiamo
     * superato il punto di salto, allora possiamo saltare uguale, per non creare glitch in cui fa avanti e indietro*/
    private int initJumpDir;

    /**malus ricevuti sulle piattaforme*/
    private int platformMalusCount;

    /**nemici/amici uccisi/salvati*/
    private int numEnemiesKilled, numFriendsKilled, numFriendsSaved, numBossKilled;

    private PlayerPower playerPower;

    /**possibili stati del giocatore*/
    public enum PlayerState {
        RUNNING,
        JUMPING,
        FALLING,
        WAIT //stato in cui entra solamente quando è respawnato (e aspetta di cadere sulla piattaforma per tornare a correre)
    }

    /**@param directory ogni giocatore è organizzato in una directory: la directory contiene un file scml con tutte le animazioni di cui il giocatore ha bisogno
     *                  contiene inoltre un file .txt con delle informazioni aggiuntive sul giocatore:
     *                  il file .txt è organizzato cosi:
     *                  Original_width_in_Spriter Draw_Width Draw_Height Width Height //(tenendo conto della dimensione virtuale dello schermo) [NB: width height sono le dimensioni vere dello sprite, le due drawing servono solo per stampare] [la larghezza in spriter, serve a ridimensionare le animazioni di spriter]
     *                  running_id running_duration start_jumping_id start_jumping_duration jumping_id jumping_duration falling_id falling_duration //coppie id, durata per ogni animazione in spriter
     *                  idle_id idle_duration hurt_id hurt_duration dying_id dying_duration (sono sulla stessa riga delle animazioni di sopra)
     *                  contiene inoltre un file shape.txt contente informazioni sullo shape del personaggio (organizzato in modo che possa essere letto da Utils.getShapesFromFile)
     * @param speed velocità del player*/
    protected Player(World2D world, Stage stage, float density, Vector2 initPosition, float speed, String directory, AssetManager assetManager, PlayerPower powers, Shape...shapes) {
        super(world, BodyDef.BodyType.DynamicBody, density, 0, 0, initPosition, false, com.pizzaroof.sinfulrush.Constants.PLAYER_CATEGORY_BITS, (short)-1, shapes);
        platforms = new LinkedList<>();
        actualPlatform = null;
        addAnimations(directory, assetManager, stage);
        setState(PlayerState.RUNNING);
        setSpeed(speed * powers.getSpeedMultiplier());
        nextSpeed = speed * powers.getSpeedMultiplier();

        MAX_HP = powers.getMaxHp();
        hp = MAX_HP;
        corrected = true;
        platformMalusCount = 0;

        numEnemiesKilled = numFriendsKilled = numFriendsSaved = numBossKilled = 0;
        jumpedPlatforms = 0;
        initJumpDir = 0;
        body.setSleepingAllowed(false); //non ammettiamo sleeping sul player... tanto si muove praticamente sempre

        this.playerPower = powers;
    }

    /**setta velocità del player*/
    public void setSpeed(float speed) {
        this.speed = speed;
        body.setGravityScale(speed); //gravità modificata in base alla velocità (più gravità -> salti più veloci)
        int ids[] = {RUNNING_ID, JUMPING_START_ID, JUMPING_LOOP_ID, IDLE_ID, HURTING_ID, DYING_ID, FALLING_ID};
        float dur[] = {origRunningDur, origJumpDur, origJloopDur, origIdleDur, origHurtDur, origDieDur, origFallDur};
        for(int i=0; i<ids.length; i++)
            setSpriterAnimationDuration(ids[i], dur[i] / speed);
    }

    /**USARE QUESTO PER CAMBIARE LA VELOCITà DEL PLAYER DURANTE IL GIOCO*/
    public void changeSpeed(float newSpeed) {
        this.nextSpeed = newSpeed;
    }

    /**aggiunge una piattaforma a quelle da saltare*/
    public void addPlatform(Platform platform) {
        if(actualPlatform == null) //la prima piattaforma inserita è quella del personaggio
            actualPlatform = platform;
        else
            platforms.add(platform);
    }

    @Override
    public void actFrameDependent(float delta) {
        //frame dependent perché facciamo controlli sulla distanza che devono essere fatti con delta piccoli
        super.actFrameDependent(delta);
        playerUpdate(delta);
    }

    public void playerUpdate(float delta) {
        //ASSUMIAMO SEMPRE CENTRO DI MASSA AL CENTRO DEL BODY
        //ASSUMIAMO CHE NON CI SIANO PIATTAFORME "UNA SOPRA L'ALTRA"
        //affinchè le piattaforme siano saltabili, quelle che si intersecano, devono avere una differenza di x maggiore di (1+MAGIC_EMPTY_PLATFORM_MUL)*width
        //affinchè le piattaforme siano saltabili, quando si effettua un salto, non deve esserci una piattaforma su cui si può "sbattere la testa"
        if(getHp() > 0) { //ancora vivo

            if(state.equals(PlayerState.WAIT)) {
                //body.setLinearVelocity(0, body.getLinearVelocity().y);
                return;
            }

            if (platforms.size() > 0 && actualPlatform.isEmpty()) { //c'è una piattaforma su cui devo arrivare (la prima) e posso saltare perchè quella attuale è vuota
                if (state.equals(PlayerState.RUNNING)) { //stiamo correndo... allora vedi in che direzione correre
                    setSpriterAnimation(RUNNING_ID); //potrebbe non esserci l'animazione giusta, se per esempio prima stavamo in combattimento
                    int dirx = getDirectionToGetNextPlatform(actualPlatform, platforms.getFirst());
                    body.setLinearVelocity(dirx * speed, body.getLinearVelocity().y);

                    setHorDirection(dirx > 0 ? SpriteActor.HorDirection.RIGHT : SpriteActor.HorDirection.LEFT); //in base a dove stiamo andando, scegli dove guardare

                    if (isInGoodPositionToJump(actualPlatform, platforms.getFirst())) { //sono arrivato in posizione per saltare...
                        setState(PlayerState.JUMPING);
                        jump(actualPlatform, platforms.getFirst()); //allora salta alla giusta destinazione

                        int jumpingDirection = directionToJump(actualPlatform, platforms.getFirst()); //verso che direzione stiamo saltando?
                        setHorDirection(jumpingDirection > 0 ? SpriteActor.HorDirection.RIGHT : SpriteActor.HorDirection.LEFT); //in base a dove stiamo andando, scegli dove guardare
                    }
                }

                float posy = getBody().getPosition().y; //posizione y in metri
                boolean goingUp = actualPlatform.getY() <= platforms.getFirst().getY();

                //sono arrivato alla y a cui dovevo correggere la traiettoria (ed è la prima volta dall'inizio del salto: posso correggere una sola volta)
                float eps = PLAYER_EPS * ((TimescaleStage)getStage()).getTimeMultiplier();
                if (Math.abs(posy - yToCorrect) <= eps && !corrected && ((state.equals(PlayerState.JUMPING) && goingUp) || (state.equals(PlayerState.FALLING) && !goingUp))) {
                    corrected = true;
                    adjustTrajectory(actualPlatform, platforms.getFirst()); //correggi traiettoria
                }

                if (getVelocity().y < 0 && state.equals(PlayerState.JUMPING)) //velocità negativa su y ma stava saltando... allora ora è in falling
                    setState(PlayerState.FALLING);
            } else {
                //c'è almeno un nemico sulla piattaforma dove vogliamo andare
                body.setLinearVelocity(0, body.getLinearVelocity().y); //stiamo fermi in balia dei nemici...
                if (getCurrentSpriterAnimation() != HURTING_ID)
                    setSpriterAnimation(IDLE_ID);
            }
        }
        else {
            if(isInCameraView())
                getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);
            else
                getBody().setActive(false);
        }


        //NON DOVREBBE FARE NIENTE!!! SERVE SOLO A RIMEDIARE A BUG TEMPORANEI CHE POSSONO VERIFICARSI
        fallingFixLastResort();
    }

    /**in che direzione dobbiamo saltare se stiamo saltando dalla piattaforma actual alla next?*/
    public int directionToJump(Platform actual, Platform next) {
        if(actual.getY() <= next.getY()) { //verso l'alto
            if(next.getX() >= actual.getX() && next.getX() + next.getWidth() <= actual.getX() + actual.getWidth()) { //caso speciale: vogliamo saltare su una piattaforma totalmente contenuta su quella dove siamo ora
                //allora (forse) possiamo saltare sia a destra che sinistra: per andare sul sicuro, saltiamo dove c'è più spazio
                float rightSpace = actual.getX() + actual.getWidth() - next.getX() - next.getWidth(); //spazio a destra
                float leftSpace = next.getX() - actual.getX(); //spazio a sinistra
                return rightSpace > leftSpace ? -1 : 1; //salto dallo spazio più grande
            }
            return actual.getX() < next.getX() ? 1 : -1; //tutti gli altri casi: salto dove posso
        }
        else { //verso il basso
            if(actual.getX() >= next.getX() && actual.getX() + actual.getWidth() <= next.getX() + next.getWidth()) { //caso speciale: la prossima piattaforma contiene completamente quella su cui siamo ora
                //allora (forse) possiamo buttarci sia a sinistra che a destra: buttiamoci dove c'è più spazio e andiamo sul sicuro
                float rightSpace = next.getX() + next.getWidth() - actual.getX() - actual.getWidth(); //spazio a destra
                float leftSpace = actual.getX() - next.getX(); //spazio a sinistra
                return rightSpace > leftSpace ? 1 : -1; //vado allo spazio più grande
            }
            return actual.getX() < next.getX() ? 1 : -1; //non è il caso speciale
        }
    }

    /**restituisce una x "prima" della piattaforma su cui si sta saltanto: se ci si arriva sicuramente non si va a sbattare con la piattaforma*/
    private float xToNotCrash(Platform actual, Platform next) {
        float ox = next.getX(), ow = next.getWidth(); //posizione/dimensioni piattaforme
        int jdir = directionToJump(actual, next); //verso che direzione devo saltare?
        float landingX = ox + (jdir > 0 ? 0 : ow); //x su cui dovrei atterrare in next (è un estremo)
        return landingX -1 * jdir * getWidth(); //x per non schiantarsi con la piattaforma @next (è un po' prima della piattaforma)
    }

    /**x necessaria affinchè si possa cadere senza colpire il pavimento della piattaforma attuale (in sostanza è un po' oltre la piattaforma attuale)*/
    private float xToNotHitGround(Platform actual, Platform next) {
        int jdir = directionToJump(actual, next); //direzione del salto
        return actual.getX() + (jdir > 0 ? actual.getWidth() : 0) + jdir * getWidth(); //estremità, più la mia larghezza per passarci (ne basta metà perchè il centro di massa è al centro)
    }

    /**x sulla piattaforma @actual da cui dobbiamo saltare verso @next (in pixel)*/
    public float xFromWhichToJump(Platform actual, Platform next) {
        float ax = actual.getX(), aw = actual.getWidth(); //posizione/dimensioni piattaforme

        int jdir = directionToJump(actual, next); //verso che direzione devo saltare?
        float edgedx = ax + aw; //estremo destro della piattaforma attuale
        float edgesx = ax; //estremo sinistro della piattaforma attuale

        if(actual.getY() <= next.getY()) { //si sale
            float xToNotCrash = xToNotCrash(actual, next); //x per non schiantarsi con la piattaforma @next (è un po' prima della piattaforma)
            return (jdir > 0 ? Math.min(edgedx, xToNotCrash) : Math.max(edgesx, xToNotCrash)); //deicido da dove saltare, considerando sia gli estremi che poco prima della piattaforma
        }
        else { //si scende
            return jdir > 0 ? edgedx : edgesx; //saltiamo sempre su un estremo nel caso in cui scendiamo
        }
    }

    /**indica la direzione su x verso cui dobbiamo muoverci se dalla piattaform actual vogliamo passare alla next*/
    private int getDirectionToGetNextPlatform(Platform actual, Platform next) {
        float xToJump = xFromWhichToJump(actual, next); //prendo la x da cui saltare...
        return getBody().getPosition().x * pixelPerMeter < xToJump ? 1 : -1; //in base a dove devo saltare, decido dove andare
    }

    /**siamo in posizione giusta per saltare dalla piattaforma actual alla next?*/
    private boolean isInGoodPositionToJump(Platform actual, Platform next) {
        float xtj = xFromWhichToJump(actual, next); //se sto vicino alla x da cui saltare, allora posso saltare
        int dir = getBody().getPosition().x * pixelPerMeter < xtj ? 1 : -1;

        if(initJumpDir == 0) //appena cambiato piattaforma: initJumpDir sta ancora a 0
            initJumpDir = dir;
        else if(dir * initJumpDir < 0) //la direzione è cambiata: ho passato il punto di salto, quindi salta e basta
            return true;

        float eps = PLAYER_EPS * ((TimescaleStage)getStage()).getTimeMultiplier();
        return Math.abs(getBody().getPosition().x * pixelPerMeter - xtj)/pixelPerMeter <= eps;
    }

    /**restituisce destinazione a cui bisogna saltare se si salta da actual a next (in pixel) */
    public Vector2 getDestinationForTheJump(Platform actual, Platform next) {
        float ax = actual.getX(), aw = actual.getWidth(), ox = next.getX(), ow = next.getWidth();
        int jdir = directionToJump(actual, next);
        float oy = next.getY() + next.getHeight(); //y della piattaforma d'arrivo (parte superiore)

        float destX; //destinazione x
        boolean centered = false; //abbiamo già centrato la posizione quando la piattaforma è vuota?
        if(actual.getY() <= next.getY()) //in base a se dobbiamo saltare verso l'alto o verso il basso, la condizione cambia
            destX = ox + (jdir > 0 ? 0 : ow); //quale estremità?
        else //verso il basso
            if (ax > ox + ow || ax + aw < ox) { //caso completamente separati
                destX = ox + (ax > ox + ow ? ow - getWidth()/4.f : getWidth()/4.f); //prendo un'estremità (in realtà leggermente più interno, o si rischia di cadere)

                if(next.isEmpty()) { //la prossima è vuota, se le piattafomre sono vicine possiamo usare la stessa idea di quando ci buttiamo su due che si intersecano
                    float xtnhg = xToNotHitGround(actual, next);
                    if( (jdir > 0 && destX < xtnhg) || (jdir < 0 && destX > xtnhg) ) { //andando all'estremità, in realtà "torniamo indietro", quindi meglio lasciarsi cadere
                        centered = true; //ci siamo già centrati, quindi non proviamo a rientrare di poco
                        destX = xtnhg;
                    }
                }
            }
            else { //caso in cui si intersecano:
                destX = ox + (jdir > 0 ? ow : 0) - jdir * getWidth() * 0.5f; //quale estremità? (rientriamo un po': se stiamo troppo sul bordo cadiamo)
                if(next.isEmpty()) { //se la prossima è vuota, possiamo andare verso il centro (solo questione grafica)
                    float xtnhg = xToNotHitGround(actual, next); //saltiamo al minimo necessario in sostanza e ci lasciamo cadere
                    destX = jdir > 0 ? Math.min(xtnhg, destX) : Math.max(xtnhg, destX);
                    centered = true;
                }
            }

        //questo rientro è solo una questione grafica
        if(next.isEmpty() && !centered) { //se la prossima piattaforma è vuota, possiamo andare un po' più interni (senza uscire fuori)
            float offset = jdir * getWidth() * EMPTY_PLATFORM_MUL; //offset di quanto rientrare
            if(destX + offset >= ox && destX + offset <= ox + ow) //provo ad andare nella direzione del salto
                destX += offset;
            else //esco fuori, allora forse posso andare in direzione opposta
                if(destX - offset >= ox && destX - offset <= ox + ow)
                    destX -= offset;
        }

        //destinazione y
        float destY = oy + (0.5f + MAGIC_PADDING_MUL) * getHeight(); //parte superiore della piattaforma (più metà altezza, perchè il centro di massa è a metà)

        return new Vector2(destX, destY);
    }

    /**applica forza (o setta velocità) per saltare da dove sei alla piattaforma successiva*/
    protected void jump(Platform actual, Platform next) {
        //ASSUMIAMO SEMPRE CENTRO DI MASSA AL CENTRO DEL BODY

        //in realtà col salto non arriviamo alla destinazione: saltiamo quello che basta per assicurarci che scendendo raggiungiamo la destinazione
        //una volta raggiunto il "punto di sicurezza" ci dirigiamo veramente al punto di destinazione (operazione gestita da @ajustTrajectory)

        Vector2 destination = getDestinationForTheJump(actual, next); //calcoliamo con esattezza la destinazione a cui saltare
        boolean goingUp = actual.getY() <= next.getY();

        //applichiamo la giusta velocità sia su y che x per raggiungere le distanze desiderate
        Vector2 start = getBody().getPosition().cpy().scl(pixelPerMeter); //posizione iniziale del body in pixel
        float maxy = getMaxHeightOfTheJump(start, destination); //massima y da raggiungere (è sempre maggiore di start.y e dest.y)

        float gravity = world.getGravity().y * body.getGravityScale(); //gravità applicata sul body (m/s^2)
        float yspace = (maxy - start.y) / pixelPerMeter; //spazio in metri da percorrere su y
        float ySpeedNeeded = com.pizzaroof.sinfulrush.util.Utils.initialSpeedToReachHeight(com.pizzaroof.sinfulrush.Constants.EPS, gravity, yspace); //velocità necessaria su y per raggiungere quello spazio

        float time; //tempo che devo impiegare per raggiungere la x buona (che non mi fa sbattere), intanto salgo lungo y, poi aggiusterò la traiettoria

        //trovo un punto comodo dove saltare per non sbattere
        //prendo una x che non mi fa sbattere con la piattaforma (ne se salto, ne se scendo), e ci arrivo mentre salgo
        float xgood = goingUp ? xToNotCrash(actual, next) : xToNotHitGround(actual, next);

        boolean longDownJump = false;
        int jdir = directionToJump(actual, next); //actual.getX() < next.getX();
        if(!goingUp && ((destination.x > xgood && jdir>0) || (destination.x < xgood && jdir<0)))
            longDownJump = true;

        //trovo in quanto tempo devo andare a xgood
        if(longDownJump) { //salto lungo e basso -> nessun rischio di colpire la mia piattaforma
            corrected = true; //non serve correggere la traiettoria
            xgood = destination.x; //andiamo direttamente a destinazione
            float t1 = com.pizzaroof.sinfulrush.util.Utils.timeToReachDistance(ySpeedNeeded, yspace, gravity); //quanto tempo ci metto ad arrivare a maxy?
            float t2 = com.pizzaroof.sinfulrush.util.Utils.timeToReachDistance(0, (destination.y - maxy)/pixelPerMeter, gravity); //quanto ci metto da maxy ad atterrare (sulla destinazione)?
            time = t1 + t2; //il tempo totale di salto
        }
        else { //qui bisogna essere più attenti: dividiamo la fase di salto in una fase di salita e una di discesa (gestita da @adjustTrajectory) (sia se stiamo saltando verso l'alto che scendendo)
            if (goingUp) { //salgo su una piattaforma più alta
                float wy = next.getY() + next.getHeight() + getHeight() * (0.5f + MAGIC_PADDING_MUL); //fino a questa y, mi muovo lungo x in maniera limitata: non voglio colpire la piattaforma
                float space2 = (wy - start.y) / pixelPerMeter; //quindi aspetto di superare l'altezza dell'altra piattaforma
                yToCorrect = wy / pixelPerMeter; //ci salviamo questa y in metri, cosi sappiamo quando correggere la traiettoria, per arrivare alla vera destinazione
                corrected = false; //ricordati che devi correggere...
                time = com.pizzaroof.sinfulrush.util.Utils.timeToReachDistance(ySpeedNeeded, space2, gravity); //aspetto solo il tempo necessario per salire fino a sopra la piattaforma d'arrivo
            } else { //scendo verso una piattaforma più bassa
                float space2 = (actual.getY() + actual.getHeight() - maxy) / pixelPerMeter; //spazio per tornare all'inizio
                yToCorrect = (actual.getY() + actual.getHeight()) / pixelPerMeter; //ci salviamo questa y in metri, cosi sappiamo quando correggere la traiettoria, per arrivare alla vera destinazione
                corrected = false; //ricordati che devi correggere...
                time = com.pizzaroof.sinfulrush.util.Utils.timeToReachDistance(ySpeedNeeded, space2, gravity);
            }
        }

        float xspace = (xgood - start.x)/pixelPerMeter; //spazio da percorrere su x
        float xSpeedNeeded = xspace / time; //velocità uniforme per percorrere lo spazio su x (moto rettilineo uniforme)

        body.setLinearVelocity(xSpeedNeeded, ySpeedNeeded);
    }

    /**restituisce massimo valore di y (in pixel) che si deve raggiungere per saltare da start a dest (entrambi in pixel)*/
    public float getMaxHeightOfTheJump(Vector2 start, Vector2 dest) {
        //intuitivamente, vogliamo che dipenda da: *differenza delle x* *altezza giocatore* *differenza delle y se si salta in alto*
        //ma deve essere limitata
        float maxy = Math.max(start.y, dest.y); //sicuramente deve essere almeno quanto il massimo

        return maxy + MAGIC_HEIGHT_MUL * getHeight();
    }

    /**aggiustiamo la traiettoria se stiamo finendo fuori destinazione*/
    private void adjustTrajectory(Platform actual, Platform next) {
        Vector2 destination = getDestinationForTheJump(actual, next); //calcoliamo destinazione per il salto

        //e stavolta andiamo veramente a destinazione mentre scendiamo
        float acty = getBody().getPosition().y * pixelPerMeter;
        float actx = getBody().getPosition().x * pixelPerMeter;
        float gravity = world.getGravity().y * getBody().getGravityScale();
        float time = com.pizzaroof.sinfulrush.util.Utils.timeToReachDistance(body.getLinearVelocity().y, (destination.y - acty)/pixelPerMeter, gravity); //tempo mancante per arrivare a destinazione

        if(time < 0) return; //tempo minore di 0 significa che in realtà è impossibile raggiungere esattamente quel punto (succede solo con distanze piccolissime, quindi non dovrebbe creare problemi)

        //aggiusta velocità per raggiungere destinazione in tempo
        float spacex = (destination.x - actx) / pixelPerMeter; //spazio ancora da percorrere su x
        float xvel = spacex / time; //velocità uniforme

        boolean goingDown = actual.getY() > next.getY();
        if(Math.abs(xvel) > speed && goingDown) //non può superare speed mentre cade e sta scendendo (quando sale gli può servire)
            xvel = xvel < 0 ? -speed : speed; //solo mentre sta scendendo perchè dà effetti grafici brutti

        body.setLinearVelocity(xvel, body.getLinearVelocity().y); //aggiorniamo velocità
    }

    /**restituisce la massima altezza raggiunta saltando da actual a next (in pixel)*/
    public float maxHeightReachedJumping(Platform actual, Platform next) {
        return getMaxHeightOfTheJump(new Vector2( xFromWhichToJump(actual, next), actual.getY() + actual.getHeight() + getHeight()*0.5f),
                getDestinationForTheJump(actual, next));
    }

    /**chiamato quando raggiungiamo la prossima piattaforma*/
    private void onReachNextPlatform() {
        jumpedPlatforms++;
        if(getHp() <= 0) //non vogliamo che questa piattaforma conti nello score
            increasePlatformMalus(1);
        actualPlatform.setJumped(true); //diciamo alla vecchia piattaforma che è stata saltata, e cambiamo piattaforma
        actualPlatform = platforms.getFirst(); //piattaforma raggiunta, vado avanti
        platforms.removeFirst();
        setState(PlayerState.RUNNING);

        if(Math.abs(nextSpeed - speed) > Constants.EPS) //bisogna cambiare velocità
            setSpeed(speed + (nextSpeed - speed) * LERP_NEXT_SPEED );
        initJumpDir = 0;
    }

    /**chiamato quando il giocatore entra il collisione con qualcosa*/
    @Override
    public void onCollisionWith(com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor actor) {
        if(actor instanceof Platform) { //entrato in collisione con una piattaforma
            if(platforms.size() > 0 && actor.samePosition(platforms.getFirst())) //è la prossima piattaforma da raggiungere
                onReachNextPlatform();

            if(state.equals(PlayerState.WAIT)) //se stava aspettando... appena tocca una piattaforma riparte
                setState(PlayerState.RUNNING);

            return;
        }

        if(actor instanceof PlatformEnemy) { //entrato in collisione con un nemico
            //è un nemico che sta sulla piattaforma che devo raggiungere
            if (platforms.size() > 0 && ((PlatformEnemy) actor).getMyPlatform().samePosition(platforms.getFirst()))
                onReachNextPlatform(); //consideriamo come se sono atterrato direttamente sulla piattaforma
            return;
        }
    }

    @Override
    protected void onSpriterAnimationEnded(int id) {
        super.onSpriterAnimationEnded(id);
        if(id == JUMPING_START_ID) //se sto saltando e l'animazione di inizio salto è finita, passo al salto in loop
            setSpriterAnimation(JUMPING_LOOP_ID);

        if(id == HURTING_ID) //finita animazione "hurt"... vai in idle
            setSpriterAnimation(IDLE_ID);

        if(id == DYING_ID) {
            //finita animazione di morte...
        }
    }

    /**prende danni*/
    @Override
    public void takeDamage(int dmg, PhysicSpriteActor attacker, Mission.BonusType damageType) {
        if(hp <= 0) return;
        hp -= dmg;
        if(hp > 0)
            hurt();
        else
            die();
    }

    public void takeDamage(int dmg, PhysicSpriteActor attacker) {
        takeDamage(dmg, attacker, null);
    }

    protected void hurt() {
        setSpriterAnimation(HURTING_ID);
    }

    protected void die() {
        setSpriterAnimation(DYING_ID);
    }

    /**setta stato del movimento del giocatore*/
    public void setState(PlayerState state) {
        this.state = state;
        //in base al nuovo stato selezioniamo la corretta animazione...
        if(getHp() > 0)
            switch(this.state) {
                case RUNNING: setSpriterAnimation(RUNNING_ID); break;
                case JUMPING: setSpriterAnimation(JUMPING_START_ID); break;
                case FALLING: setSpriterAnimation(FALLING_ID); break;
            }
    }

    /**legge le animazioni del player, rispettando il formato del file*/
    private void addAnimations(String directory, AssetManager assetManager, Stage stage) {
        try {
            BufferedReader reader = com.pizzaroof.sinfulrush.util.Utils.getInternalReader(com.pizzaroof.sinfulrush.util.Utils.playerInfoPath(directory));

            String strs[] = reader.readLine().split(" "); //prima riga: original_width draw_width-draw_height width-height considerando la dimensione virtuale dello schermo
            setOriginalWidth(Float.parseFloat(strs[0])); //width originale in spriter (height messa per mantenere aspect ratio)
            setDrawingWidth(Float.parseFloat(strs[1])); //dimensioni di drawing
            setDrawingHeight(Float.parseFloat(strs[2]));
            setWidth(Float.parseFloat(strs[3])); //dimensioni "vere" dello sprite nel gioco
            setHeight(Float.parseFloat(strs[4]));

            strs = reader.readLine().split(" "); //seconda riga: tante coppie (id, durata) per ogni animazione

            setSpriterData(assetManager.get(com.pizzaroof.sinfulrush.util.Utils.playerScmlPath(directory)), stage.getBatch());

            recomputeSpriterScale();
            recomputeSpriterFlip();
            recomputePosition();
            spriterPlayer.update();

            Animation.PlayMode modes[] = {Animation.PlayMode.LOOP, Animation.PlayMode.NORMAL, Animation.PlayMode.LOOP, Animation.PlayMode.NORMAL, Animation.PlayMode.LOOP, Animation.PlayMode.NORMAL, Animation.PlayMode.NORMAL}; //come eseguire le varie animazioni
            for(int i=0; i<2*modes.length; i+=2) { //4 coppie
                int id = Integer.parseInt(strs[i]);
                float dur = Float.parseFloat(strs[i+1]); //durata animazione
                addSpriterAnimation(id, dur, modes[i/2]); //aggiungi animazione spriter
                if(i==0) { RUNNING_ID = id; origRunningDur = dur; } //salva gli id per le varie azioni
                if(i==2) { JUMPING_START_ID = id; origJumpDur = dur; }
                if(i==4) { JUMPING_LOOP_ID = id; origJloopDur = dur; }
                if(i==6) { FALLING_ID = id; origFallDur = dur; }
                if(i==8) { IDLE_ID = id; origIdleDur = dur; }
                if(i==10) { HURTING_ID = id; origHurtDur = dur; }
                if(i==12) { DYING_ID = id; origDieDur = dur; }
            }

            reader.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(getMaxHp(), hp));
    }

    @Override
    public int getMaxHp() {
        return MAX_HP;
    }

    @Override
    public void heal(int hp) {
        if(this.hp <= 0) return; //già morto
        this.hp = Math.min(getMaxHp(), getHp() + hp);
    }

    public float getSpeed() {
        return speed;
    }

    public PlayerState getState() {
        return state;
    }

    public Platform getActualPlatform() {
        return actualPlatform;
    }

    public Deque<Platform> getPlatforms() {
        return platforms;
    }

    /**quante piattaforme ha saltato?*/
    public int getJumpedPlatforms() {
        return jumpedPlatforms;
    }

    public void onSavedFriend() {
    }

    @Override
    public void resetY(float maxy) {
        super.resetY(maxy);
        yToCorrect -= (maxy / world.getPixelPerMeter());
    }

    /**lo muove istantaneamente sulla prima piattaforma (NB: usato essenzialmente per respawnare)*/
    public void instantMoveOnFirstPlatform() {
        setSpriterAnimation(IDLE_ID);
        Platform platform = getRespawnPlatform();
        initJumpDir = 0;
        float x = platform.getX() + platform.getWidth() * 0.5f;
        float y = platform.getY() + platform.getHeight() + this.getHeight() * 0.5f;
        body.setTransform(new Vector2(x / world.getPixelPerMeter(), y / world.getPixelPerMeter()), body.getAngle());
        body.setActive(true);
        body.setLinearVelocity(0, 0);
        //body.setAwake(true); //non necessario... tanto non si addormenta mai
        setState(PlayerState.WAIT);
        recomputePosition();
        recomputeSpriterScale();
    }

    /**piattaforma su cui respawnare*/
    public Platform getRespawnPlatform() {
        return getActualPlatform().isInCameraView() ? getActualPlatform() : getPlatforms().getFirst();
    }

    public void setCharacterMaps(int charmaps) {
        if(charmaps >= 0)
            spriterPlayer.setCharacterMaps(charmaps);
    }

    public void increasePlatformMalus(int count) {
        platformMalusCount = Math.min(platformMalusCount + count, getJumpedPlatforms()); //non vogliamo superare le piattaforme saltate (sennò andiamo in negativo)
    }

    public void increaseEnemiesKilled() {
        numEnemiesKilled++;
    }

    public void increaseFriendsKilled() {
        numFriendsKilled++;
    }

    public void increaseFriendsSaved() {
        numFriendsSaved++;
    }

    public void increaseBossKilled() {
        numBossKilled++;
    }

    public int getNumEnemiesKilled() {
        return numEnemiesKilled;
    }

    public int getNumFriendsKilled() {
        return numFriendsKilled;
    }

    public int getNumFriendsSaved() {
        return numFriendsSaved;
    }

    public int getNumBossKilled() {
        return numBossKilled;
    }

    public int getPlatformMalusCount() {
        return platformMalusCount;
    }

    public PlayerPower getPlayerPower() {
        return playerPower;
    }

    /**METODO CHIAMATO IN CASO IL GIOCATORE CADA DALLA PIATTAFORMA: LO RIMETTE SULLA PIATTAFORMA
     * IN MODO DA POTER CONTINUARE IL GIOCO.
     * TEORICAMENTE NON DOVREBBE MAI SUCCEDERE!!! PERò IN CASO DI BUG STRANI CHE LO FACCIANO CAPITARE, L'UTENTE
     * PUò CONTINUARE A GIOCARE IN ATTESA DI FIXARE IL PROBLEMA*/
    protected void fallingFixLastResort() {
        if(getHp() > 0 && !isInCameraView()) {

            float midx = getStage().getCamera().position.x;
            if(com.pizzaroof.sinfulrush.util.Utils.pointInCamera(getStage().getCamera(), midx, getY()) || Utils.pointInCamera(getStage().getCamera(), midx, getY() + getHeight()))
                return;

            //il personaggio è vivo ma non sta nella view... allora è caduto, gli facciamo saltare la piattaforma

            //System.out.println("CADUTOOOOOOOOO");

            setSpriterAnimation(IDLE_ID);
            Platform platform = getPlatforms().getFirst(); //gli facciamo saltare la piattaforma (se è caduto, la sequenza di piattaforme non era saltabile...)
            float x = platform.getX() + platform.getWidth() * 0.5f;
            float y = platform.getY() + platform.getHeight() + this.getHeight() * 0.5f;
            body.setTransform(new Vector2(x / world.getPixelPerMeter(), y / world.getPixelPerMeter()), body.getAngle());
            body.setActive(true);
            body.setLinearVelocity(0, 0);
            body.setAwake(true);
            setState(PlayerState.WAIT);
            recomputePosition();
            recomputeSpriterScale();

            ArrayList<PlatformEnemy> enemies = new ArrayList<>(platform.getEnemies());
            enemies.addAll(actualPlatform.getEnemies());
            for(PlatformEnemy e : enemies) {
                e.destroyChildren();
                e.takeDamage(Constants.INFTY_HP, null);
            }
        }
    }
}
