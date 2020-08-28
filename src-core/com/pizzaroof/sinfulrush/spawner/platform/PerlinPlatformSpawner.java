package com.pizzaroof.sinfulrush.spawner.platform;

import com.badlogic.gdx.assets.AssetManager;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.util.PerlinNoise;

/**spawner in cui le x si generano con perlin noise invece che uniformemente*/
public class PerlinPlatformSpawner extends UniformPlatformSpawner{

    private static final int MAX_TIME = 1000;
    private static final float STEP = 0.4f;
    private float actualTime;
    private PerlinNoise perlin;

    /**
     * @param upDirection   deve generarli andando verso l'alto o verso il basso?
     */
    public PerlinPlatformSpawner(boolean upDirection, AssetManager assetManager, float viewportWidth, World2D world) {
        super(upDirection, assetManager, viewportWidth, world);
        perlin = new PerlinNoise(rand.nextLong(), MAX_TIME);
        actualTime = 0;
    }

    @Override
    public float getNextRandomX(int max) {
        float p = perlin.noise01(actualTime); //perlin noise tra 0 e 1
        //System.out.println(p);
        actualTime += STEP; //aggiorna tempo
        if(actualTime > MAX_TIME) //ricomincia se necessario
            actualTime -= MAX_TIME;
        return p * max;
    }
}
