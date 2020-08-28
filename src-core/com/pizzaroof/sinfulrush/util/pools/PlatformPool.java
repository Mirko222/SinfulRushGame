package com.pizzaroof.sinfulrush.util.pools;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Pool;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;

/**pool per piattaforme (estendiamo da SpriteActor perché ogni SpriteActor ha un riferimento alla propria pool)*/
public class PlatformPool extends Pool<SpriteActor> {

    private World2D world;
    private AssetManager assetManager;
    private String padName;
    private boolean flipped;

    private Group frontLayer;
    private String coverPad;

    public PlatformPool(World2D world, AssetManager assetManager, String padName, boolean flipped) {
        this.world = world;
        this.assetManager = assetManager;
        this.padName = padName;
        this.flipped = flipped;
        frontLayer = null;
        coverPad = null;
    }

    public void setCover(Group layer, String cover) {
        frontLayer = layer;
        coverPad = cover;
    }

    @Override
    protected Platform newObject() {
        Platform plt = Platform.createPlatform(world, assetManager, Vector2.Zero, padName, flipped, coverPad, frontLayer);
        plt.setPool(this);
        return plt;
    }
}
