package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.math.Vector2;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.util.pools.Pools;

/**powerball che diventa più potente col passare della distanza percorsa*/
public class IncreasingPowerball extends TimescaleWisePowerball {

    /**massimo power che può ottenere*/
    protected int powerMin, powerMax;

    protected static final float DIST_FOR_MAX_POWER = 800.f / Constants.PIXELS_PER_METER; //distanza da percorrere per raggiungere massimo valore

    protected Vector2 spawnPoint;


    /**
     * crea powerball
     *
     * @param world
     * @param effect     effetto particellare della powerball
     * @param spawnPoint punto da dove inizia la traiettoria
     * @param speed      velocità della powerball
     * @param power      potere della powerball (è il power minimo)8
     * @param radius
     * @param direction  direzione della powerball
     */
    /*public IncreasingPowerball(World2D world, String effect, Vector2 spawnPoint, float speed, int power, int powerMax, float radius, Vector2 direction) {
        super(world, effect, spawnPoint, speed, power, radius, direction);
        this.powerMax = powerMax;
        this.powerMin = power;
        this.spawnPoint = spawnPoint;
    }*/

    /**chiamare init se usi questo!!!*/
    public IncreasingPowerball(World2D world, float radius) {
        super(world, radius);
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);
        if(isOnStage()) {
            float dist = getBody().getPosition().dst2(spawnPoint);
            float p = Math.min(1.f, dist / (DIST_FOR_MAX_POWER * DIST_FOR_MAX_POWER));
            power = (int)((powerMax - powerMin) * p + powerMin);
        }
    }

    public void init(String effect, Vector2 spawnPoint, float speed, int power, Vector2 direction, int powerMax, com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor color) {
        init(effect, spawnPoint, speed, power, direction, powerMax, true, color);
    }

    public void init(String effect, Vector2 spawnPoint, float speed, int power, Vector2 direction, int powerMax, boolean useParticles, Pools.PEffectColor color) {
        super.init(effect, spawnPoint, speed, power, direction, useParticles, color);
        this.powerMin = power;
        this.powerMax = powerMax;
        this.spawnPoint = spawnPoint;
    }
}
