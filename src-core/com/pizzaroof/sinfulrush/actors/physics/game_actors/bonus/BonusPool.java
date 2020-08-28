package com.pizzaroof.sinfulrush.actors.physics.game_actors.bonus;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Pool;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.util.Utils;

/**pool per i bonus*/
public class BonusPool extends Pool<SpriteActor> {

    private String dir;
    private AssetManager assetManager;
    private Stage stage;
    private SoundManager soundManager;

    public BonusPool(String directory, AssetManager assetManager, Stage stage, SoundManager soundManager) {
        this.assetManager = assetManager;
        this.dir = directory;
        this.stage = stage;
        this.soundManager = soundManager;
    }

    @Override
    protected Bonus newObject() {
        Bonus b = Utils.getBonusFromDirectory(dir, assetManager, stage, soundManager);
        b.setPool(this);
        return b;
    }
}
