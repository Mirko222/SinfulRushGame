package com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.pizzaroof.sinfulrush.actors.stage.DoubleActActor;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.util.Utils;

/**background che copre tutta la view della camera, dando effetto di scorrimento*/
public class ScrollingBackground extends DoubleActActor {

    /**controller della telecamera: ci da informazioni sull'area di schermo da coprire*/
    protected com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController cameraController;

    /**immagine da usare per tappezzare lo spazio della telecamera.
     * Deve andare in loop orizzontalmente, verticalmente si specchia per mandarla in loop*/
    private Texture image;

    /**quanto padding extra coprire oltre allo spazio della telecamera*/
    protected int PADDING_X = 50, PADDING_Y = 12;

    protected Group frontLayer;

    /**y attuale da cui partire a coprire*/
    protected float actY;
    /**quando iniziamo a coprire, partiamo flippando o no?*/
    protected boolean flipVert;

    /**è la prima immagine di background che viene stampata?*/
    protected boolean firstImage;

    /**@param frontLayer layer superiore a tutti gli oggetti (cosi possiamo mettere cose sia in secondo piano, dietro tutto, che in primo piano)*/
    public ScrollingBackground(CameraController controller, AssetManager assetManager, Group frontLayer, String repeatingBackground) {
        this.cameraController = controller;
        image = assetManager.get(repeatingBackground);
        this.frontLayer = frontLayer;
        actY = Float.NaN;
        flipVert = false;
        firstImage = true;
    }

    public ScrollingBackground(){}

    //stampa qui tutto quello che devi, i calcoli dovresti già averli fatti in act
    @Override
    public void draw(Batch batch, float alpha) {
        super.draw(batch, alpha);

        //cameraController.getCameraX() restituisce x della telecamera
        //cameraController.getCameraY() restituisce y della telecamera (le coordinate della camera sono essenzialmente il centro dello schermo)
        //cameraController.getViewportWidth() e cameraController.getViewportHeight() sono larghezza e altezza dello schermo
        //quindi in sostanza, se vuoi le coordiante del punto in basso a sinistra, sono cameraController.getCameraX() - cameraController.getViewportWidth()/2 ecc.. ecc..

        float lowerY = getLowerYToCover();
        float upperY = cameraController.getCameraY() + cameraController.getViewportHeight() / 2.f + PADDING_Y; //y in alto allo schermo

        //ricalcolo costante di actY e flip: dà problemi quando la y viene resettata, si perde per un attimo la sensazione di continuità
        //float actY = (float)Math.floor(lowerY / image.getHeight()) * image.getHeight(); //non partiamo da lowerY, altrimenti non diamo sensazione di scorrimento: tappezziamo con una certa altezza
        //boolean flip = (int)Math.abs(Math.floor(lowerY / image.getHeight())) % 2 == 1; //devo alternare pari e dispari, quindi controllo se quella da cui parto è pari o dispari

        //invece di ricalcolarlo sempre: li aggiorniamo gradualmente, questo ci permette di mantenere continuità anche quando facciamo salti

        if(Float.isNaN(actY)) { //acty non è inizializzata... la ricalcoliamo immaginando di tappezzare con l'immagine finché possiamo
            actY = lowerY;
        }

        float mul = actY <= lowerY ? 1 : -1;

        if(lowerY - actY > (image.getHeight() + PADDING_Y) * mul) { //actY è "molto" più in basso di dove dobbiamo coprire veramente (in sostanza, stiamo stampando immagini in eccesso)
            actY += (mul * image.getHeight()); //spostiamo l'y attuale
            flipVert = !flipVert; //e ricordiamoci di invertire flipVert
            firstImage = false;
        }

        boolean flip = flipVert; //tappezziamo verticalmente partendo da actY
        float tmpActY = actY;
        int count = 0;

        while(tmpActY < upperY) { //continuiamo a stampare finché non copriamo tutto in verticale
            Texture img = getImage(count == 0 && firstImage);
            Utils.repeatImageHorizontally(batch, getLeftXToCover(), getRightXToCover(), tmpActY, img, img.getWidth(), img.getHeight(), flip); //copri questo livello orizzontale
            tmpActY += (image.getHeight()-5);
            flip = !flip;
            count++;
        }
    }

    /**x a sinistra dello schermo*/
    protected float getLeftXToCover() {
        return cameraController.getCameraX() - cameraController.getViewportWidth() / 2.f - PADDING_X; //x a sinistra dello schermo
    }

    /**x a destra dello schermo*/
    protected float getRightXToCover() {
        return cameraController.getCameraX() + cameraController.getViewportWidth() / 2.f + PADDING_X;
    }

    /**y in basso da coprire*/
    protected float getLowerYToCover() {
        return cameraController.getCameraY() - cameraController.getViewportHeight() / 2.f - PADDING_Y; //y in basso allo schermo
    }

    /**come getLowerY, ma usa la posizione della camera restored (senza screenshake o altro)*/
    protected float getLowerRestoredYToCover() {
        return cameraController.getRestoredCameraY() - cameraController.getViewportHeight() / 2.f - PADDING_Y;
    }

    /**x a sinistra dello schermo*/
    protected float getLeftRestoredXToCover() {
        return cameraController.getRestoredCameraX() - cameraController.getViewportWidth() / 2.f - PADDING_X; //x a sinistra dello schermo
    }

    /**x a destra dello schermo*/
    protected float getRightRestoredXToCover() {
        return cameraController.getRestoredCameraX() + cameraController.getViewportWidth() / 2.f + PADDING_X;
    }

    /**resetta y, se diventa troppo grande*/
    public void resetY(float maxy) {
        actY -= maxy;
    }


    public void onScreenResized() {
    }

    /**@param first è la prima immagine di tutto il livello che viene stampata?*/
    protected Texture getImage(boolean first) {
        return image;
    }

    /**è tutto pronto nel background?*/
    public boolean backgroundReady() {
        return true;
    }
}
