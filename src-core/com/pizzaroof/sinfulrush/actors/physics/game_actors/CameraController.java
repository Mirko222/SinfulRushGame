package com.pizzaroof.sinfulrush.actors.physics.game_actors;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.pizzaroof.sinfulrush.actors.stage.DoubleActActor;
import com.pizzaroof.sinfulrush.actors.stage.TimescaleStage;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.util.PerlinNoise;

/**controller di una telecamera (fa in modo di seguire il personaggio)*/
public class CameraController extends DoubleActActor {

    /**player da seguire*/
    private Player player;

    /**camera da controllare*/
    private Camera camera;

    /**offset iniziale delle y*/
    private float startingOffset;

    /**stiamo andando in alto?*/
    private boolean goingUp;

    /**di quanto dobbiamo avvicinarci ogni volta al punto destinazione?*/
    private static final float FIXED_SMOOTH_PRM = 0.9f; //90%

    /**intensità dello shake in [0, 1]*/
    private float shakeTrauma;

    /**ci salviamo le trasformazioni effettuate sulla camera nell'ultimo frame.
     * x = incremento posizione x,
     * y = incremento posizione y,
     * z = incremento dell'angolo per rotazione su asse z*/
    private Vector3 cameraTransformations;

    private com.pizzaroof.sinfulrush.util.PerlinNoise shakeNoises[];

    private RandomXS128 rand = new RandomXS128();

    public static final float MAX_X_OFFSET = 20; //16
    public static final float MAX_Y_OFFSET = 12; //8
    private static final float MAX_ANGLE = 1.3f; //1.1 (radianti: [0, 6.28])

    /**di quanto deve scendere il trauma ogni secondo*/
    private static final float TRAUMA_DECREMENT = 0.6f; //0.4 //di quanto deve decrescere in un secondo

    public static final float MAX_TRAUMA = 1.f, HIGH_TRAUMA = 0.8f, MEDIUM_TRAUMA = 0.65f, LOW_TRAUMA = 0.5f;

    /**può muoversi? usata solo all'inizio degli scenari...*/
    private boolean canMove;

    /**screenshake abilitato?*/
    private boolean canShake;

    protected Vector2 upDownBoundings;

    /**passa @player e @camera da controllare
     * @param startingOffset offset del centro della camera (se 0, la camera punterà al centro), < 0 per abbassarla (rende più oggetti visibili sotto),
     * >0 per alzarla (più oggetti visibili sopra)*/
    public CameraController(Player player, Camera camera, float startingOffset, boolean goingUp, boolean canShake) {
        this.player = player;
        this.camera = camera;
        this.startingOffset = startingOffset;
        this.goingUp = goingUp;
        shakeTrauma = 0;
        cameraTransformations = new Vector3();
        shakeNoises = new com.pizzaroof.sinfulrush.util.PerlinNoise[3]; //un noise per x, uno per y, e uno per rotazione (usati nello shake)
        for(int i=0; i<3; i++)
            shakeNoises[i] = new com.pizzaroof.sinfulrush.util.PerlinNoise(); //ogni volta il seme sarà diverso
        canMove = true;
        this.canShake = canShake;
        upDownBoundings = new Vector2();
    }

    @Override
    public void actSkipTolerant(float delta) {
        restoreCamera(); //sistema la camera
        if(canMove)
            recomputePosition(delta); //ricalcola la posizione
        shake(delta); //fai lo shake della camera
    }

    protected void recomputePosition(float delta) {
        float newY = getNewY(); //qual è la Y che dobbiamo raggiungere?
        float acty = camera.position.y; //posizione y attuale

        float mul = Math.min(1.f, FIXED_SMOOTH_PRM * delta * player.getSpeed());
        float newCameraY = acty + (newY - acty) * mul; //dove ci muoviamo? (vogliamo un movimento smooth, non possiamo andare direttamente a newY)
        camera.translate(0, newCameraY - acty, 0);
    }

    /**restituisce nuova y da raggiungere*/
    private float getNewY() {
        //l'altezza da raggiungere è essenzialmente l'ultima piattaforma raggiunta dal giocatore

        //se sta saltando o cadendo, il player sta passando alla prossima piattaforma
        if(player.getState().equals(Player.PlayerState.JUMPING) || player.getState().equals(Player.PlayerState.FALLING)) {
            //allora ci muoviamo direttamente verso la prossima
            return player.getPlatforms().getFirst().getY() + startingOffset + (goingUp ? 0 : player.getPlatforms().getFirst().getHeight()); //se andiamo verso il basso, la destinazione è la parte superiore della piattaforma (altrimenti la inferiore)
        }

        return player.getActualPlatform().getY() + startingOffset + (goingUp ? 0 : player.getActualPlatform().getHeight());
    }

