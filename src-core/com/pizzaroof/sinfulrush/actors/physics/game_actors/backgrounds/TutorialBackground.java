package com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.util.Utils;

/**background per il gameplay di tutorial*/
public class TutorialBackground extends SingleImageBackground {

    public static final float MIN_ASP_RATIO = 0.25f, MAX_ASP_RATIO = 0.35f, MIN_WIDTH = 150, MAX_WIDTH = 275, MIN_SPEED = 200, MAX_SPEED = 300, MIN_WAIT = 0.3f, MAX_WAIT = 2.5f;
    private float timeToWait, timeWaited;
    private TextureRegion [] cloudRegs;

    private Group backgroundLayer;

    public TutorialBackground(CameraController controller, AssetManager assetManager, Group backgroundLayer) {
        super(controller, assetManager, com.pizzaroof.sinfulrush.Constants.MENU_BACKGROUND);
        cloudRegs = new TextureRegion[2];
        cloudRegs[0] = assetManager.get(com.pizzaroof.sinfulrush.Constants.HELL_DECORATIONS_BG, TextureAtlas.class).findRegion("nuvola1");
        cloudRegs[1] = assetManager.get(Constants.HELL_DECORATIONS_BG, TextureAtlas.class).findRegion("nuvola2");
        timeToWait = timeWaited = 0;
        this.backgroundLayer = backgroundLayer;
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);

        timeWaited += delta;
        if(timeWaited >= timeToWait) {
            createRandomCloud();
            timeWaited = 0;
            timeToWait = com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_WAIT, MAX_WAIT);
        }
    }

    private void createRandomCloud() {
        boolean dir = com.pizzaroof.sinfulrush.util.Utils.randBool();
        HorizontalDecoration cloud = new HorizontalDecoration(cloudRegs[com.pizzaroof.sinfulrush.util.Utils.randInt(0, 1)], false, (dir ? 1 : -1) * com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_SPEED, MAX_SPEED));
        cloud.setWidth(com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_WIDTH, MAX_WIDTH));
        cloud.setHeight(cloud.getWidth() * com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_ASP_RATIO, MAX_ASP_RATIO));

        float y = cameraController.getRestoredCameraY(), h = cameraController.getViewportHeight();
        cloud.setY(com.pizzaroof.sinfulrush.util.Utils.randFloat(Math.max(330, y - h*0.5f), y + h * 0.5f - cloud.getHeight()));
        cloud.setX(dir ? -cloud.getWidth() : getStage().getCamera().viewportWidth);
        cloud.flip(Utils.randBool());
        backgroundLayer.addActor(cloud);
    }
}
