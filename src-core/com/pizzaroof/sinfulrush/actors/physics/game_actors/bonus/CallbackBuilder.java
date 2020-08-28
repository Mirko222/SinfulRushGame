
package com.pizzaroof.sinfulrush.actors.physics.game_actors.bonus;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.FlyingSniperEnemy;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.FriendEnemy;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.PlatformEnemy;
import com.pizzaroof.sinfulrush.actors.stage.TimescaleStage;
import com.pizzaroof.sinfulrush.attacks.Armory;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.util.Utils;

import java.util.HashMap;

/**classe in grado di creare le callback per i bonus*/
public class CallbackBuilder {

    /**usato per non far avvenire gli effetti contemporaneamente*/
    private static final float MAX_DELAY_INCR = 0.1f;

    /**hp che rigenerno le varie pozioni*/
    private static final int SMALL_HP = 15, MEDIUM_HP = 35, LARGE_HP = 100;

    /**parametri per bonus che rallenta tempo*/
    private static final float SLOW_TIME_MUL = 0.45f, SLOW_TIME_DUR = 4f;

    /**numero di nemici uccisi per arrivare al massimo shake*/
    private static final float MAX_SHAKE_KILLED_ENEMIES = 10;

    /**massimo numero di dita in generale per le armi*/
    private static final int MAX_FINGERS = 2;

