package com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies;

/**callback per alcune operazioni sugli enemy*/
public interface EnemyCallback {
    /**chiamata quando @enemy cambia i suoi hp (sia per danni che per cure)*/
    void onHpChanged(Enemy enemy);

    /**chiamato quando viene rimosso dallo stage*/
    void onRemoved(Enemy enemy);
}
