package com.pizzaroof.sinfulrush.spawner.platform;

import com.badlogic.gdx.assets.AssetManager;

import com.badlogic.gdx.math.Vector2;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.util.Pair;
import com.pizzaroof.sinfulrush.util.Utils;

import java.io.IOException;
import java.util.ArrayList;

/**spawner di piattaforme*/
public abstract class PlatformSpawner {

    /**ci manteniamo in memoria l'ultima piattaforma creata*/
    protected Platform lastCreatedPlatform;

    protected boolean upDirection; //piattaforme generate verso l'alto?

    /**giocatore che salterà le piattaforme*/
    protected Player player;

    /**lista delle path delle piattaforme disponibili che possono essere generate*/
    protected ArrayList<String> padAvailable;

    //manteniamo riferimenti al mondo2d e all'asset manager perchè ci servono per creare piattaforme
    protected World2D world;
    protected AssetManager assetManager;

    /**prossimo tipo di piattaforma da generare? (ci salviamo anche le dimensioni, per non riaccedere troppo spesso ai file*/
    protected Pair<String, Vector2> nextType;

    /**@param upDirection deve generarli andando verso l'alto o verso il basso?*/
    public PlatformSpawner(boolean upDirection, World2D world, AssetManager asset) {
        this.upDirection = upDirection;
        this.world = world;
        this.assetManager = asset;
        padAvailable = new ArrayList<>();
        nextType = new Pair<>();
    }

    /**genera nuovo tipo di piattaforma, da usare nel generateNextPlatform*/
    protected abstract String generateNextPlatformType();

    /**genera nuova piattaforma*/
    protected abstract Platform generateNextPlatform();

    /**restituisce una nuova piattaforma*/
    public Platform getNextPlatform() {
        lastCreatedPlatform = generateNextPlatform();
        return lastCreatedPlatform;
    }

    /**restituisce un nuovo tipo di piattaforma, dalla lista di quelle disponibili.
     * Deve essere chiamata prima di getNextPlatform, in modo da cambiare il tipo di piattaforma,
     * altrimenti genera sempre la stessa. (restituiamo anche le dimensioni, per non dover accedere
     * ai file troppo spesso)*/
    public Pair<String, Vector2> getNextPlatformType() throws IOException {
        nextType.v1 = generateNextPlatformType();
        nextType.v2 = Utils.padDimensions(nextType.v1);
        return nextType;
    }

    /**restituisce ultima piattaforma creata*/
    public Platform getLastCreatedPlatform() {
        return lastCreatedPlatform;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    /**aggiunge @pad alla lista delle piattaforme disponibili per la creazione*/
    public void addPadAvailable(String pad) {
        padAvailable.add(pad);
    }

    public void removePadAvailable(String pad) {
        padAvailable.remove(pad);
    }

    /**usato per resettare y quando diventa troppo grande*/
    public void resetY(float maxy) {
    }
}
