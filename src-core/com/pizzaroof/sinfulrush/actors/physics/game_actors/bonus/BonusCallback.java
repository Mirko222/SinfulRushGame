package com.pizzaroof.sinfulrush.actors.physics.game_actors.bonus;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.pizzaroof.sinfulrush.attacks.Armory;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;

/**callback che riguardano i bonus*/
public abstract class BonusCallback {

    /**callback per quando il bonus viene preso.
     * @param armory può manipolare l'armeria dell'utente (magari gli cambia arma...)
     * @param enemiesGroup gruppo dei nemici
     * @param player può manipolare il giocatore (magari aumenta vita...)*/
    public abstract void onTaken(Armory armory, Group enemiesGroup, Player player);

    /**callback per quando sparisce*/
    public void onDisappear() {}
}
