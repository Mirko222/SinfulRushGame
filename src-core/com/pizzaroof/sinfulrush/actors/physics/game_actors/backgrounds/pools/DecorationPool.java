package com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.pools;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.HorizontalDecoration;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.MovingDecoration;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.StaticDecoration;

public class DecorationPool extends Pool<com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.StaticDecoration> {

    private TextureRegion region;
    private Array<TextureRegion> animation;
    private boolean horizontal, moving;
    private float duration;
    private Animation.PlayMode playMode;

    /**@param hor è horizontal o static decoration?*/
    public DecorationPool(TextureAtlas atlas, String name, boolean hor, boolean moving, boolean isAnimation, float dur, Animation.PlayMode playMode) {
        if(!isAnimation)
            this.region = atlas.findRegion(name);
        else
            this.animation = new Array<>(atlas.findRegions(name));
        this.horizontal = hor;
        this.moving = moving;
        this.duration = dur;
        this.playMode = playMode;
    }

    public DecorationPool(TextureAtlas atlas, String name, boolean hor) {
        this(atlas, name, hor, false, false, 0, null);
    }


    @Override
    protected com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.StaticDecoration newObject() {
        com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.StaticDecoration sd;
        if(horizontal)
            sd = new HorizontalDecoration(region, false, 0);
        else if(moving) {
            if(region == null)
                sd = new com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.MovingDecoration(animation, duration, playMode, false);
            else
                sd = new MovingDecoration(region, false);
        } else
            sd = new StaticDecoration(region, false);
        sd.setMyPool(this);
        return sd;
    }
}
