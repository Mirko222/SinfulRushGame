package com.pizzaroof.sinfulrush.actors;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.pizzaroof.sinfulrush.actors.stage.DoubleActActor;
import com.pizzaroof.sinfulrush.util.Utils;

import java.util.HashMap;

/**attore che gestisce gli sprite (solo animazioni, niente fisica)*/
public class SpriteActor extends DoubleActActor implements Pool.Poolable, ResettableY {

    protected final static float MAX_TIME = 1000; //1000secondi

    /**animazioni disponibili per lo sprite (ogni animazione deve avere una chiave intera)*/
    protected HashMap<Integer, Animation<TextureRegion>> animations;

    /**id animazione corrente*/
    protected int currentAnimId;

    /**da quanto tempo stiamo eseguendo l'animazione corrente?*/
    protected float currentAnimTime;

    /**dimensioni usate per la stampa dello sprite:
     * è utile perchè spesso gli sprite potrebbero avere margini vuoti, e quindi le dimensioni dei personaggi non corrispondono
     * alle dimensuioni che bisogna usare per stampare*/
    protected float drawingWidth, drawingHeight;

    /**reference alla pool di cui facciamo parte (può essere null, se non stiamo usando pooling)*/
    protected Pool<SpriteActor> myPool;

    /**direzione orizzontale per lo sprite (di default guarda a destra) NB: in sostanza indica se dobbiamo "specchiare" lo sprite o stamparlo normale*/
    public enum HorDirection {
        RIGHT,
        LEFT
    }

    /**direazione orizzontale in cui lo sprite sta guardando*/
    protected HorDirection hdir;
    protected HorDirection originalDirection; //ci manteniamo la direzione originale dello sprite: cosi sappiamo quando dobbiamo specchiare e quando no

    /**@param originalDirection qual è la direzione verso cui guarda questo sprite di default?*/
    public SpriteActor(HorDirection originalDirection) {
        animations = new HashMap<>();
        currentAnimId = -1;
        currentAnimTime = 0;
        hdir = originalDirection;
        this.originalDirection = originalDirection;
        myPool = null;
        setName("SpriteActor");
    }

    /**costruttore in cui assumiamo che lo sprite guarda a destra*/
    public SpriteActor() {
        this(HorDirection.RIGHT);
    }

