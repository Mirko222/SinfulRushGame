package com.pizzaroof.sinfulrush.actors.stage;


import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.World2D;

/**stage in grado di gestire timescale*/
public class TimescaleStage extends Stage {

    /**limitiamo il numero di updates in un frame*/
    private static final int MAX_UPDATES_PER_FRAME = 80; //8

    /**accumulatore del tempo per cui dobbiamo aggiornare il gioco*/
    private float timeAccumulator;

    /**moltiplicatore per il tempo (usato per rallentare/velocizzare tutto)*/
    private float timeMultiplier;

    /**durata per il time multiplier*/
    private float timeMultiplierDuration;

    /**attributi per ottimizzazioni: facciamo un act skipTol e gli altri act frameDep*/
    private boolean skipTolAct, frameDepAct;

    public TimescaleStage() {
        timeAccumulator = 0;
        timeMultiplier = 1.f;
        timeMultiplierDuration = -1;
    }

    /**setta temporaneamente un moltiplicatore di tempo
     * @param timeMul moltiplicatore di tempo
     * @param duration durata REALE (in secondi) per la quale deve durare il moltiplicatore di tempo*/
    public void setTemporalyTimeMultiplier(float timeMul, float duration) {
        timeMultiplier = timeMul;
        timeMultiplierDuration = duration;
    }

    @Override
    public void act(float delta) {
        float realDelta = delta;

        if(timeMultiplierDuration > 0) { //dobbiamo ancora applicare moltiplicatore
            delta *= timeMultiplier;
        }

        //invece di fare un'update di delta secondi, ne facciamo tanti da 1/60 di secondi
        //questo impedisce problemi di "scatti" in teoria
        //NB: l'ho fatto soprattutto perchè nel salto del player verifico delle condizioni basandomi sulla distanza player-punto specifico, e questo è frame-dependent
        //NB2: se questa soluzione dovesse dare problemi, si possono sostituire le condizioni basate su distanza inserendo dei sensori 2d statici, e verificare la collisione con quei sensori
        // (originariamente questa soluzione scattava, per qualche motivo)
        //NB3: stiamo facendo questo giochetto solo nell'update, quindi non stampiamo a video più volte, inutilmente
        delta = Math.min(delta, com.pizzaroof.sinfulrush.actors.physics.World2D.MAX_DELTA_TIME * timeMultiplier); //non vogliamo delta troppo grandi, o scatta tutto
        timeAccumulator += delta; //tempo da aggiornare

        float fixedDelta = com.pizzaroof.sinfulrush.actors.physics.World2D.FIXED_DELTA_TIME * (timeMultiplierDuration > 0 ? timeMultiplier : 1);

        skipTolAct = true; //primo update è skiptolerance
        frameDepAct = false;
        int numUpds = (int)Math.floor(timeAccumulator / fixedDelta); //quanti update dobbiamo fare?

        numUpds = Math.min(numUpds, MAX_UPDATES_PER_FRAME);
        //Gdx.app.log("INFO", "NUMERO UPDATES: "+numUpds);

        if(numUpds > 0) {
            if(numUpds > 1) { //se facciamo 1 solo update, allora lo facciamo contemporaneamente skinTol e frameDep
                //se dobbiamo fare più di un update,
                //facciamo prima quello skipTol
                fixedAct(fixedDelta * numUpds);
                skipTolAct = false;
            }
            frameDepAct = true;
            while (timeAccumulator >= fixedDelta) { //facciamo tanti aggiornamenti da FIXED_DELTA_TIME
                fixedAct(fixedDelta);
                timeAccumulator -= fixedDelta;
            }
        }

        if(timeMultiplierDuration > 0) { //diminuiamo durata del moltiplicatore
            timeMultiplierDuration -= Math.min(realDelta, World2D.MAX_DELTA_TIME); //sottraiamo vero delta
            if(timeMultiplierDuration <= 0) //a tempo scaduto torna normale
                timeMultiplier = 1.f;
        }
    }

    protected void fixedAct(float delta) {
        //if(!isFrameDependentAct() && !isSkipTolerantAct())
        //    System.exit(-136);
        super.act(delta);
    }

    public float getTimeMultiplierDuration() {
        return timeMultiplierDuration;
    }

    public float getTimeMultiplier() {
        return timeMultiplier;
    }

    /**l'act che chiama questa funzione è skinTolerance?*/
    public boolean isSkipTolerantAct() {
        return skipTolAct;
    }

    /**l'act che chiama questa funzione è frameDependent?*/
    public boolean isFrameDependentAct() {
        return frameDepAct;
    }

    public boolean timeMultiplierApplied() {
        return Math.abs(1 - getTimeMultiplier()) > Constants.EPS;
    }
}
