package com.pizzaroof.sinfulrush.util.pools;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.pizzaroof.sinfulrush.actors.DamageFlashText;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.bonus.Bonus;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.bonus.BonusPool;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticle;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticlePool;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.util.Pair;
import com.pizzaroof.sinfulrush.util.Utils;

import java.util.HashMap;

/**classe di utlity con tutte le pool*/
public class Pools {
    /**minimo e massimo numero di effetti nella pool*/
    private static final int INIT_EFFECTS = 1, MAX_EFFECTS = 25;

    /**map delle pool per gli effetti. chiave=path dell'effeto, valore: pool per gli effetti*/
    private static HashMap<String, ParticleEffectPool> effectPools = new HashMap<>();

    /**hashmap delle pool per particelle fisiche (indicizzate da un ID univoco)*/
    private static HashMap<Integer, PhysicParticlePool> physicParticlePools = new HashMap<>();

    /**hashmap delle pool per oggetti che rappresentano bonus*/
    private static HashMap<String, BonusPool> bonusPools = new HashMap<>();

    /**pool per piattaforme*/
    private static HashMap<com.pizzaroof.sinfulrush.util.Pair<String, Boolean>, PlatformPool> platformPools = new HashMap<>();

    /**pool per flash text*/
    private static DamageFlashTextPool flashTextPool = new DamageFlashTextPool();

    /**id della testa del golem di lava per indicizzare le pool delle particelle fisiche*/
    public static final int PPPGolemHead = 1, PPPGolem3Head = 5,
                            PPPZombie1Head = 2, PPPZombie2Head = 3, PPPZombie3Head = 4;

    /**aggiungi un tipo di effetto alla pool*/
    public static void addEffectPool(String id, ParticleEffect effect) {
        if(effectPools.containsKey(id)) return;
        effectPools.put(id, new ParticleEffectPool(effect, INIT_EFFECTS, MAX_EFFECTS));
    }

    /**ottiene un nuovo particle effect del tipo specificato*/
    public static ParticleEffectPool.PooledEffect obtainEffect(String id) {
        ParticleEffectPool.PooledEffect e = effectPools.get(id).obtain();
        e.reset();
        return e;
    }

    /**possibili colori per gli effetti particellari*/
    public enum PEffectColor {
        NONE,
        FIRE,
        BLUE,
        DARK,
        GREEN,
        VIOLET,
        SKY,
        WHITE,
        MALUS_VIOLET,
        TREASURE
    }

    /**ottiene un effetto particellare colorato in una maniera standard*/
    public static ParticleEffectPool.PooledEffect obtainColoredEffect(String id, PEffectColor color) {
        ParticleEffectPool.PooledEffect e = obtainEffect(id);
        Utils.colorEffect(e, color);
        return e;
    }

    /**chiamala per liberare un effetto, in modo che sia di nuovo disponibile*/
    public static void freeEffect(String path, ParticleEffectPool.PooledEffect effect) {
        if(effectPools.containsKey(path))
            effectPools.get(path).free(effect);
    }

    /**chiamala per ottenere un nuova physical particle effect*/
    public static PhysicParticle obtainPParticle(int id, World2D world, int nextRadius, float nextRestitution) {
        if(!physicParticlePools.containsKey(id)) physicParticlePools.put(id, new PhysicParticlePool(world, true, true));
        physicParticlePools.get(id).setNextRadius(nextRadius);
        physicParticlePools.get(id).setNextRestitution(nextRestitution);
        return physicParticlePools.get(id).obtain();
    }

    /**chiamala per ottenere un nuovo bonus di id @id*/
    public static Bonus obtainBonus(String directory, AssetManager assetManager, Stage stage, SoundManager soundManager) {
        if(!bonusPools.containsKey(directory)) bonusPools.put(directory, new BonusPool(directory, assetManager, stage, soundManager));
        //System.out.println("dim pool "+directory+": "+bonusPools.get(directory).getFree());
        return (Bonus)bonusPools.get(directory).obtain();
    }

    /**restituisce una piattaforma*/
    public static Platform obtainPlatform(String padName, boolean flipped, World2D world, AssetManager assetManager, Vector2 position, String padCover, Group fronLayer) {
        com.pizzaroof.sinfulrush.util.Pair<String, Boolean> p = new Pair<>(padName, flipped);
        if(!platformPools.containsKey(p)) {
            PlatformPool pp = new PlatformPool(world, assetManager, padName, flipped);
            pp.setCover(fronLayer, padCover);
            platformPools.put(p, pp);
        }
        //System.out.println(platformPools.get(p).getFree());
        Platform plt = (Platform)platformPools.get(p).obtain();
        plt.init(position);
        return plt;
    }

    /**NB: bisogna passare sempre la stessa skin*/
    public static DamageFlashText obtainFlashText(Skin skin) {
        flashTextPool.setSkin(skin);
        return (DamageFlashText)flashTextPool.obtain();
    }

    public static void cleanAllPools() {
        cleanPools();
        effectPools.clear();
        flashTextPool.clear();
    }

    /**pulisce pool che dipendono da oggetti che vengono distrutti in game diversi
     * eg: world, stage*/
    public static void cleanPools() {
        physicParticlePools.clear();
        bonusPools.clear();
        platformPools.clear();
    }
}