    /**aggiorna lo sprite
     * @param delta quanto tempo (in ms) è passato dall'ultima esecuzione?*/
    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);
        updateAnimation(delta);
    }

    /**si occupa di aggiornare lo stato dell'animazione (manda avanti il tempo)*/
    protected void updateAnimation(float delta) {
        if(animations.containsKey(currentAnimId)) {
            //aggiorna il tempo di esecuzione dell'animazione (stando attenti all'overflow)
            if (MAX_TIME - delta < currentAnimTime) //vado in overflow... aggiungo facendo modulo
                currentAnimTime = currentAnimTime - MAX_TIME + delta;
            else //niente overflow, aggiungo il tempo e basta
                currentAnimTime += delta;
        }
    }

    /**stampa a video lo sprite*/
    @Override
    public void draw(Batch batch, float alphaChannel) {

        if(animations.containsKey(currentAnimId)) { //deve esserci l'animazione corretta, altrimenti non abbiamo niente da stampare
            Color tmp = batch.getColor();
            batch.setColor(getColor());

            TextureRegion frame = animations.get(currentAnimId).getKeyFrame(currentAnimTime); //prendiamo il giusto frame
            drawFrame(frame, batch);

            batch.setColor(tmp);
        }
    }

    protected void drawFrame(TextureRegion frame, Batch batch) {
        if(!isInCameraView()) return;

        float x = getX() + (!hdir.equals(originalDirection) ? getDrawingWidth() : 0); //in base alla direzione attuale, stampa lo sprite "normale" o ruotato
        float w = (!hdir.equals(originalDirection) ? -1 : 1) * getDrawingWidth();

        float orx = w/2, ory = getDrawingHeight()/2.f; //l'origine è al centro (serve in sostanza solo per la rotazione)
        batch.draw(frame, x, getY(), orx, ory, w, getDrawingHeight(), 1, 1, getRotation());
    }

    /**inizia ad eseguire l'animazione @id*/
    public void setAnimation(int id) {
        if(currentAnimId == id) return; //l'animazione è la stessa che stiamo già eseguendo, non la cambiamo

        onAnimationChanged(currentAnimId, id); //dobbiamo fare qualcosa nel cambio dell'animazione?
        currentAnimId = id; //cambiamo animazione (NB: non controlliamo se l'animazione esiste veramente)
        currentAnimTime = 0; //azzeriamo tempo di esecuzione: l'abbiamo appena cambiata :)
    }

    /**aggiunge un'animazione allo sprite
     * @param sheet texture sheet dello sprite: contiene ogni frame in rettangoli di dimensione uguale
     * @param numRows numero righe nella sheet
     * @param numCols numero colonne nella sheet
     * @param totalDuration quanto dura in totale l'animazione? (SI ASSUME CHE I FRAME SIANO EQUIDISTANTI)
     * @param playmode come dobbiamo eseguire l'animazione? una volta e basta? in loop? ecc..*/
    public void addAnimationFromSheet(int id, Texture sheet, int numRows, int numCols, float totalDuration, Animation.PlayMode playmode) {
        if(animations.containsKey(id)) return; //se avevamo già messo un'animazione con lo stesso id, non facciamo niente

        TextureRegion tmp[][] = TextureRegion.split(sheet, sheet.getWidth() / numCols, sheet.getHeight() / numRows); //prendi la matrice dei frame
        TextureRegion frames[] = new TextureRegion[numRows * numCols]; //trasformiamo la matrice in vettore
        int ite = 0;
        for(int i=0; i<numRows; i++)
            for(int j=0; j<numCols; j++)
                frames[ite++] = tmp[i][j];
        animations.put(id, new Animation<>(totalDuration / frames.length, new Array<>(frames), playmode));
    }

    /**aggiunge animazione prendendola dalla texture atlas
     * @param id id dell'animazione
     * @param atlas texture atlas da dove prendere l'animazione
     * @param regionName nome della regione nella texture atlas contentente l'animazione
     * @param totalDuration durata totale dell'animazione
     * @param playMode come dobbiamo eseguire l'animazione?*/
    public void addAnimationFromAtlas(int id, TextureAtlas atlas, String regionName, float totalDuration, Animation.PlayMode playMode) {
        if(animations.containsKey(id)) return; //non aggiungiamo più volte stessa chiave
        Array<TextureAtlas.AtlasRegion> region = atlas.findRegions(regionName); //prendi l'animazione dall'atlas
        animations.put(id, new Animation<>(totalDuration / region.size, region, playMode)); //aggiungi l'animazione
    }

    /**true se l'animazione corrente è finita, false altrimenti*/
    public boolean isAnimationEnded() {
        if(!animations.containsKey(currentAnimId)) return true;
        return animations.get(currentAnimId).isAnimationFinished(currentAnimTime);
    }

    /**questo metodo viene eseguito quando si passa dall'animazione @oldId all'animazione @newId (non si assicura che queste animazioni esistano veramente)*/
    protected void onAnimationChanged(int oldId, int newId) {
        //usiamo l'animazione attuale per ricalcolare le dimensioni dello sprite (potrebbe essere una cosa che non ha senso a seconda dello sprite: in caso fai override)
        if(animations.containsKey(newId)) {
            setDrawingWidth(animations.get(newId).getKeyFrame(0).getRegionWidth());
            setDrawingHeight(animations.get(newId).getKeyFrame(0).getRegionHeight());
        }
    }

    /**questo sprite è nella view della camera?*/
    public boolean isInCameraView() {
        if(!isOnStage()) return false; //deve essere nello stage per essere nella view di una camera
        Camera camera = getStage().getCamera();

        //controlla se uno dei 4 vertici si vede nella telecamera
        return com.pizzaroof.sinfulrush.util.Utils.pointInCamera(camera, getX(), getY()) || com.pizzaroof.sinfulrush.util.Utils.pointInCamera(camera, getX()+getDrawingWidth(), getY())
                || com.pizzaroof.sinfulrush.util.Utils.pointInCamera(camera, getX(), getY()+getDrawingHeight()) || com.pizzaroof.sinfulrush.util.Utils.pointInCamera(camera, getX()+getDrawingWidth(), getY()+getDrawingHeight());
    }

    /**il punto centrale dello sprite è nella view?*/
    public boolean centerInView() {
        if(!isOnStage()) return false;
        return Utils.pointInCamera(getStage().getCamera(), getX()+getDrawingWidth()/2.f, getY()+getDrawingHeight()/2.f);
    }

    /**restituisce tutte animazioni*/
    public HashMap<Integer, Animation<TextureRegion>> getAnimations() {
        return animations;
    }

    /**setta larghezza di drawing*/
    public void setDrawingWidth(float drawingWidth) {
        this.drawingWidth = drawingWidth;
    }

    /**setta larghezza di drawing*/
    public void setDrawingHeight(float drawingHeight) {
        this.drawingHeight = drawingHeight;
    }

    /**ritorna larghezza usata per la stampa*/
    public float getDrawingWidth() {
        return drawingWidth;
    }

    /**ritorna altezza usata per la stampa*/
    public float getDrawingHeight() {
        return drawingHeight;
    }

    /**restituisce id animazione corrente*/
    public int getCurrentAnimId() {
        return currentAnimId;
    }

    /**questo sprite è sullo stage?*/
    public boolean isOnStage() {
        return getStage() != null;
    }

    /**setta direzione orizzontale dello sprite (si assume sempre che lo sprite originale sia con direzione "destra")*/
    public void setHorDirection(HorDirection dir) {
        hdir = dir;
    }

    /**direzione orizzontale?*/
    public HorDirection getHorDirection() {
        return hdir;
    }

    /**stessa posizone di un altro attore?*/
    public boolean samePosition(SpriteActor actor) {
        return actor.getX() == getX() && actor.getY() == getY();
    }

    /**posizione del centro di questo attore*/
    public Vector2 centerPosition() {
        return new Vector2(getX() + getDrawingWidth() / 2.f, getY() + getDrawingHeight() / 2.f);
    }

    /**setta la posizione, sapendo che il centro è a (cx, cy) in pixel*/
    public void setPositionFromCenter(float cx, float cy) {
        setPosition(cx - getDrawingWidth()/2.f, cy - getDrawingHeight()/2.f); //si assume che il centro dello sprite sia al centro dell'immagine da stampare
    }

    public void setPositionFromCenter(Vector2 pos) {
        setPositionFromCenter(pos.x, pos.y);
    }

    public void setPool(Pool<SpriteActor> pool) {
        myPool = pool;
    }

    public void removeAndFree() {
        super.remove();
        if(myPool != null)
            myPool.free(this);
    }

    public void setPosition(Vector2 pos) {
        setPosition(pos.x, pos.y);
    }

    /**setta la posizione all'istante (teletrasportando l'attore)*/
    public void instantSetPosition(Vector2 position) {
        setPosition(position);
    }

    /**resetta la y quando si sfora @maxy*/
    @Override
    public void resetY(float maxy) {
        setY(getY() - maxy);
    }

    /**chiamata da un disapperingSmoke*/
    public void onDisappearing() {
    }

    /**rimuove l'attore, e se ha una pool, lo rilascia*/
    public void removeAndFreeIfPossible() {
        if(myPool == null) //se non è in una pool, facciamo remove
            remove();
        else //altrimenti remove and free
            removeAndFree();
    }

    @Override
    public void reset() {
        currentAnimId = -1;
        currentAnimTime = 0;
        setVisible(true);
    }
}