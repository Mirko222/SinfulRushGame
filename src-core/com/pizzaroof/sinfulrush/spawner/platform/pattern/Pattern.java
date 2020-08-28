package com.pizzaroof.sinfulrush.spawner.platform.pattern;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.util.Pair;

/**classe per un pattern nel generatore di piattaforme*/
public abstract class Pattern {
    /**deve essere specchiato?*/
    protected boolean flip;
    /**quanti elementi mancano da generare?*/
    protected int remaining;

    public Pattern(boolean flip) {
        this.flip = flip;
        remaining = 0;
    }

    public int getRemaining() {
        return remaining;
    }

    /**è possibile applicare questo pattern?*/
    public abstract boolean isPossible(Platform last, float platformWidth, Player player);

    /**ricomincia questo pattern*/
    public abstract void start();

    /**genera prossimo elemento del patter
     * @param spaceUp quanto spazio sopra la piattaforma?
     * @param type di piattaforma da creare (con dimensioni già calcolate)*/
    public abstract PlatformInfo generate(Platform last, Pair<String, Vector2> type, float viewportWidth, RandomXS128 rand, float spaceUp, Player player);
}