package com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.pools.DecorationPool;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.util.Utils;

import java.util.HashMap;

/**background per cimitero*/
public class CemeteryBackground extends ScrollingBackground {

    /**dati per la luna*/
    private static final String MOON_NAME = "luna";
    private static final int MOON_WIDTH = 192, MOON_HEIGHT = 192;
    private static final int MIN_MOON_Y = 1448, MOON_PADDING = 80;
    private static final float INTERPOLATION_STRENGHT = 1.7f;
    private float moonOffsetX, moonOffsetY, actualMoonY;
    private TextureRegion moonTexture;

    /**dati per albero a destra*/
    private static final String TREE_RIGHT_NAME = "albero_destra";
    private static final int TREE_RIGHT_WIDTH = 757, TREE_RIGHT_HEIGHT = 622, TREE_RIGHT_X_OFFSET = 337, TREE_RIGHT_Y_OFFSET = 35;
    private StaticDecoration treeRight;

    /**dati per albero a sinistra*/
    private static final String TREE_LEFT_NAME = "albero_sinistra";
    private static final int TREE_LEFT_WIDTH = 331, TREE_LEFT_HEIGHT = 321, TREE_LEFT_X_OFFSET = 172, TREE_LEFT_Y_OFFSET = 124;
    private StaticDecoration treeLeft;
    private boolean treeLeftVisible;

    /**dati per terreno*/
    private static final String TERRAIN_NAME = "piattaforma";
    private static final int TERRAIN_WIDTH = 1080, TERRAIN_HEIGHT = 211;
    private TextureRegion terrainTexture;
    private static final int TERRAIN_PADDING = 5;
    private float terrainY;
    private boolean terrainVisible;

    /**dati per nuvolette*/
    private static final float MIN_CLOUD_SPEED = 210, MAX_CLOUD_SPEED = 290; //340, 520
    private static final float CLOUD_ASPECT_RATIO = 0.3f, CLOUD_ASPECT_RATIO_DELTA = 0.05f;
    private static final float MAX_CLOUD_WIDTH = 300, MIN_CLOUD_WIDTH = 180;
    private static final float MIN_TIME_BETWEEN_CLOUDS = 0.2f, MAX_TIME_BETWEEN_CLOUDS = 2.9f; //0.3 3.f
    private float timeToWaitBetweenClouds, timeWaitedLastCloud;
    private static final String[] CLOUD_NAMES = {"nuvola1", "nuvola2", "nuvola3", "nuvola4"};
    private HashMap<String, DecorationPool> cloudPool;

    /**prima immagine è diversa, solo per quando si parte*/
    private Texture firstGradient;

    private int tmpPadX;

    private AssetManager assetManager;

    private Group backgroundLayer;

    public CemeteryBackground(CameraController controller, AssetManager assetManager, Group frontLayer, Group backgroundLayer) {
        super(controller, assetManager, frontLayer, com.pizzaroof.sinfulrush.Constants.CEMETERY_GRADIENT_BG);
        firstGradient = assetManager.get(com.pizzaroof.sinfulrush.Constants.CEMETERY_FIRST_GRADIENT_BG);
        tmpPadX = PADDING_X;
        this.assetManager = assetManager;
        this.backgroundLayer = backgroundLayer;
        terrainVisible = true;
        treeLeftVisible = true;

        timeToWaitBetweenClouds = 5.f; // tempo iniziale da aspettare
        timeWaitedLastCloud = 0.f;
        cloudPool = new HashMap<>();
        for(String s : CLOUD_NAMES)
            cloudPool.put(s, new DecorationPool(assetManager.get(com.pizzaroof.sinfulrush.Constants.CEMETERY_DECORATIONS), s, true));
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);
        float destinationMoonY = cameraController.getCameraY() + moonOffsetY;
        actualMoonY += (destinationMoonY - actualMoonY) * INTERPOLATION_STRENGHT * delta;
        actualMoonY = Math.min(actualMoonY, destinationMoonY);

        terrainVisible = terrainVisible && (terrainY + TERRAIN_HEIGHT >= cameraController.getCameraY() - cameraController.getViewportHeight() * 0.5f);

        treeLeftVisible = treeLeftVisible && (treeLeft.getY() + treeLeft.getHeight() >= cameraController.getCameraY() - cameraController.getViewportHeight() * 0.5f);

