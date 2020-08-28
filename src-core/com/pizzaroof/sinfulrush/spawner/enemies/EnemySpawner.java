package com.pizzaroof.sinfulrush.spawner.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.FriendEnemy;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.PlatformEnemy;
import com.pizzaroof.sinfulrush.util.Pair;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.util.Utils;

import java.io.IOException;
import java.util.ArrayList;

/**spawner di nemici*/
public abstract class EnemySpawner {

    /**lista dei nemici possibili*/
    protected ArrayList<String> possibleEnemies;

    /**ultimo tipo di nemico generato (salviamo directory e dimensioni)*/
    protected Pair<String, Vector2> lastType;

    /**prossimo tipo di nemico da generare? (salviamo directory e dimensioni, in pixels)*/
    protected Pair<String, Vector2> nextType;

    /**quante piattaforme vuote dobbiamo creare prima di partire?*/
    protected int slowStart;

    /**quanti nemici al massimo su una stessa piattaforma?
     * In quest'implementazione massimo 3 nemici*/
    protected int maxEnemiesPerPlatform;

    protected RandomXS128 rand;

    protected SoundManager soundManager;

    protected Player player;

    /**abilita 3 chibi su piattaforme "più piccole", di default, anche se si mettono 3 nemici su piattaforma,
     * questo avviene solo sulla piattaforma più grande*/
    private boolean threeChibisOnMorePlatformsEnabled;

    /**permette ai nemici "grandi" di avere affianco 2 nemici piccoli sulle piattaforme grandi e 1 nemico piccolo sulle piattaforme piccole
     * (di default, possono avere solo 1 nemico piccolo sulle grandi e 0 nemici piccoli sulle piccole)*/
    private boolean expandBigGuysEnabled;

    /**score button... necessario ai friend che attaccano lo score*/
    private Button scoreButton;
    /**gruppo hud, usato sempre da friend che attaccano score*/
    private Group hudGroup;

    /**
     * @param slowStart numero minimo di piattaforme vuote da creare prima di partire*/
    public EnemySpawner(int slowStart, SoundManager soundManager, Player player) {
        possibleEnemies = new ArrayList<>();
        this.soundManager = soundManager;
        this.player = player;
        lastType = new Pair<>(null, null); //nemico nullo: cioè nessun nemico
        nextType = new Pair<>(null, null);
        this.slowStart = Math.max(0, slowStart);
        rand = new RandomXS128();
        setMaxEnemiesPerPlatform(1);
        threeChibisOnMorePlatformsEnabled = false;
        expandBigGuysEnabled = false;
    }

    /**path del prossimo nemico che verrà generata?
     * @param platformType tipo della piattaforma su cui il nemico sarà (abbiamo sia il file che le dimensioni; la piattaforma vera e proprio deve ancora
     * essere creata)*/
    public Pair<String, Vector2> getNextEnemyType(Pair<String, Vector2> platformType) {
        lastType = nextType;
        if(slowStart == 0) //iniziamo sul serio
            nextType.v1 = generateNextType(platformType);
        else { //lascia vuote per un po'
            nextType.v1 = null; //genera null ancora
            slowStart--;
        }

        try { //nexttype può essere null se non ci sono mostri
            nextType.v2 = nextType.v1 == null ? null : Utils.enemyDimensions(nextType.v1);
        }catch(IOException e) {
            e.printStackTrace();
        }
        return nextType;
    }

    /**implementa effettivamente la scelta del prossimo nemico da generare*/
    protected abstract String generateNextType(Pair<String, Vector2> platformType);

