package com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**decorazione che si muove da sx a dx*/
public class HorizontalDecoration extends MovingDecoration {

    /**@param speed velocità su asse x, in pixel al secondo*/
    public HorizontalDecoration(TextureRegion texture, boolean visible, float speed) {
        super(texture, visible);
        setSpeed(speed);
        setDirection(new Vector2(1, 0));
    }
}
