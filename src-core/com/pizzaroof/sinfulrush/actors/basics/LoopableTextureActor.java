package com.pizzaroof.sinfulrush.actors.basics;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.pizzaroof.sinfulrush.util.Utils;

/**texture actor in cui la texture può essere stampata in loop orizzontalmente*/
public class LoopableTextureActor extends TextureActor {
    
    /**offset iniziale da lasciare prima di iniziare a stampare in loop*/
    private float offsetX;

    /**minima e massima x da coprire*/
    private float minX, maxX;

    public LoopableTextureActor(Texture texture) {
        super(texture);
        offsetX = 0;
    }
    
    public void setOffsetX(float x) {
        this.offsetX = x;
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        Utils.repeatImageHorizontally(batch, minX, maxX, getY(), texture, getWidth(), getHeight(), false, offsetX);
    }
}
