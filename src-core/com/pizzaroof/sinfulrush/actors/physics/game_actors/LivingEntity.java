package com.pizzaroof.sinfulrush.actors.physics.game_actors;

import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.missions.Mission;

/**interfaccia per entità viventi*/
public interface LivingEntity {
    /**restituisce punti vita*/
    int getHp();
    /**restituisce punti vita massimi*/
    int getMaxHp();
    /**si cura di @hp*/
    void heal(int hp);
    /**prende @dmg danni*/
    void takeDamage(int dmg, PhysicSpriteActor attacker, Mission.BonusType damageType);
}
