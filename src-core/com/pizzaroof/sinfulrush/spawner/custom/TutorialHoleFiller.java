package com.pizzaroof.sinfulrush.spawner.custom;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.attacks.Armory;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.spawner.HoleFiller;

public class TutorialHoleFiller extends HoleFiller {

    public TutorialHoleFiller(World2D world, Stage stage, AssetManager assetManager, SoundManager soundManager, Group[] groups, Player player, Group enemiesGroup, Group bonusGroup, CameraController cameraController, Armory armory, Preferences preferences) {
        super(world, stage, assetManager, soundManager, groups, player, enemiesGroup, bonusGroup, cameraController, armory, preferences, null);
    }

    @Override
    public String validityCheckOnNextObject(String obj) {
        if(armory.isUsingSword(false) && obj.equals(Constants.SWORD_BONUS_NAME))
            return null;
        return super.validityCheckOnNextObject(obj);
    }
}