        timeWaitedLastCloud += delta;
        if(timeWaitedLastCloud >= timeToWaitBetweenClouds) {
            createCloud();
            timeWaitedLastCloud = 0.f;
            timeToWaitBetweenClouds = com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_TIME_BETWEEN_CLOUDS, MAX_TIME_BETWEEN_CLOUDS);
        }
    }

    @Override
    public void draw(Batch batch, float alpha) {
        super.draw(batch, alpha);

        float moonX = cameraController.getCameraX() + moonOffsetX; //stampa luna
        batch.draw(moonTexture, moonX, actualMoonY, MOON_WIDTH, MOON_HEIGHT);

        if(treeLeftVisible)
            treeLeft.draw(batch, alpha);

        if(terrainVisible)
            com.pizzaroof.sinfulrush.util.Utils.repeatImageHorizontally(batch, -TERRAIN_PADDING, cameraController.getViewportWidth()+TERRAIN_PADDING, terrainY,
                                            terrainTexture, TERRAIN_WIDTH, TERRAIN_HEIGHT, false);

    }

    /**crea luna*/
    private void createMoon(AssetManager assetManager) {
        moonTexture = assetManager.get(com.pizzaroof.sinfulrush.Constants.CEMETERY_DECORATIONS, TextureAtlas.class).findRegion(MOON_NAME);
        moonOffsetX = com.pizzaroof.sinfulrush.util.Utils.randFloat(MOON_PADDING, cameraController.getViewportWidth() - MOON_WIDTH - MOON_PADDING) - cameraController.getViewportWidth() * 0.5f;
        moonOffsetY = com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_MOON_Y, cameraController.getViewportHeight() - MOON_HEIGHT - MOON_PADDING) - cameraController.getViewportHeight() * 0.5f;
        actualMoonY = cameraController.getCameraY() + moonOffsetY;
    }

    /**crea albero destro*/
    private void createRightTree(AssetManager assetManager) {
        if(treeRight == null) {
            treeRight = new StaticDecoration(assetManager.get(com.pizzaroof.sinfulrush.Constants.CEMETERY_DECORATIONS, TextureAtlas.class).findRegion(TREE_RIGHT_NAME), true);
            treeRight.setX(cameraController.getViewportWidth() - TREE_RIGHT_X_OFFSET);
            treeRight.setY(cameraController.getCameraY() - cameraController.getViewportHeight() * 0.5f - TREE_RIGHT_Y_OFFSET);
            treeRight.setWidth(TREE_RIGHT_WIDTH);
            treeRight.setHeight(TREE_RIGHT_HEIGHT);
            frontLayer.addActor(treeRight);
        }
    }

    /**crea albero sinistro*/
    private void createLeftTree(AssetManager assetManager) {
        if(treeLeft == null) {
            treeLeft = new StaticDecoration(assetManager.get(com.pizzaroof.sinfulrush.Constants.CEMETERY_DECORATIONS, TextureAtlas.class).findRegion(TREE_LEFT_NAME), true);
            treeLeft.setX(-TREE_LEFT_X_OFFSET);
            treeLeft.setY(cameraController.getCameraY() - cameraController.getViewportHeight() * 0.5f + TREE_LEFT_Y_OFFSET);
            treeLeft.setWidth(TREE_LEFT_WIDTH);
            treeLeft.setHeight(TREE_LEFT_HEIGHT);
        }
    }

    /**crea il terreno*/
    private void createTerrain() {
        if(terrainTexture == null) {
            terrainTexture = assetManager.get(Constants.CEMETERY_DECORATIONS, TextureAtlas.class).findRegion(TERRAIN_NAME);
            terrainY = cameraController.getCameraY() - cameraController.getViewportHeight() * 0.5f;
        }
    }

    private void createCloud() {
        boolean dir = com.pizzaroof.sinfulrush.util.Utils.randBool(); //true: da sx a dx, false: da dx a sx
        String name = com.pizzaroof.sinfulrush.util.Utils.randChoice(CLOUD_NAMES);

        HorizontalDecoration cloud = (HorizontalDecoration)cloudPool.get(name).obtain();
        cloud.setSpeed((dir ? 1 : -1) * com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_CLOUD_SPEED, MAX_CLOUD_SPEED));
        cloud.flip(com.pizzaroof.sinfulrush.util.Utils.randBool());
        cloud.setWidth(com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_CLOUD_WIDTH, MAX_CLOUD_WIDTH));
        cloud.setHeight( (com.pizzaroof.sinfulrush.util.Utils.randFloat(-CLOUD_ASPECT_RATIO_DELTA, CLOUD_ASPECT_RATIO_DELTA) + CLOUD_ASPECT_RATIO) * cloud.getWidth() );
        cloud.setX(dir ? -cloud.getWidth() : cameraController.getViewportWidth());

        float ry = com.pizzaroof.sinfulrush.util.Utils.randGaussian(0.75f, 0.25f); //media a 3/4 di schermo, con deviazione standard di 1/4 di schermo
        if(ry > 1.f) ry = com.pizzaroof.sinfulrush.util.Utils.randFloat(0.8f, 1.f); //gestisce situazioni > 1 o < 0
        if(ry < 0.f) ry = Utils.randFloat(0.f, 0.2f);
        float bottomy = cameraController.getCameraY() - cameraController.getViewportHeight() * 0.5f;

        cloud.setY(bottomy + ry * (cameraController.getViewportHeight() -  cloud.getHeight()));
        backgroundLayer.addActor(cloud);
    }

    @Override
    public void onScreenResized() {
        createMoon(assetManager);
        createRightTree(assetManager);
        createLeftTree(assetManager);
        createTerrain();
    }

    @Override
    public void resetY(float maxy) {
        super.resetY(maxy);
        actualMoonY -= maxy;
    }

    @Override
    public Texture getImage(boolean first) {
        if(first) {
            PADDING_X = 0;
            return firstGradient;
        }
        PADDING_X = tmpPadX;
        return super.getImage(false);
    }

    @Override
    public boolean backgroundReady() {
        return moonTexture != null;
    }
}
