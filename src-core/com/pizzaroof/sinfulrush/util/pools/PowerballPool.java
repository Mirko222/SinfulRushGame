package com.pizzaroof.sinfulrush.util.pools;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Pool;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.attacks.FollowingPowerball;
import com.pizzaroof.sinfulrush.attacks.Powerball;
import com.pizzaroof.sinfulrush.attacks.SplittingPowerball;
import com.pizzaroof.sinfulrush.attacks.TimescaleWisePowerball;

/**pool per powerball*/
public class PowerballPool extends Pool<SpriteActor> {

    /**tipo della powerball*/
    public enum PowerballType {
        STANDARD,
        FOLLOWING,
        SPLITTING
    }

    private PowerballType type;

    private World2D world2D;
    private float radius;

    private PowerballPool followingPool, randomPool;

    private AssetManager assetManager;

    /**@param type che tipo di powerball vogliamo?*/
    public PowerballPool(PowerballType type, World2D world2D, AssetManager assetManager, float radius, PowerballPool followingPool, PowerballPool randomPool) {
        this.type = type;
        this.world2D = world2D;
        this.radius = radius;
        this.followingPool = followingPool;
        this.randomPool = randomPool;
        this.assetManager = assetManager;
    }

    @Override
    protected Powerball newObject() {
        Powerball ret;
        switch(type) {
            case SPLITTING:
                ret = new SplittingPowerball(world2D, radius, followingPool, randomPool, assetManager);
                break;

            case FOLLOWING:
                ret = new FollowingPowerball(world2D, radius);
                break;

            case STANDARD:
                ret = new TimescaleWisePowerball(world2D, radius);
                break;

            default:
                return null;
        }
        ret.setPool(this);
        return ret;
    }
}
