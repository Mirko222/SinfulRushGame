package com.pizzaroof.sinfulrush.actors.physics.game_actors;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.screens.custom.TutorialScreen;
import com.pizzaroof.sinfulrush.util.PlayerPower;
import com.pizzaroof.sinfulrush.util.Utils;

import java.io.IOException;

public class TutorialPlayer extends JuicyPlayer {
    TutorialScreen screen;

    private TutorialPlayer(com.pizzaroof.sinfulrush.screens.custom.TutorialScreen screen, com.pizzaroof.sinfulrush.actors.physics.World2D world, Stage stage, SoundManager soundManager, float density, Vector2 initPosition, float speed, String directory, AssetManager assetManager, boolean canVibrate, Group effectGroup, Shape... shapes) {
        super(world, stage, soundManager, density, initPosition, speed, directory, assetManager, new PlayerPower(), canVibrate, effectGroup, shapes);
        this.screen = screen;
    }

    @Override
    public void takeDamage(int dmg, PhysicSpriteActor attacker) {
        if(dmg < getHp()) //non può morire
            super.takeDamage(dmg, attacker);
    }

    @Override
    public void heal(int h) {
        super.heal(h);
        screen.onPlayerHeal();
    }

    @Override
    public void onSavedFriend() {
        screen.onSavedFriend();
    }

    /**factory method per creare un giocatore dalla sua cartella*/
    public static TutorialPlayer createPlayer(String directory, TutorialScreen screen, World2D world, float density, Vector2 initPosition, float speed, AssetManager assetManager, Stage stage, boolean canVibrate, SoundManager soundManager, Group effectGroup, String soundtrackName) throws IOException {
        String strs[] = com.pizzaroof.sinfulrush.util.Utils.getInternalReader(com.pizzaroof.sinfulrush.util.Utils.playerInfoPath(directory)).readLine().split(" "); //legge dimensioni player dal file info
        float bbw = Float.parseFloat(strs[1]); //in realtà leggiamo le dimensioni di drawing
        float bbh = Float.parseFloat(strs[2]);
        Shape shapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(Utils.playerShapePath(directory), bbw, bbh, world.getPixelPerMeter()); //crea shapes
        return new TutorialPlayer(screen, world, stage, soundManager, density, initPosition, speed, directory, assetManager, canVibrate, effectGroup, shapes); //ora possiamo creare player
    }
}
