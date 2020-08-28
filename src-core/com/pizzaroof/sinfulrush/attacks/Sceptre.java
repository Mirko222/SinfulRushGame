package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.util.pools.PowerballPool;

import java.util.HashMap;
import java.util.Map;

/**arma scettro*/
public class Sceptre extends Attack {
    /**map<numero dito, swing>*/
    protected HashMap<Integer, SceptreSwing> swings;

    protected Group effectGroup, enemiesGroup;

    protected int powerMin, powerMax;

    /**pool per le sfere lanciate dallo scettro*/
    protected com.pizzaroof.sinfulrush.util.pools.PowerballPool ballPool;

    /**pool per le sfere piccole create dopo gli impatti*/
    protected com.pizzaroof.sinfulrush.util.pools.PowerballPool smallBallPool;

    /**pool per sfere piccole, ma quelle che vengono lanciate a caso (non sono following)*/
    protected com.pizzaroof.sinfulrush.util.pools.PowerballPool smallBallRandPool;

    protected boolean canSplit;

    protected static final float BALL_RADIUS = 0.2f;
    protected static final float SMALL_BALL_RADIUS = BALL_RADIUS * 0.5f;

    private static final float SPLIT_RANGE = 2f;

    protected AssetManager assetManager;

    public Sceptre(Stage stage, World2D world2D, Group effectGroup, Group enemiesGroup, AssetManager assetManager, int powerMin, int powerMax, SoundManager soundManager) {
        super(stage, world2D, soundManager);
        swings = new HashMap<>();
        this.effectGroup = effectGroup;
        this.enemiesGroup = enemiesGroup;
        this.powerMin = powerMin;
        this.powerMax = powerMax;
        this.assetManager = assetManager;

        smallBallPool = new com.pizzaroof.sinfulrush.util.pools.PowerballPool(com.pizzaroof.sinfulrush.util.pools.PowerballPool.PowerballType.FOLLOWING, world2D, assetManager, SMALL_BALL_RADIUS, null, null);
        smallBallRandPool = new com.pizzaroof.sinfulrush.util.pools.PowerballPool(com.pizzaroof.sinfulrush.util.pools.PowerballPool.PowerballType.STANDARD, world2D, assetManager, SMALL_BALL_RADIUS, null, null);
        ballPool = new com.pizzaroof.sinfulrush.util.pools.PowerballPool(com.pizzaroof.sinfulrush.util.pools.PowerballPool.PowerballType.SPLITTING, world2D, assetManager, BALL_RADIUS, smallBallPool, smallBallRandPool);

        canSplit = false;
        /*Array<SpriteActor> arr = new Array<>();
        for(int i=0; i<1000; i++)
            arr.add(smallBallPool.obtain());
        smallBallPool.freeAll(arr);*/
    }

    public com.pizzaroof.sinfulrush.util.pools.PowerballPool getBallPool() {
        return ballPool;
    }

    public PowerballPool getSmallBallRandPool() {
        return smallBallRandPool;
    }

    public Group getEnemiesGroup() {
        return enemiesGroup;
    }

    public Group getEffectGroup() {
        return effectGroup;
    }

    public void setCanSplit(boolean split) {
        this.canSplit = split;
    }

    public boolean canSplit() {
        return canSplit;
    }

    public float getSplitRange() {
        return canSplit ? SPLIT_RANGE : -1;
    }

    /*@Override
    public void draw(Batch batch, float alpha) {
        for(Map.Entry<Integer, SceptreSwing> swing : swings.entrySet())
            swing.getValue().draw(batch);
    }*/

    protected void createSceptreSwing(int pointer, int x, int y) {
        if(!swings.containsKey(pointer))
            swings.put(pointer, new SceptreSwing(effectGroup, Constants.SCEPTRE_BALL_EFFECT, assetManager, powerMin, powerMax, this, new Vector2(x, y)));
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(pointer >= 1) return false;

        createSceptreSwing(pointer, screenX, screenY);
        swings.get(pointer).onTouchDown(screenX, screenY);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(pointer >= 1) return false;

        createSceptreSwing(pointer, screenX, screenY);
        swings.get(pointer).onTouchUp(screenX, screenY);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(pointer >= 1) return false;

        createSceptreSwing(pointer, screenX, screenY);
        swings.get(pointer).onTouchDragged(screenX, screenY);
        return false;
    }

    public void setPowers(int min, int max) {
        this.powerMin = min;
        this.powerMax = max;
        for(Map.Entry<Integer, SceptreSwing> swing : swings.entrySet())
            swing.getValue().setPowers(powerMin, powerMax);
    }

    public void cleanPools() {
        ballPool.clear();
        smallBallRandPool.clear();
        smallBallPool.clear();
    }
}
