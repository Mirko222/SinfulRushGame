package com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.pools.DecorationPool;
import com.pizzaroof.sinfulrush.actors.stage.DoubleActActor;
import com.pizzaroof.sinfulrush.screens.MainMenuScreen;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.util.Utils;

import java.util.HashMap;

/**background per livello inferno*/
public class HellBackground extends ScrollingBackground {

    private static final String LEFT_PAD_NAME = "left", RIGHT_PAD_NAME = "right";
    private static final int LAVA_HEIGHT = 775, MOUNTAINS_HEIGHT = 437, TERRAIN_HEIGHT = 758;
    private static final int LEFTPAD_WIDTH = 465, LEFTPAD_HEIGHT = 172, RIGHTPAD_WIDTH = 445, RIGHTPAD_HEIGHT = 173, LEFTPAD_OFF_Y = 800, RIGHTPAD_OFF_Y = 800;

    /**texture per le montagne*/
    private Sprite mountains;

    private Sprite terrain;
    private float terrainY;
    private boolean terrainVisible;

    private AssetManager assetManager;

    private Group backDeckLayer;

    private static final float MIN_METEOR_SPEED = 1050, MAX_METEOR_SPEED = 1350;
    private static final int MIN_METEOR_WIDTH = 230, MAX_METEOR_WIDTH = 473; //230
    private static final float MIN_TIME_BETWEEN_METEORS = 0.2f, MAX_TIME_BETWEEN_METEORS = 2.f; //0.2, 2.9
    private float timeToWaitBetweenMeteors, timeWaitedLastMeteor;
    private static final float METEOR_ASPECT_RATIO_DELTA = 0.005f;
    private static final float METEOR_RATIOS[] = {0.072f, 0.093f};
    private static final String[] METEOR_NAMES = {"meteora1", "meteora2"};
    private static final float METEOR_MIN_ANIM_DUR = 0.3f, METEOR_MAX_ANIM_DUR = 0.7f;
    private HashMap<String, DecorationPool> meteorPool;
    private boolean meteorsLoaded;

    private static final int CLOUD_MIN_SPEED = 150, CLOUD_MAX_SPEED = 250;

    public HellBackground(CameraController controller, AssetManager assetManager, Group frontLayer, Group backDecLayer) {
        super(controller, assetManager, frontLayer, com.pizzaroof.sinfulrush.Constants.HELL_GRADIENT_BG);
        this.assetManager = assetManager;
        buildFrontLayer(assetManager);
        mountains = assetManager.get(com.pizzaroof.sinfulrush.Constants.HELL_DECORATIONS_BG, TextureAtlas.class).createSprite("mountains");
        terrain = null;
        terrainVisible = true;
        this.backDeckLayer = backDecLayer;

        meteorPool = new HashMap<>();
        timeToWaitBetweenMeteors = 5.f + com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_TIME_BETWEEN_METEORS, MAX_TIME_BETWEEN_METEORS);
        timeWaitedLastMeteor = 0;

