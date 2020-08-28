package com.pizzaroof.sinfulrush.actors.stage;

import com.badlogic.gdx.scenes.scene2d.Actor;

/**è un attore in cui ci sono due modalità di act:
 * #actSkipTolerant: è un act che tollera salti nel delta
 * #actFrameDependent: è un act che è frame dependent e che verrà sempre eseguito con un delta fisso
 *                     (se ne occupa il TimescaleStage)
 *
 * l'actFrameDependent è una act più pensante, perché può essere chiamato più volte nello stesso intervallo
 * quindi bisogna metterci il minimo indispensabile, lo SkipTolerant invece viene chiamato una volta per frame
 * ma non si hanno garanzie su piccoli delta (i delta del frameDependent saranno circa 1/60 sec)*/
public class DoubleActActor extends Actor {

    public DoubleActActor() {
        super();
        setName("DoubleActActor");
    }

    @Override
    public void act(float delta) {
        if(getStage() == null) {
            //System.out.println("stage null: " + getName());

            return; //può accadere perché l'abbiamo tolto durante un act, ed è rimasto nella coda degli oggetti da processare?...
        }

        if(getStage() instanceof com.pizzaroof.sinfulrush.actors.stage.TimescaleStage) {
            boolean st = ((com.pizzaroof.sinfulrush.actors.stage.TimescaleStage) getStage()).isSkipTolerantAct();
            boolean fd = ((TimescaleStage) getStage()).isFrameDependentAct();
            if (st && fd) { //devo fare sia lo skip tolerant che il frame dependent
                actSkipTolerant(delta);
                actFrameDependent(delta);
            } else if (fd) //faccio solo frmae dependent
                actFrameDependent(delta);
            else //solo skip tolerant
                actSkipTolerant(delta);
        } else {
            actSkipTolerant(delta);
            actFrameDependent(delta);
        }
    }

    public void actSkipTolerant(float delta) {
        super.act(delta); //l'act della classe Actor è skip tolerant
    }

    public void actFrameDependent(float delta) {
    }
}
