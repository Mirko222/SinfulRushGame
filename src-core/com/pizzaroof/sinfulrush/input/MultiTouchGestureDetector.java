package com.pizzaroof.sinfulrush.input;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.pizzaroof.sinfulrush.actors.stage.DoubleActActor;

import java.util.HashMap;

/**classe che permette di usare gesture detector, ma con multitouch.
 * NB: non supporta operazioni "a due dita", (anzi attualmente supporta solo il tap multitouch)*/
public class MultiTouchGestureDetector extends DoubleActActor implements InputProcessor {

    /**ogni gesture detector è in grado di gestire un solo tocco... ci teniamo tanti handler per tanti tocchi*/
    protected HashMap<Integer, NGGestureDetector> gesturesDetector;

    public MultiTouchGestureDetector() {
        gesturesDetector = new HashMap<>();
    }

    /**chiamata quando il tocco @pointer tappa su (screenX, screenY)*/
    public boolean tap(float screenX, float screenY, int pointer, int count, int button) {
        return false;
    }

    /***/
    public boolean fling(float velx, float vely, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        addGesture(pointer); //deleghiamo tutto al gesture associato, facendogli credere che il puntatore è 1
        return gesturesDetector.get(pointer).touchDown(screenX, screenY, 0, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        addGesture(pointer);
        return gesturesDetector.get(pointer).touchUp(screenX, screenY, 0, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        addGesture(pointer);
        return gesturesDetector.get(pointer).touchDragged(screenX, screenY, 0);
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
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public void addGesture(int pointer) {
        if(gesturesDetector.containsKey(pointer)) return;
        gesturesDetector.put(pointer, new NGGestureDetector(new GestureDetector.GestureListener() {
            @Override
            public boolean touchDown(float x, float y, int pointer, int button) {
                return false;
            }

            @Override
            public boolean tap(float x, float y, int count, int button) {
                return MultiTouchGestureDetector.this.tap(x, y, pointer, count, button);
            }

            @Override
            public boolean longPress(float x, float y) {
                return false;
            }

            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
                return MultiTouchGestureDetector.this.fling(velocityX, velocityY, pointer, button);
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
                return false;
            }

            @Override
            public boolean panStop(float x, float y, int pointer, int button) {
                return false;
            }

            @Override
            public boolean zoom(float initialDistance, float distance) {
                return false;
            }

            @Override
            public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
                return false;
            }

            @Override
            public void pinchStop() {

            }
        }));
    }
}
