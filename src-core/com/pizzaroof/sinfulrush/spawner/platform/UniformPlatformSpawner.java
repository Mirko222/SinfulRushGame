package com.pizzaroof.sinfulrush.spawner.platform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.io.IOException;
import java.util.Arrays;

/**generatore uniforme: genera piattaforme scegliendo (x, y) a caso in maniera uniforme.*/
public class UniformPlatformSpawner extends PlatformSpawner {

    /**generatore numeri pseudocasuali*/
    protected RandomXS128 rand;

    /**larghezza della viewport*/
    protected float viewportWidth;

    /**maxHeight[i] contiene l'altezza massima a cui il giocatore arriverà per x=i (in pixel)*/
    protected float maxHeights[];

    /**altezza massima dei mostri sulla prossima piattaforma che verrà generata (si assume che venga valorizzato opportunamente in qualche modo
     * esterno allo spawner di piattaforme)*/
    protected float heightOfMonstersOnNextPlatform;

    /**layer davanti piattaforma*/
    protected Group frontLayer;

    /**
     * @param upDirection deve generarli andando verso l'alto o verso il basso?
     */
    public UniformPlatformSpawner(boolean upDirection, AssetManager assetManager, float viewportWidth, World2D world) {
        super(upDirection, world, assetManager);
        rand = new RandomXS128();
        this.assetManager = assetManager;
        this.world = world;
        this.viewportWidth = viewportWidth;
        heightOfMonstersOnNextPlatform = 0;
        frontLayer = null;

        maxHeights = new float[(int)viewportWidth+1];
        for(int i=0; i<viewportWidth; i++)
            maxHeights[i] = 0;
    }

    @Override //override perchè dobbiamo aggiornare qualche struttura dati...
    public Platform getNextPlatform() {
        Platform newp = generateNextPlatform();
        if(upDirection) { //se sto salendo, devo aggiornare le altezze massime raggiungibili per le x
            if(getLastCreatedPlatform() != null)
                updateMaxHeights(getLastCreatedPlatform(), newp); //aggiorno sapendo di dover saltare
            updateMaxHeights(newp); //aggiorno sapendo che ci dovrà essere spazio sopra la piattaforma
        }

        lastCreatedPlatform = newp;
        //System.out.println(lastCreatedPlatform.getY());
        return newp;
    }

    //dobbiamo generare la prossima piattaforma
    @Override
    protected Platform generateNextPlatform() {
        //la piattaforma da generare è quella in nextType

        int w = 0, h = 0;
        try {
            Vector2 dim = Utils.padDimensions(nextType.v1);
            w = (int)dim.x; //width della piattaforma scelta
            h = (int)dim.y; //height della piattaforma scelta
        }catch(IOException e) {
            e.printStackTrace();
        }
        boolean flipX = rand.nextBoolean(); //tira una moneta per decidere se specchiare la piattaforma o no

        //una volta scelta la piattaforma: trova la posizione in cui generarla

        //la x si sceglie a caso in maniera uniforme, la y invece è la più piccola possibile (quindi la y non è nemmeno aleatoria in realtà)

        float x = getNewX(w); //prendiamo la x per la nuova piattaforma (basso a sinistra)
        float y = getNewY(x, w, h); //y per nuova piattaforma (basso a sinistra)
        float bodyx = (x + w / 2.f) / Constants.PIXELS_PER_METER; //x del body sta al centro ed è in metri
        float bodyy = (y + h / 2.f) / Constants.PIXELS_PER_METER; //come per x

        return Pools.obtainPlatform(nextType.v1, flipX, world, assetManager, new Vector2(bodyx, bodyy), getNextCover(), frontLayer); //creiamo nuova piattaforma
        //return Platform.createPlatform(world, assetManager, new Vector2(bodyx, bodyy), nextType.v1, flipX); //creiamo nuova piattaforma
    }