    /**restituisce la callback di un bonus data la directory*/
    public static BonusCallback getBonusCallback(String name, AssetManager assetManager, Stage stage, Group effectGroup, HashMap<String, Integer> bonusCount, CameraController cameraController, SoundManager soundManager) {
        if(name.equals(com.pizzaroof.sinfulrush.Constants.GLOVE_BONUS_NAME)) { //guanto
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, com.pizzaroof.sinfulrush.actors.physics.game_actors.Player player) {
                    armory.setGloveAttack();
                }

                @Override
                public void onDisappear() {
                    bonusCount.put(name, bonusCount.get(name) - 1);
                }
            };
        }
        if(name.equals(Constants.SWORD_BONUS_NAME)) { //spada normale
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, Player player) {
                    armory.setSwordAttack(false, 1, Color.WHITE);
                }

                @Override
                public void onDisappear() {
                    bonusCount.put(name, bonusCount.get(name) - 1);
                }
            };
        }
        if(name.equals(Constants.DOUBLE_SWORD_BONUS_NAME)) {
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, Player player) {
                    armory.setSwordAttack(false, MAX_FINGERS, Color.WHITE);
                }

                @Override
                public void onDisappear() {
                    bonusCount.put(Constants.SWORD_BONUS_NAME, bonusCount.get(Constants.SWORD_BONUS_NAME) - 1);
                }
            };
        }
        if(name.equals(Constants.RAGE_SWORD_BONUS_NAME)) { //doppia spada con rage
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, com.pizzaroof.sinfulrush.actors.physics.game_actors.Player player) {
                    armory.setSwordAttack(true, 1, new Color(0.21f, 0.21f, 0.21f, 1.f));
                }

                @Override
                public void onDisappear() {
                    bonusCount.put(name, bonusCount.get(name) - 1);
                }
            };
        }
        if(name.equals(Constants.DOUBLE_RAGE_SWORD_BONUS_NAME)) { //doppia spada con rage
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, Player player) {
                    armory.setSwordAttack(true, MAX_FINGERS, new Color(0.21f, 0.21f, 0.21f, 1.f));
                }

                @Override
                public void onDisappear() {
                    bonusCount.put(Constants.RAGE_SWORD_BONUS_NAME, bonusCount.get(Constants.RAGE_SWORD_BONUS_NAME) - 1);
                }
            };
        }
        if(name.equals(Constants.SCEPTRE_BONUS_NAME)) { //scettro
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, Player player) {
                    armory.setSceptreAttack(false);
                }

                @Override
                public void onDisappear() {
                    bonusCount.put(name, bonusCount.get(name) - 1);
                }
            };
        }
        if(name.equals(Constants.SCEPTRE_SPLIT_BONUS_NAME)) { //scettro con split
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, Player player) {
                    armory.setSceptreAttack(true);
                }

                @Override
                public void onDisappear() {
                    bonusCount.put(name, bonusCount.get(name) - 1);
                }
            };
        }

        if(name.equals(Constants.LIGHTNING_BONUS_NAME)) { //fulmine: uccide tutti i nemici su piattaforme
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, com.pizzaroof.sinfulrush.actors.physics.game_actors.Player player) {
                    int numKilled = 0;
                    float delay = 0; //ritardo con cui far saltare i nemici
                    for(Actor e : enemiesGroup.getChildren()) //facciamo saltare in aria i nemici su piattaforma nella view
                        if(e instanceof PlatformEnemy && ((PlatformEnemy) e).isInCameraView() &&
                            !(e instanceof FriendEnemy)) {
                            ((PlatformEnemy) e).hitByLightning(delay);
                            delay += com.pizzaroof.sinfulrush.util.Utils.randFloat(0, MAX_DELAY_INCR); //incrementa a caso man mano il ritardo
                            numKilled++;
                        }
                    if(numKilled > 0) {
                        float trauma = Math.min(1.f, numKilled / MAX_SHAKE_KILLED_ENEMIES) * com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController.HIGH_TRAUMA;
                        trauma = Math.max(trauma, com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController.MEDIUM_TRAUMA);
                        cameraController.setIncresingTrauma(trauma);
                        soundManager.thunder();
                    }
                }
            };
        }
        if(name.equals(Constants.WIND_BONUS_NAME)) { //vento: uccide tutti i nemici volanti
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, com.pizzaroof.sinfulrush.actors.physics.game_actors.Player player) {
                    float delay = 0;
                    int numKilled = 0;
                    for(Actor e : enemiesGroup.getChildren()) //nemici da uccidere: volanti nella view
                        if(e instanceof FlyingSniperEnemy && ((Enemy)e).isInCameraView()) {
                            ((FlyingSniperEnemy) e).flyAway(delay);
                            delay += Utils.randFloat(0, MAX_DELAY_INCR);
                            numKilled++;
                        }
                    if(numKilled > 0) {
                        float trauma = Math.min(1.f, numKilled / MAX_SHAKE_KILLED_ENEMIES) * com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController.HIGH_TRAUMA;
                        trauma = Math.max(trauma, CameraController.MEDIUM_TRAUMA);
                        cameraController.setIncresingTrauma(trauma);
                        soundManager.wind();
                    }
                }
            };
        }

        if(name.equals(Constants.ELISIR_S_BONUS_NAME)) {
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, com.pizzaroof.sinfulrush.actors.physics.game_actors.Player player) {
                    player.heal(SMALL_HP);
                }
            };
        }

        if(name.equals(Constants.ELISIR_M_BONUS_NAME)) {
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, com.pizzaroof.sinfulrush.actors.physics.game_actors.Player player) {
                    player.heal(MEDIUM_HP);
                }
            };
        }

        if(name.equals(Constants.ELISIR_L_BONUS_NAME)) {
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, com.pizzaroof.sinfulrush.actors.physics.game_actors.Player player) {
                    player.heal(LARGE_HP);
                }
            };
        }

        if(name.equals(Constants.SLOWTIME_BONUS_NAME)) { //rallenta tempo
            if(stage instanceof TimescaleStage) { //ci serve un timescale stage
                return new BonusCallback() {
                    @Override
                    public void onTaken(Armory armory, Group enemiesGroup, com.pizzaroof.sinfulrush.actors.physics.game_actors.Player player) {
                        ((TimescaleStage)stage).setTemporalyTimeMultiplier(SLOW_TIME_MUL, SLOW_TIME_DUR);
                        soundManager.slowtime();
                    }
                };
            }
        }

        if(name.equals(Constants.ICE_BONUS_NAME)) { //bonus ghiaccio
            return new BonusCallback() {
                @Override
                public void onTaken(Armory armory, Group enemiesGroup, Player player) {
                    for(Actor e : enemiesGroup.getChildren())
                        if(e instanceof Enemy && ((Enemy)e).isInCameraView() && ((Enemy) e).getHp() > 0 &&
                            !(e instanceof FriendEnemy))
                            ((Enemy) e).freeze();
                    soundManager.iceSpawn();
                }
            };
        }

        return null;
    }

    /**tipo di bonus*/
    public static Mission.BonusType getBonusType(String name) {
        if(name.equals(Constants.GLOVE_BONUS_NAME))
            return Mission.BonusType.PUNCH;
        if(name.equals(Constants.SWORD_BONUS_NAME) || name.equals(Constants.RAGE_SWORD_BONUS_NAME) ||
            name.equals(Constants.DOUBLE_RAGE_SWORD_BONUS_NAME) || name.equals(Constants.DOUBLE_SWORD_BONUS_NAME))
            return Mission.BonusType.SWORD;
        if(name.equals(Constants.SCEPTRE_BONUS_NAME) || name.equals(Constants.SCEPTRE_SPLIT_BONUS_NAME))
            return Mission.BonusType.SCEPTRE;

        if(name.equals(Constants.LIGHTNING_BONUS_NAME))
            return Mission.BonusType.LIGHTNING;
        if(name.equals(Constants.WIND_BONUS_NAME))
            return Mission.BonusType.WIND;
        if(name.equals(Constants.ICE_BONUS_NAME))
            return Mission.BonusType.ICE;
        if(name.equals(Constants.SLOWTIME_BONUS_NAME))
            return Mission.BonusType.SLOWTIME;
        if(name.equals(Constants.ELISIR_L_BONUS_NAME) || name.equals(Constants.ELISIR_M_BONUS_NAME) || name.equals(Constants.ELISIR_S_BONUS_NAME))
            return Mission.BonusType.HEAL;

        return Mission.BonusType.MAG_STONE; //ci sono alcuni bonus che non salviamo esplicitamente, ma come bonus generici
    }

}
