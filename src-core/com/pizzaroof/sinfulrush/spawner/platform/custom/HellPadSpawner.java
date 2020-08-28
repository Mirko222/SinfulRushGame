package com.pizzaroof.sinfulrush.spawner.platform.custom;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.spawner.platform.PatternPlatformSpawner;


public class HellPadSpawner extends PatternPlatformSpawner {

    /**ci salviamo quante piattaforme abbiamo creato (in realtà non lo manteniamo aggiornato, dopo x piattaforme).
     * questo perché le prime x piattaforme avranno posizione fissa.
     * VA DA 1 A X_SPECIAL_PLATFORMS*/
    private int numCreated;

    /**larghezza della prima piattarforma (terreno)*/
    private static final int GROUND_WIDTH = 780;

    private static final int X_SPECIAL_PLATFORMS = 2; //prime due piattaforme speciali

    public HellPadSpawner(AssetManager assetManager, float viewportWidth, World2D world, float patternProb) {
        super(false, assetManager, viewportWidth, world, patternProb);
        numCreated = 0;
    }

    @Override
    public Platform generateNextPlatform() {
        if(numCreated >= X_SPECIAL_PLATFORMS)
            return super.generateNextPlatform();

        numCreated++;
        switch (numCreated) {
            case 1: //prima piattaforma
                if(Utils.randBool()) //a caso tra sinistra e destra
                    return Platform.createInvisiblePlatform(world, new Vector2(50, 0), GROUND_WIDTH, 110);
                return Platform.createInvisiblePlatform(world, new Vector2(viewportWidth - 50, 0), GROUND_WIDTH, 110);
            case 2: //seconda piattaforma
                float x = getLastCreatedPlatform().getX() < viewportWidth * 0.5f ? //metto la seconda piattaforma in base a dove sta la prima
                        getLastCreatedPlatform().getX() + getLastCreatedPlatform().getWidth() + 100 :
                        getLastCreatedPlatform().getX() - 100;
                Vector2 pos = new Vector2(x / world.getPixelPerMeter(), -400.f / world.getPixelPerMeter());
                String first = Utils.randChoice(Constants.HELL_PAD_1, Constants.HELL_PAD_2, Constants.HELL_PAD_4);
                return Platform.createPlatform(world, assetManager, pos, first, Utils.randBool(), null, frontLayer);
        }
        return null;
    }

    public int getNumCreated() {
        return numCreated;
    }
}
