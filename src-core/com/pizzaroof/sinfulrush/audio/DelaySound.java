package com.pizzaroof.sinfulrush.audio;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.pizzaroof.sinfulrush.Constants;

/**suoni che vengono riprodotti solo aspettando un certo tempo tra una riproduzione e l'altra*/
public class DelaySound {
    private Sound sound;

    private AssetManager assetManager;
    private String name;

    private float delay, delayPassed;

    /**@param delay minimo ritardo tra una riproduzione e l'altra*/
    public DelaySound(AssetManager assetManager, String name, float delay) {
        this.assetManager = assetManager;
        this.name = name;
        this.delay = delay;
        delayPassed = delay + 1;
    }

    public void update(float delta) {
        if(delayPassed < delay)
            delayPassed += delta;
    }

    public void play(float pitch, float pan, float volume) {
        if(!useSfx(volume) || delay - delayPassed > com.pizzaroof.sinfulrush.Constants.EPS) return;

        if(sound == null)
            if(assetManager.isLoaded(name))
                sound = assetManager.get(name, Sound.class);
            else
                return;
        delayPassed = 0;
        sound.play(volume, pitch, pan);
    }

    private boolean useSfx(float volume) {
        return volume >= Constants.EPS;
    }
}
