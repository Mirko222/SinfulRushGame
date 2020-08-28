package com.pizzaroof.sinfulrush.actors;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.brashmonkey.spriter.gdx.SpriterData;
import com.pizzaroof.sinfulrush.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;

/**attore che permette di rimuovere un'altro sprite coprendolo prima con del fumo (o in realtà quasiasi animazione).
 * Il file info contiene:
 * width_originale height_originale massima_width_possibile durata percentuale_alla_quale_far_sparire_l'attore*/
public class DisappearSmoke extends SpriterAnimActor {

    /**attore che bisogna far sparire*/
    private SpriteActor disapperingActor;

    /**frazione alla quale far sparire l'attore associato*/
    private float disappearingFrac;

    /**dobbiamo mantenerci anche l'altezza originale: ci serve perché dobbiamo vedere il massimo tra altezza e larghezza,
     * e non fare l'altezza in proporzione alla larghezza*/
    private float originalHeight;

    /**massimo fattore di scala per questo effetto*/
    private float maxScaleFactor;

    /**imposta l'attore che vogliamo far scomparire*/
    public void setDisapperingActor(SpriteActor actor) {
        disapperingActor = actor;

        float scaleFactor = Math.min(Math.max(actor.getWidth() / getOriginalWidth(), actor.getHeight() / originalHeight),
                            maxScaleFactor); //vogliamo coprire tutto l'attore... però non possiamo superare una certa soglia (perchè altrimenti otteniamo cose potenzialmente pixelate)
        setDrawingWidth(getOriginalWidth() * scaleFactor); //setta larghezza di conseguenza

        setSpriterAnimation(0);
        //spriterPlayer.setAngle(new RandomXS128().nextInt(360));
    }

    @Override
    public void actSkipTolerant(float delta) {
        if(spriterPlayer != null && disapperingActor != null) {
            Vector2 tmp = disapperingActor.centerPosition();
            setPositionFromCenter(tmp.x, tmp.y);
        }
        super.actSkipTolerant(delta);
    }

    @Override
    public void onSpriterAnimationEnded(int id) {
        disapperingActor.remove(); //alla fine lo rimuoviamo veramente
        disapperingActor = null;
        remove(); //a fine animazione ce ne andiamo
    }

    @Override
    public void onSpriterAnimationExecuting(int id, int actualFrame, int totFrames) {
        if(actualFrame >= totFrames * disappearingFrac) { //dopo una certa frazione, togliamo l'attore (intuitivamente: deve essere il punto in cui l'effetto ha dimensione massima)
            if(disapperingActor != null) { //dopo un po' rendiamo invisibile l'attore
                disapperingActor.setVisible(false);
                disapperingActor.onDisappearing();
            }
        }
    }

    @Override
    public void setPositionFromCenter(float x, float y) {
        super.setPositionFromCenter(x, y);
        //ASSUMIAMO CHE L'ORIGINE DEGLI EFFETTI SIA AL CENTRO
        if(spriterPlayer != null) {
            spriterPlayer.setPosition(x, y);
        }
    }

    /**setta il massimo fattore per cui si può scalare quest'effetto*/
    public void setMaxScaleFactor(float scaleFactor) {
        maxScaleFactor = scaleFactor;
    }

    public void setOriginalHeight(float h) {
        originalHeight = h;
    }

    public void setDisappearingFrac(float f) {
        disappearingFrac = f;
    }

    /**factory method*/
    public static DisappearSmoke create(String directory, AssetManager assetManager, Stage stage) {
        DisappearSmoke smoke = new DisappearSmoke();
        smoke.setSpriterData(assetManager.get(com.pizzaroof.sinfulrush.util.Utils.sheetEffectScmlPath(directory), SpriterData.class), stage.getBatch());

        float dur = 0;
        try {
            BufferedReader reader = com.pizzaroof.sinfulrush.util.Utils.getInternalReader(Utils.sheetEffectInfoPath(directory));

            String strs[] = reader.readLine().split(" "); //legge roba dal file
            smoke.setOriginalWidth(Float.parseFloat(strs[0]));
            smoke.setOriginalHeight(Float.parseFloat(strs[1]));
            smoke.setMaxScaleFactor(Float.parseFloat(strs[2]) / Float.parseFloat(strs[0]));
            dur = Float.parseFloat(strs[3]);
            smoke.setDisappearingFrac(Float.parseFloat(strs[4]));

            reader.close();
        }catch(IOException e) {
            e.printStackTrace();
        }

        smoke.addSpriterAnimation(0, dur, Animation.PlayMode.NORMAL);
        return smoke;
    }

    @Override
    public void reset() {
        super.reset();
        disapperingActor = null;
    }
}
