package com.pizzaroof.sinfulrush.spawner.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.PlatformEnemy;
import com.pizzaroof.sinfulrush.util.MarkovChain;
import com.pizzaroof.sinfulrush.util.Pair;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.util.Utils;

/**spawner di nemici basato su catena di markov.
 * Gli stati sono i nemici, quindi in sostanza il prossimo nemico dipende solo dal nemico precedente*/
public class MarkovEnemySpawner extends EnemySpawner {

    /**catena di markov usata*/
    protected MarkovChain<String> markovChain;

    /**catena di markov per inserire nuovi nemici sulla stessa piattaforma.
     * Le due catene devono avere gli stessi stati*/
    protected MarkovChain<String> extraChain;

    /**orizzontalmente voglio crearlo a sinistra o destra?*/
    protected boolean leftSide;

    /**mi tengo un generatore random*/
    protected RandomXS128 rand;

    /**
     * @param slowStart numero minimo di piattaforme vuote da creare prima di partire
     */
    public MarkovEnemySpawner(int slowStart, SoundManager soundManager, Player player) {
        super(slowStart, soundManager, player);
        rand = new RandomXS128();
    }

    /**cambia la catena di markov usata dallo spawner*/
    public void changeMarkovChain(String newPath) {
        markovChain = MarkovChain.fromFile(newPath);
    }

    /**setta la catena di markov per i nemici extra*/
    public void changeExtraChain(String path) {
        extraChain = MarkovChain.fromFile(path);
    }

    /**generiamo il nuovo tipo semplicemente andando avanti nella catena di markov*/
    @Override
    protected String generateNextType(Pair<String, Vector2> platformType) {
        if(markovChain == null) return null;

        String ret = markovChain.moveTranslatedState();
        if(extraChain != null) //setto lo stato per quella orizzontale da questa markov chain
            extraChain.setInitialState(markovChain.getActualState());
        return ret;
    }

    @Override
    protected PlatformEnemy generateNextEnemy(Platform platform, World2D world, AssetManager assetManager, Group backgroundGroup, Group effectGroup, Group enemiesGroup, Stage stage) {
        if(nextType == null || nextType.v1 == null) //è uscito nemico nullo... diamo nemico nullo
            return null;
        Vector2 position = new Vector2(platform.getX(), platform.getY()); //in mezzo alla piattaforma
        position.x += platform.getWidth()/2;
        position.y += platform.getHeight()*0.8f + nextType.v2.y/2;
        position.x /= world.getPixelPerMeter();
        position.y /= world.getPixelPerMeter();

        //il metodo util si prende cura di generare il nemico giusto dalla directory
        return (PlatformEnemy)Utils.getEnemyFromDirectory(nextType.v1, assetManager, world, position, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player);
    }

    /**si occupa di generare nuovi nemici per la stessa piattaforma
     * @enemy è il nemico originale sulla piattaforma*/
    @Override
    protected PlatformEnemy generateEnemyForSamePlatform(PlatformEnemy enemy, int num, World2D world, AssetManager assetManager, Group backgroundGroup, Group effectGroup, Group enemiesGroup, Stage stage) {
        if(num > 3 || extraChain == null) return null; //non ne genera più di 3
        if(num == 2) leftSide = rand.nextBoolean(); //il primo lo scelgo a caso
        if(num == 3) leftSide = !leftSide; //l'altro lo metto dalla parte opposta

        int tmp = extraChain.getActualState(); //non voglio muovere lo stato iniziale (prendo i nuovi nemici sempre dallo stato del primo nemico)
        String newe = extraChain.moveTranslatedState();
        extraChain.setInitialState(tmp);
        if(newe == null) return null;

        try {
            Vector2 dim = Utils.enemyDimensions(newe);
            Vector2 position = new Vector2(0, (enemy.getMyPlatform().getY() + enemy.getMyPlatform().getHeight() * 0.8f + dim.y * 0.5f)/world.getPixelPerMeter());
            if (leftSide) //a sinistra
                position.x = enemy.getMyPlatform().getX() + dim.x / 2.f;
            else //a destra
                position.x = enemy.getMyPlatform().getX() + enemy.getMyPlatform().getWidth() - dim.x / 2.f;
            position.x /= world.getPixelPerMeter();
            return (PlatformEnemy)Utils.getEnemyFromDirectory(newe, assetManager, world, position, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
