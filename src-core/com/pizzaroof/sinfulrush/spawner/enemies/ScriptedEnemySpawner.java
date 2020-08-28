package com.pizzaroof.sinfulrush.spawner.enemies;

import com.badlogic.gdx.math.Vector2;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.util.Pair;
import com.pizzaroof.sinfulrush.audio.SoundManager;

import java.util.HashMap;

/**spawner in cui alcuni nemici sono "scriptati"
 * cioè: possiamo decidere quale sarà l'x-esimo nemico (teoriacamente anche tutti i nemici)*/
public class ScriptedEnemySpawner extends MarkovEnemySpawner {

    private HashMap<Integer, String> scriptedEnemies;
    private int generatedEnemies;

    /**
     * @param slowStart    numero minimo di piattaforme vuote da creare prima di partire
     * @param soundManager
     * @param player
     */
    public ScriptedEnemySpawner(int slowStart, SoundManager soundManager, Player player) {
        super(slowStart, soundManager, player);
        scriptedEnemies = new HashMap<>();
        generatedEnemies = 0;
    }

    @Override
    protected String generateNextType(Pair<String, Vector2> platformType) {
        generatedEnemies++;
        if(scriptedEnemies.containsKey(generatedEnemies-1))
            return scriptedEnemies.get(generatedEnemies-1);
        return super.generateNextType(platformType);
    }

    public void addScriptedEnemy(int num, String enemy) {
        scriptedEnemies.put(num, enemy);
    }


}
