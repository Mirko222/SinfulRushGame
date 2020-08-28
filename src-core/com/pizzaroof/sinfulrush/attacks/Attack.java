package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;

/**classe per un attacco generico dell'utente*/
public class Attack extends SpriteActor implements InputProcessor {

    protected Stage stage;

    protected World2D world;

    /**appoggio, per non ricrearlo sempre*/
    protected Vector3 worldPoint;

    protected SoundManager soundManager;

    public Attack(Stage stage, World2D world2D, SoundManager soundManager) {
        this.stage = stage;
        this.world = world2D;
        worldPoint = new Vector3();
        this.soundManager = soundManager;
    }

    /**converte coordinate da schermo a world (in metri). NB: usa sempre @worldPoint*/
    public Vector3 toWorldPoint(int screenX, int screenY) {
        worldPoint.set(screenX, screenY, 0);
        worldPoint = stage.getCamera().unproject(worldPoint);
        worldPoint.x /= world.getPixelPerMeter();
        worldPoint.y /= world.getPixelPerMeter();
        return worldPoint;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    /**resetta y quando diventa troppo grande*/
    public void resetY(float maxy) {

    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

}