    /**rimette la camera apposto se sono state eseguite trasformazioni temporanee
     * (es: shake)*/
    protected void restoreCamera() {
        if(cameraTransformations.x != 0) { //x modificata all'ultimo frame (NB: facciamo i controlli per non fare roba inutile)
            camera.position.x -= cameraTransformations.x;
            cameraTransformations.x = 0;
        }
        if(cameraTransformations.y != 0) { //y modificata
            camera.position.y -= cameraTransformations.y;
            cameraTransformations.y = 0;
        }
        if(cameraTransformations.z != 0) { //rotazione su z modificata
            camera.rotate(Vector3.Z, -cameraTransformations.z);
            cameraTransformations.z = 0;
        }
    }

    /**effettua lo shake della camera*/
    protected void shake(float delta) {
        float shakeF = shakeFactor();

        //calcola come si deve muovere la telecamera (consideriamo sia lo shake factor, che un rumore casuale)t
        if(shakeF > Constants.EPS && canShake) { //se lo shakefactor è 0, è inutile farlo

            /*cameraTransformations.x = MAX_X_OFFSET * shakeF * (rand.nextFloat() * 2.f - 1.f);
            cameraTransformations.y = MAX_Y_OFFSET * shakeF * (rand.nextFloat() * 2.f - 1.f);
            cameraTransformations.z = MAX_ANGLE * shakeF * (rand.nextFloat() * 2.f - 1.f);*/

            float timeMul = ((TimescaleStage)getStage()).getTimeMultiplier();
            float inc = timeMul * PerlinNoise.DEF_TIME_INC;
            cameraTransformations.x = MAX_X_OFFSET * shakeF * shakeNoises[0].incNoise11(inc); //calcola nuovi valori
            cameraTransformations.y = MAX_Y_OFFSET * shakeF * shakeNoises[1].incNoise11(inc);
            cameraTransformations.z = MAX_ANGLE * shakeF * shakeNoises[2].incNoise11(inc);

            camera.position.x += cameraTransformations.x; //applica nuovi valori
            camera.position.y += cameraTransformations.y;
            camera.rotate(Vector3.Z, cameraTransformations.z);
        }

        shakeTrauma = Math.max(0, shakeTrauma - delta * TRAUMA_DECREMENT); //facciamo scendere il trauma linearmente
    }

    /**chiamato per aumentare il trauma*/
    public void incrementTrauma(float inc) {
        setTrauma(shakeTrauma + inc);
    }

    /**setta il trauma a valore fisso*/
    public void setTrauma(float trauma) {
        shakeTrauma = Math.min(1.f, Math.max(0, trauma));
    }

    /**setta il trauma, ma solo se è almeno quanto il trauma attuale*/
    public void setIncresingTrauma(float trauma) {
        setTrauma(Math.max(trauma, shakeTrauma));
    }

    /**calcola il fattore di shake*/
    protected float shakeFactor() {
        //trauma^2 funziona bene (anche trauma^3 è molto consigliato)
        return shakeTrauma * shakeTrauma;
        //return shakeTrauma * shakeTrauma * shakeTrauma;
    }

    /**x della camera (NB: le coordinate della camera indicano il centro dello schermo)*/
    public float getCameraX() {
        return camera.position.x;
    }

    /**y della camera (NB: le coordinate della camera indicano il centro dello schermo)*/
    public float getCameraY() {
        return camera.position.y;
    }

    /**x della camera rimuovendo effetti vari (eg: screenshake)*/
    public float getRestoredCameraX() {
        return getCameraX() - cameraTransformations.x;
    }

    /**y della camera rimuovendo effetti vari (eg: screenshake)*/
    public float getRestoredCameraY() {
        return getCameraY() - cameraTransformations.y;
    }

    /**larghezza viewport (cioè larghezza schermo)*/
    public float getViewportWidth() {
        return camera.viewportWidth;
    }

    /**altezza viewport (cioè altezza schermo)*/
    public float getViewportHeight() {
        return camera.viewportHeight;
    }

    /**aggiorna starting offset della camera*/
    public void setStartingOffset(float offset) {
        startingOffset = offset;
    }

    /**metti la camera subito in posizione giusta*/
    public void instantMoveCamera() {
        camera.position.y = getNewY();
    }

    public float getStartingOffset() {
        return startingOffset;
    }

    public void allowMovement(boolean allowMovement) {
        canMove = allowMovement;
    }

    public boolean movementAllowed() {
        return canMove;
    }

    /**restituisce estremo inferiore e superiore dello schermo di giooc visibile (usato per non far prendere danni ai nemici fuori da quest'area)*/
    public Vector2 getDownUpBoundings() {
        upDownBoundings.set(getRestoredCameraY() - getViewportHeight()*0.5f, getRestoredCameraY() + getViewportHeight()*0.5f);
        return upDownBoundings;
    }


    /**usata per resettare la y*/
    public void resetY(float maxy) {
        camera.position.y -= maxy;
        //cameraTransformations.y -= GameplayScreen.MAX_Y_COORD;
    }
}
