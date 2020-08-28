package com.pizzaroof.sinfulrush.spawner.platform.custom;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.spawner.platform.UniformPlatformSpawner;

import java.util.HashMap;

/**pad spawner per cimitero*/
public class CemeteryPadSpawner extends UniformPlatformSpawner {

    /**ci salviamo quante piattaforme abbiamo creato (in realtà non lo manteniamo aggiornato, dopo x piattaforme).
     * questo perché le prime x piattaforme avranno posizione fissa.
     * VA DA 1 A X_SPECIAL_PLATFORMS*/
    private int numCreated;

    /**larghezza della prima piattarforma (terreno)*/
    private static final int GROUND_WIDTH = 700;

    private static final int X_SPECIAL_PLATFORMS = 2; //prime due piattaforme speciali

    private HashMap<String, String> covers;

    public CemeteryPadSpawner(AssetManager assetManager, float viewportWidth, World2D world) {
        super(true, assetManager, viewportWidth, world);
        numCreated = 0;
        covers = new HashMap<>();
    }

    /**aggiunge pad disponibile, associato alla cover*/
    public void addPadAvailable(String pad, String cover) {
        super.addPadAvailable(pad);
        covers.put(pad, cover);
    }

    @Override
    public String getNextCover() {
        return covers.get(nextType.v1);
    }

    @Override
    public Platform generateNextPlatform() {
        if(numCreated >= X_SPECIAL_PLATFORMS)
            return super.generateNextPlatform();

        numCreated++;
        switch (numCreated) {
            case 1: //prima piattaforma
                return Platform.createInvisiblePlatform(world, new Vector2(Constants.VIRTUAL_WIDTH - GROUND_WIDTH * 0.5f, 50), GROUND_WIDTH, 110);
            case 2: //seconda piattaforma
                Vector2 pos = new Vector2((getLastCreatedPlatform().getX() - 100.f) / world.getPixelPerMeter(), 450.f / world.getPixelPerMeter());
                String first = Utils.randChoice(Constants.CEMETERY_PAD_4, Constants.CEMETERY_PAD_1, Constants.CEMETERY_PAD_3);
                return Platform.createPlatform(world, assetManager, pos, first, Utils.randBool(), covers.get(first), frontLayer);
        }
        return null;
    }
}
