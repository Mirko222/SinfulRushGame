package com.pizzaroof.sinfulrush.actors.stage;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.EnemyCallback;

import java.util.HashSet;

/**stage che consente alcune ottimizzazioni: ad esempio consente di accedere velocemente ai nemici danneggiati, utili agli healer per cercare velocemente
 * un target, e anche per decidere se e quando fare screenshake*/
public class OptimizedStage extends ShaderStage implements EnemyCallback {
    protected HashSet<com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy> damagedEnemies;

    public OptimizedStage(ShaderProgram screenShader) {
        super(screenShader);
        damagedEnemies = new HashSet<>();
    }

    public HashSet<com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy> getDamagedEnemies() {
        return damagedEnemies;
    }

    @Override
    public void onHpChanged(com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy enemy) {
        if(enemy.getHp() > 0 && enemy.getHp() < enemy.getMaxHp())
            damagedEnemies.add(enemy);
        else
            damagedEnemies.remove(enemy);
    }

    @Override
    public void onRemoved(Enemy enemy) {
        damagedEnemies.remove(enemy); //potrebbe essere ancora presente se era un nemico volante: è stato danneggiato e però non viene ucciso, alla fine uscirà comunque dallo schermo
    }
}
