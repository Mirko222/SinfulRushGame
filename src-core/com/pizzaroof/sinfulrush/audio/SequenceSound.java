package com.pizzaroof.sinfulrush.audio;

import com.badlogic.gdx.audio.Sound;
import com.pizzaroof.sinfulrush.Constants;

import java.util.ArrayList;

/**sequenza di suoni con ritardi di esecuzione tra un suono e l'altro*/
public class SequenceSound {

    private ArrayList<SeqSound> sounds;

    private boolean started;
    private float time;
    private int index;

    private float volume;

    public SequenceSound() {
        sounds = new ArrayList<>();
        started = false;
        time = 0;
        index = 0;
    }

    /**fa ricominciare la sequenza di suoni*/
    public void play(float volume) {
        if(volume < Constants.EPS) {
            started = false;
            return;
        }
        started = true;
        this.volume = volume;
        time = 0;
        index = 0;
    }

    public void update(float delta) {
        if(started) {
            if(index >= sounds.size()) {
                started = false;
                return;
            }

            time += delta;
            while(index < sounds.size() && time >= sounds.get(index).startTime) {
                sounds.get(index).sound.play(volume, sounds.get(index).pitch, 0);
                index++;
            }
        }
    }

    public void addSound(Sound sound, float delay, float pitch) {
        SeqSound s = new SeqSound();
        s.sound = sound;
        s.startTime = (sounds.size() > 0 ? sounds.get(sounds.size()-1).startTime : 0) + delay;
        s.pitch = pitch;
        sounds.add(s);
    }

    public void setPitch(int index, float pitch) {
        sounds.get(index).pitch = pitch;
    }

    public int getSize() {
        return sounds.size();
    }

    private class SeqSound {
        Sound sound;
        float startTime;
        float pitch;
    }

}
