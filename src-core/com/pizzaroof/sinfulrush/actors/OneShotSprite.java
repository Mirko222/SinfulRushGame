package com.pizzaroof.sinfulrush.actors;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.brashmonkey.spriter.gdx.SpriterData;

/**sprite che esegue un'animazione e termina subito*/
public class OneShotSprite extends SpriterAnimActor {

    /**usa animazione spriter?*/
    private boolean spriterShot;

    /**versione con spriter*/
    public OneShotSprite(SpriterData data, Batch batch, int anim, float dur) {
        super();
        spriterShot = true;
        setSpriterData(data, batch);
        addSpriterAnimation(anim, dur, Animation.PlayMode.NORMAL);
        setSpriterAnimation(anim);
    }

    public OneShotSprite(TextureAtlas atlas, String regName, float dur) {
        super();
        spriterShot = false;
        addAnimationFromAtlas(0, atlas, regName, dur, Animation.PlayMode.NORMAL);
        setAnimation(0);
    }

    @Override
    public void onSpriterAnimationEnded(int id) {
        if(spriterShot)
            remove();
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);
        if(!spriterShot && isAnimationEnded())
            remove();
    }
}
