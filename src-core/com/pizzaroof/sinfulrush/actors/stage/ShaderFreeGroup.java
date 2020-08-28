package com.pizzaroof.sinfulrush.actors.stage;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;

/**gruppo di oggetti immuni allo shader
 * NB: usare con attenzione, è inefficiente perché obbliga uno shader switch*/
public class ShaderFreeGroup extends Group {

    @Override
    public void draw(Batch batch, float alpha) {
        ShaderProgram tmp = null;
        boolean nts = needToSwitch();
        if(nts) { //dobbiamo switchare... allora togli lo shader
            batch.end();
            tmp = batch.getShader();
            batch.setShader(null);
            batch.begin();
        }

        super.draw(batch, alpha);

        if(nts) { //rimetti apposto lo shader
            batch.end();
            batch.setShader(tmp);
            batch.begin();
        }
    }

    /**dobbiamo togliere per forza lo shader?
     * magari in alcune situazioni possiamo stampare senza fare lo switch e ottenere lo stesso risultato, migliorando le performance*/
    public boolean needToSwitch() {
        return true;
    }
}
