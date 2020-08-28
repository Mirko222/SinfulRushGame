package com.pizzaroof.sinfulrush.input;

import com.badlogic.gdx.InputProcessor;
import com.pizzaroof.sinfulrush.screens.GameplayScreen;

public class GameplayInputFilter implements InputProcessor {

    private GameplayScreen screen;

    public GameplayInputFilter(GameplayScreen screen) {
        this.screen = screen;
    }

    @Override
    public boolean keyDown(int keycode) {
        return screen.getGame().isShowingAd();
    }

    @Override
    public boolean keyUp(int keycode) {
        return screen.getGame().isShowingAd();
    }

    @Override
    public boolean keyTyped(char character) {
        return screen.getGame().isShowingAd();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return screen.getGame().isShowingAd();
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return screen.getGame().isShowingAd();
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return screen.getGame().isShowingAd();
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return screen.getGame().isShowingAd();
    }

    @Override
    public boolean scrolled(int amount) {
        return screen.getGame().isShowingAd();
    }
}