    /**genera nemici sulla piattaforma @platform
     * @param backgroundGroup gruppo degli oggetti in secondo piano
     * @param effectGroup gruppo dove mettere gli effetti (in sostanza sopra i nemici)*/
    public void createEnemies(Platform platform, Player player, World2D world, AssetManager assetManager, Group backgroundGroup, Group effectGroup, Group enemiesGroup, Stage stage) {
        PlatformEnemy enemy = generateNextEnemy(platform, world, assetManager, backgroundGroup, effectGroup, enemiesGroup, stage);

        if(enemy != null) { //l'enemy può essere nullo (nessun enemy in sostanza)
            enemy.setPlayer(player);
            enemy.setMyPlatform(platform);
            platform.addEnemy(enemy);
            setExtraPropertiesIfNeeded(enemy);

            int mx = Math.min(maxEnemiesPerPlatform, maxEnemiesCreatable(enemy, platform, player)); //quanti ne posso creare al massimo fisicamente?

            for(int c=1; c<Math.min(mx, maxEnemiesPerPlatform); c++) { //genero tutti quelli extra (considerando il massimo, ma anche il massimo fisico)
                PlatformEnemy e = generateEnemyForSamePlatform(enemy, c+1, world, assetManager, backgroundGroup, effectGroup, enemiesGroup, stage);
                if(e != null) {
                    e.setPlayer(player);
                    e.setMyPlatform(platform);
                    platform.addEnemy(e);
                    setExtraPropertiesIfNeeded(e);
                }
            }
        }
    }

    /**quanti nemici si possono creare al massimo al fianco di @enemy? (utile modificarlo per ogni scenario)*/
    protected int maxEnemiesCreatable(PlatformEnemy enemy, Platform platform, Player player) {
        //qui facciamo una stima... ma probabilmente servirà un'implementazione adhod per ogni scenario
        if(expandBigGuysEnabled && platform.getWidth() > 400 && enemy.getWidth() > 100) return 3;
        if(expandBigGuysEnabled && platform.getWidth() > 300 && enemy.getWidth() > 100) return 2;

        if(threeChibisOnMorePlatformsEnabled && platform.getWidth() > 300 && enemy.getWidth() < 100) return 3;

        float spaceRem = Math.max(0, platform.getWidth() - player.getWidth() - enemy.getWidth());
        float otherWidth = 100.f; //assumiamo che gli altri nemici siano larghi 100 //enemy.getWidth() //assumo che tutti gli altri enemy siano larghi quanto l'enemy attuale
        return 1 + (int)(spaceRem / otherWidth);
    }

    public void setScoreButton(Button button) {
        this.scoreButton = button;
    }

    public void setHudGroup(Group group) {
        hudGroup = group;
    }

    private void setExtraPropertiesIfNeeded(PlatformEnemy e) {
        if(e instanceof FriendEnemy) {
            ((FriendEnemy) e).setScoreButton(scoreButton);
            ((FriendEnemy)e).setHudGroup(hudGroup);
        }
    }

    /**genera prossimo nemico
     * @param platform piattaforme dove vivrà il nemico
     * @param backgroundGroup gruppo degli oggetti in secondo piano
     * @param effectGroup gruppo dove mettere gli effetti (in sostanza sopra i nemici)*/
    protected abstract PlatformEnemy generateNextEnemy(Platform platform, World2D world, AssetManager assetManager, Group backgroundGroup, Group effectGroup, Group enemiesGroup, Stage stage);

    /**genera un altro nemico per la stessa piattaforma di @enemy.
     * @param num contatore del nemico generato (da 2° in poi)*/
    protected abstract PlatformEnemy generateEnemyForSamePlatform(PlatformEnemy enemy, int num, World2D world, AssetManager assetManager, Group backgroundGroup, Group effectGroup, Group enemiesGroup, Stage stage);

    /**aggiungi un nemico ai nemici disponibili per la generazione
     * (la directory dell'enemy)*/
    public void addPossibleEnemy(String enemy) {
        possibleEnemies.add(enemy);
    }

    /**setta massimo numero di nemici sulla piattaforma*/
    public void setMaxEnemiesPerPlatform(int mepp) {
        maxEnemiesPerPlatform = mepp;
    }

    public void setThreeChibisOnMorePlatformsEnabled(boolean enabled) {
        threeChibisOnMorePlatformsEnabled = enabled;
    }

    public void setExpandBigGuysEnabled(boolean enabled) {
        expandBigGuysEnabled = enabled;
    }
}
