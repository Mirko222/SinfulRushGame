package com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.util.Utils;

public class SingleImageBackground extends ScrollingBackground {

    protected Texture img;
    protected float y;
    protected String imgPath;
    protected AssetManager assetManager;
    private boolean imgVisible;

    public SingleImageBackground(CameraController controller, AssetManager assetManager, String image) {
        super();
        PADDING_X = PADDING_Y = 1;
        this.cameraController = controller;
        imgPath = image;
        this.assetManager = assetManager;
        imgVisible = true;
    }

    @Override
    public void onScreenResized() {
        if(img == null) {
            img = assetManager.get(imgPath, Texture.class);
            y = getLowerRestoredYToCover();
        }
    }

    @Override
    public boolean backgroundReady() {
        return img != null;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        if(y + img.getHeight() >= getLowerRestoredYToCover() && imgVisible)
            Utils.repeatImageHorizontally(batch, getLeftRestoredXToCover(), getRightRestoredXToCover(), y, img, img.getWidth(), img.getHeight(), false);
        else
            imgVisible = false;
    }
}