        meteorsLoaded = false;
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);
        terrainVisible = terrainVisible && terrainY <= cameraController.getRestoredCameraY() + cameraController.getViewportHeight() * 0.5f;

        meteorsLoaded = meteorsLoaded || assetManager.isLoaded(com.pizzaroof.sinfulrush.Constants.HELL_METEORS); //le meteore le carichiamo mentre giochiamo
        if(meteorsLoaded && meteorPool.size() == 0) { //non ho inizializzato la pool delle meteore...
            for(String s : METEOR_NAMES)
                meteorPool.put(s, new DecorationPool(assetManager.get(com.pizzaroof.sinfulrush.Constants.HELL_METEORS), s, false, true, true, .5f, Animation.PlayMode.LOOP_PINGPONG));
        }

        timeWaitedLastMeteor += delta;
        if(timeWaitedLastMeteor >= timeToWaitBetweenMeteors && meteorsLoaded) {
            timeWaitedLastMeteor = 0;
            timeToWaitBetweenMeteors = com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_TIME_BETWEEN_METEORS, MAX_TIME_BETWEEN_METEORS);
            createMeteor();
        }
    }

    private void createMeteor() { //crea meteora a caso
        boolean dir = com.pizzaroof.sinfulrush.util.Utils.randBool(); //true: da sx a dx, false: da dx a sx
        int id = com.pizzaroof.sinfulrush.util.Utils.randInt(0, METEOR_NAMES.length - 1);
        String name = METEOR_NAMES[id];

        MovingDecoration meteor = (MovingDecoration)meteorPool.get(name).obtain();
        meteor.setSpeed(com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_METEOR_SPEED, MAX_METEOR_SPEED));
        meteor.flipVert(com.pizzaroof.sinfulrush.util.Utils.randBool());
        meteor.setWidth(com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_METEOR_WIDTH, MAX_METEOR_WIDTH));
        meteor.setHeight( (com.pizzaroof.sinfulrush.util.Utils.randFloat(-METEOR_ASPECT_RATIO_DELTA, METEOR_ASPECT_RATIO_DELTA) + METEOR_RATIOS[id]) * meteor.getWidth() );
        meteor.setX(dir ? -meteor.getWidth() : cameraController.getViewportWidth());
        meteor.setAnimationDuration(com.pizzaroof.sinfulrush.util.Utils.randFloat(METEOR_MIN_ANIM_DUR, METEOR_MAX_ANIM_DUR));

        float ry = com.pizzaroof.sinfulrush.util.Utils.randFloat(0.65f, 1f);
        float bottomy = cameraController.getCameraY() - cameraController.getViewportHeight() * 0.5f;
        meteor.setY(bottomy + ry * cameraController.getViewportHeight());

        float rx = dir ? com.pizzaroof.sinfulrush.util.Utils.randFloat(0.2f, 1f) : 1.f - com.pizzaroof.sinfulrush.util.Utils.randFloat(0.2f, 1f); //20-100% dello schermo quando va da sinistra a destra, 0-80% dello schermo quando va da destra a sinistra
        meteor.setDirection(new Vector2(cameraController.getViewportWidth() * rx, bottomy).sub(meteor.getX(), meteor.getY()).nor());

        backDeckLayer.addActor(meteor);
    }

    @Override
    public void draw(Batch batch, float alpha) {
        super.draw(batch, alpha);
        //stampa montagne sul background
        com.pizzaroof.sinfulrush.util.Utils.repeatImageHorizontallySprite(batch, getLeftRestoredXToCover(), getRightRestoredXToCover(), getLowerRestoredYToCover(), mountains, com.pizzaroof.sinfulrush.Constants.VIRTUAL_WIDTH, MOUNTAINS_HEIGHT, false, 0);

        if(terrainVisible)
            com.pizzaroof.sinfulrush.util.Utils.repeatImageHorizontally(batch, cameraController.getRestoredCameraX() - cameraController.getViewportWidth() * 0.5f -1,
                        cameraController.getRestoredCameraX() + cameraController.getViewportWidth() * 0.5f +1, terrainY, terrain, com.pizzaroof.sinfulrush.Constants.VIRTUAL_WIDTH, TERRAIN_HEIGHT, false, 0);
    }

    /**crea layer in primo piano*/
    private void buildFrontLayer(AssetManager assetManager) {
        frontLayer.addActor(new Lava(assetManager));
    }

    @Override
    public void onScreenResized() {
        if(terrain == null) {
            terrain = assetManager.get(com.pizzaroof.sinfulrush.Constants.HELL_DECORATIONS_BG, TextureAtlas.class).createSprite("first_background");
            float topy = cameraController.getRestoredCameraY() + cameraController.getViewportHeight() * 0.5f;
            terrainY = topy - TERRAIN_HEIGHT;

            com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.StaticDecoration leftPad = new com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.StaticDecoration(assetManager.get(com.pizzaroof.sinfulrush.Constants.HELL_DECORATIONS, TextureAtlas.class).findRegion(LEFT_PAD_NAME), true);
            com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.StaticDecoration rightPad = new StaticDecoration(assetManager.get(com.pizzaroof.sinfulrush.Constants.HELL_DECORATIONS, TextureAtlas.class).findRegion(RIGHT_PAD_NAME), true);
            leftPad.setWidth(LEFTPAD_WIDTH); leftPad.setHeight(LEFTPAD_HEIGHT);
            rightPad.setWidth(RIGHTPAD_WIDTH); rightPad.setHeight(RIGHTPAD_HEIGHT);
            leftPad.setX(-1); leftPad.setY(topy - LEFTPAD_OFF_Y);
            rightPad.setX(cameraController.getCameraX() + cameraController.getViewportWidth() * 0.5f - RIGHTPAD_WIDTH + 1);
            rightPad.setY(topy - RIGHTPAD_OFF_Y);
            backDeckLayer.addActor(leftPad);
            backDeckLayer.addActor(rightPad);

            createClouds();
        }
    }

    @Override
    public boolean backgroundReady() {
        return terrain != null;
    }

    public float getLavaHeight() {
        return 70;
    }

    /**crea nuvolette iniziali per la prima parte del background*/
    private void createClouds() {
        TextureRegion [] regions = new TextureRegion[2];
        regions[0] = assetManager.get(com.pizzaroof.sinfulrush.Constants.HELL_DECORATIONS_BG, TextureAtlas.class).findRegion("nuvola1");
        regions[1] = assetManager.get(com.pizzaroof.sinfulrush.Constants.HELL_DECORATIONS_BG, TextureAtlas.class).findRegion("nuvola2");

        float x = getStage().getCamera().position.x, y = getStage().getCamera().position.y, w = getStage().getCamera().viewportWidth, h = getStage().getCamera().viewportHeight;
        int num = com.pizzaroof.sinfulrush.util.Utils.randInt(2, 4);
        for(int i=0; i<num; i++) {
            HorizontalDecoration cloud = new HorizontalDecoration(regions[com.pizzaroof.sinfulrush.util.Utils.randInt(0, 1)], false, com.pizzaroof.sinfulrush.util.Utils.randFloat(CLOUD_MIN_SPEED, CLOUD_MAX_SPEED));
            cloud.setWidth(com.pizzaroof.sinfulrush.util.Utils.randFloat(MainMenuScreen.MIN_WIDTH, MainMenuScreen.MAX_WIDTH));
            cloud.setHeight(cloud.getWidth() * com.pizzaroof.sinfulrush.util.Utils.randFloat(MainMenuScreen.MIN_ASP_RATIO, MainMenuScreen.MAX_ASP_RATIO));

            cloud.setY(com.pizzaroof.sinfulrush.util.Utils.randFloat(y + h*0.5f - 450, y + h*0.5f - cloud.getHeight()));
            cloud.setX(com.pizzaroof.sinfulrush.util.Utils.randFloat(x - w*0.5f, x + w*0.5f - cloud.getWidth()));

            if(cloud.getX() + cloud.getWidth()*0.5f > x) cloud.setSpeed(cloud.getSpeed() * -1);
            cloud.flip(com.pizzaroof.sinfulrush.util.Utils.randBool());
            backDeckLayer.addActor(cloud);
        }
    }

    private class Lava extends DoubleActActor {
        TextureRegion lava;
        float offsetX;
        float width;
        private static final float SPEED = 30.f; //pixel al secondo

        Lava(AssetManager assetManager) {
            lava = assetManager.get(com.pizzaroof.sinfulrush.Constants.HELL_DECORATIONS_BG, TextureAtlas.class).findRegion("lava"); //aggiungiamo un oggetto statico: lava sul front layer, e lo facciamo ripetere su y
            offsetX = 0;
            width = Constants.VIRTUAL_WIDTH;
        }

        @Override
        public void actSkipTolerant(float delta) {
            offsetX += (SPEED * delta);
            while(offsetX > width)
                offsetX -= width;
        }

        @Override
        public void draw(Batch batch, float alpha) {
            Utils.repeatImageHorizontally(batch, getLeftRestoredXToCover(), getRightRestoredXToCover(), getLowerRestoredYToCover(), lava, width, LAVA_HEIGHT, false, offsetX);
        }
    }
}