    /**restituisce file per la nuova piattaforma*/
    @Override
    protected String generateNextPlatformType() {
        //scelto in maniera uniforme
        return padAvailable.get(rand.nextInt(padAvailable.size()));
    }

    public String getNextCover() {
        return null;
    }

    public void setFrontLayer(Group layer) {
        frontLayer = layer;
    }


    boolean dir = true;
    /**x per la nuova piattaforma
     * @param w larghezza della nuova piattaforma (in pixel)*/
    protected float getNewX(int w) {

        Platform last = getLastCreatedPlatform(); //ultima piattaforma creata

        if(last == null) //prima piattaforma
            return rand.nextInt((int)Math.floor(viewportWidth) - w);

        float interruptX, restartX;

        if(upDirection) { //stiamo andando verso l'alto
            //per essere "saltabile", bisogna lasciare almeno player.getWidth() di spazio tra le x della vecchia e la nuova piattaforma (altrimenti il player va a sbattere la testa)
            //le x considerate qui, sono quelle dell'angolo in basso a sx
            interruptX = Math.max(last.getX() + last.getWidth() - player.getWidth() - w, 0); //x da cui non si può più generare
            restartX = last.getX() + player.getWidth(); //x a cui si può riniziare a generare
        }
        else {
            //verso il basso
            //sulla piattaforma su cui scenderò, devo avere almeno player.getWidth() spazio per non rischiare di colpire la piattaforma
            //float magicmul = 1.1f; //aggiungiamo un po' di padding alla larghezza del player
            interruptX = Math.max(0, last.getX() - player.getWidth()); //massima x a cui posso arrivare, altrimenti non ho più spazio
            restartX = Math.max(0, last.getX() + last.getWidth() + player.getWidth() - w); //però da questa x posso ripartire a spawnare
        }

        if (restartX < interruptX) //se possiamo ricominciare prima di interrompere: allora possiamo spawnare ovunque
            restartX = interruptX;
        float diff = restartX - interruptX; //quanto spazio perdiamo per spawnare?
        float x = getNextRandomX((int) Math.floor(viewportWidth) - w - (int) Math.ceil(diff)); //prendo una x nello spazio disponibile

        if (x >= interruptX) //se la x è dopo interrupX //&& x <= restartX
            x += diff; //aumento di diff: in sostanza voglio una x nello spazio che è ok: spazio: __ok1__--diff--__ok2__--w--
        return x;
    }

    /**restituisce nuova y (conoscendo già la x nuova, e @h = altezza piattaforma, @w = larghezza piattaforma, TUTTO IN PIXEL)*/
    protected float getNewY(float x, int w, int h) {
        Platform last = getLastCreatedPlatform();

        if(last == null) //prima piattaforma
            return 0;

        if(upDirection) { //sto andando verso l'alto
            //la y deve essere almeno quanto la y massima a cui il giocatore arriverà sotto questa piattaforma (più metà altezza giocatore, cosi ci passa agevolmente)
            //NB: il massimo valore sotto la piattaforma potrebbe essere più basso dell'ultima y uscita: questo non va bene perchè vogliamo sempre salire
            float diffH = Math.max(0, last.getHeight() - h); //aumentiamo della differenza di altezze: se la piattaforma vecchia era alta, sembra che scendiamo anche se stiamo alla stessa y
            float lastY = last.getY() + diffH; //quanto stavo alto prima? non posso scendere!!

            float minNeeded = getMaxHeight((int) x, (int) x + w); //qual è il minimo considerando quelli che mi saltano sotto?
            if (lastY < minNeeded) //se il massimo è dato da quelli che mi saltano sotto, prendo direttamente questo valore
                return minNeeded;
            //altrimenti aggiungo una componente casuale per non restare alla stessa altezza
            //la nuova y è una gaussiana di media lastY e deviazione standard proporzionale all'altezza del giocatore (NB: non vogliamo che sia minore di @lasY, quindi prendiamo valore assoluto)

            float stdev = player.getHeight();
            float gauss = Math.max(Math.min(Math.abs((float)rand.nextGaussian()), 2), Constants.EPS);
            return gauss * stdev + lastY; //nextGaussian è per una gaussiana di media 0 e deviazione standard 1 (gaussiana standardizzata)
        }

        //stiamo andando verso il basso...
        float spaceUp = Math.max(player.getHeight() * (1.5f + Player.MAGIC_PADDING_MUL), heightOfMonstersOnNextPlatform); //quanto spazio bisogna lasciare sopra? (devono entrarci il giocatore e i nemici)
        return last.getY() - spaceUp - h; //scendiamo abbastanza in basso da rendere possibili i salti (potrebbe essere più del necessario, ma è difficile dirlo a priori);
    }

    /**restituisce il prossimo valore random per le x (0<= valore <= max)*/
    protected float getNextRandomX(int max) {
        return rand.nextInt(max); //la prendiamo in maniera uniforme
    }

    /**aggiorna maxheights sapendo che si salterà da @from a @to*/
    protected void updateMaxHeights(Platform from, Platform to) {
        //bisogna considerare il punto di inizio del salto e il punto di fine del salto.
        //per semplicità, in quest'intervallo consideriamo come altezza massima raggiunta, quella di tutto il salto

        float jumph = player.maxHeightReachedJumping(from, to) + player.getHeight() * 0.5f; //altezza massima che raggiungerò durante il salto
        int jdir = player.directionToJump(from, to); //direzione verso cui saltiamo
        int lastx = Math.min((int)viewportWidth-1, Math.max(0, (int)(player.xFromWhichToJump(from, to) -1 * jdir * player.getWidth()))); //partenza del salto
        int destx = Math.min((int)viewportWidth-1, Math.max(0, (int)(player.getDestinationForTheJump(from, to).x + jdir * player.getWidth()))); //arrivo del salto

        for(int i=Math.min(lastx, destx); i<Math.max(lastx, destx); i++) //nell'intervallo di salto mettiamo l'altezza massima del salto
            maxHeights[i] = Math.max(maxHeights[i], jumph);
    }

    /**aggiorna maxheights sapendo che atterrerai su @act*/
    protected void updateMaxHeights(Platform act) {
        //lungo tutta la piattaforma, l'altezza che riesce a raggiungere è almeno y_plat + h_plat + h_player
        //(perchè la raggiunge semplicemente stando sulla piattaforma, senza nemmeno saltare)

        //questo metodo può essere chiamato col player a null: se succede usiamo l'altezza piattaforma al posto dell'altezza player
        float playerH = player == null ? act.getHeight() : player.getHeight();

        for(int i=(int)act.getX(); i<act.getX()+act.getWidth(); i++)
            maxHeights[i] = Math.max(maxHeights[i], act.getY()+act.getHeight() + Math.max(playerH*1.5f, heightOfMonstersOnNextPlatform)); //1.5f perchè deve anche saltare
    }

    /**restitisce il massimo di maxHeights in [a, b]*/
    protected float getMaxHeight(int a, int b) {
        float m = maxHeights[a];
        for(int i=a+1; i<=b; i++)
            m = Math.max(maxHeights[i], m);
        return m;
    }

    /**chiamala per aggiornare la larghezza della viewport*/
    public void setViewportWidth(float viewportWidth) {
        this.viewportWidth = viewportWidth;
        maxHeights = Arrays.copyOf(maxHeights, (int)viewportWidth+1); //allunga maxheights per ottenere la dimensione voluta (o tronca)
    }

    /**usala per settare l'altezza dei mostri sulla prossima piattaforma (metti un upper bound se non puoi prevederlo)*/
    public void setHeightOfMonstersOnNextPlatform(float h) {
        heightOfMonstersOnNextPlatform = h;
    }

    /**usato per resettare y quando diventa troppo grande*/
    @Override
    public void resetY(float maxy) {
        for(int i=0; i<maxHeights.length; i++)
            maxHeights[i] -= maxy;
    }
}
