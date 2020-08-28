package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.brashmonkey.spriter.gdx.SpriterData;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Boss;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.FriendEnemy;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;
import com.pizzaroof.sinfulrush.util.pools.PowerballPool;

/**powerball che quando colpisce si divide in altre powerball più piccole*/
public class SplittingPowerball extends IncreasingPowerball {

    /**effetto per le palle che si generano dopo l'hit*/
    protected String splittedEffect;

    /**eventuale esplosione delle palline*/
    protected String splittedExplosion;

    /**range in metri, di dove si arriva a splittare*/
    protected float splittedRange;

    /**gruppo degli effetti: deve generare nuove sfere*/
    protected Group effectGroup;

    /**gruppo dei nemici*/
    protected Group enemiesGroup;

    /**già esplosa?*/
    protected boolean exploded;

    /**pool da dove prendere le following ball*/
    protected com.pizzaroof.sinfulrush.util.pools.PowerballPool followingPool;

    /**pool per sfere create a caso*/
    protected com.pizzaroof.sinfulrush.util.pools.PowerballPool randomPool;

    /**dimensioni smallball se non si usano le particelle*/
    protected int splitOriginalWidth, splitDrawWidth, splitDrawHeight;
    /**dati per animazioni smallball se non si usano particelle*/
    protected float splitTravelTime, splitExplodeTime;
    protected int splitTravelId, splitExplodeId;

    protected AssetManager assetManager;

    /**chiamare init se usi questo!!!*/
    public SplittingPowerball(World2D world, float radius, com.pizzaroof.sinfulrush.util.pools.PowerballPool followingPool, PowerballPool randomPool, AssetManager assetManager) {
        super(world, radius);
        this.followingPool = followingPool;
        this.randomPool = randomPool;
        this.assetManager = assetManager;
    }

    public void setSplittedExplosion(String splittedExplosion) {
        this.splittedExplosion = splittedExplosion;
    }

    @Override
    protected void explode(PhysicSpriteActor actor) {
        super.explode(actor);
        if(!evil && !exploded && splittedRange > 0) { //funziona solo quando non è malvagia: crea altre sfere che attaccano nemici
            exploded = true;

            if(!(actor instanceof Boss)) { //col boss non spawniamo palline
                if (Utils.randFloat() <= 0.12f) //12% probabilità di fare un random split
                    randomSplit(10, actor);
                else { //altrimenti attacchiamo quelli vicini
                    Vector2 pos = getBody().getPosition();

                    int numRem = 2; //almeno 2 palline spawniamole
                    for (Actor a : enemiesGroup.getChildren()) //trova i nemici abbastanza vicini da poter essere colpiti con una nuova sfera
                        if (a instanceof Enemy && ((Enemy) a).getHp() > 0 && a.hashCode() != actor.hashCode() &&
                                ((Enemy) a).getBody().getPosition().dst2(pos) <= splittedRange * splittedRange &&
                                !(a instanceof FriendEnemy)) { //non creiamo split su amici
                            splitOnEnemy((Enemy) a);
                            numRem--;
                        }
                    if (numRem > 0) //qualche pallina rimasta da spawnare
                        randomSplit(numRem, actor);
                }
            }
        }
    }

    /**splitta a caso (crea una nuova powerball a caso)*/
    protected void randomSplit(int num, Actor ignore) {
        for(int i=0; i<num; i++) {
            Vector2 dir = new Vector2();
            dir.setToRandomDirection();
            Powerball ball = (Powerball)randomPool.obtain();
            ball.init(splittedEffect, getBody().getPosition(), speed * 0.75f, (int)(power * 0.5f), dir, splitTravelId == -1, effectColor);

            initBallAnimations(ball);
            ball.setEvil(evil);
            ball.setIgnoreActor(ignore);
            ball.setTimescale(doesTimescale());
            ball.setAutomaticBall(true);
            //if(!evil)
            //    ball.setSoundManager(soundManager);
            effectGroup.addActor(ball);
        }
    }

    /**crea una nuova sfera per colpire il nemico*/
    protected void splitOnEnemy(Enemy enemy) {
        FollowingPowerball powerball = (FollowingPowerball)followingPool.obtain();

        powerball.setBackupDirection(new Vector2().setToRandomDirection());
        powerball.init(splittedEffect, getBody().getPosition(), speed * 0.75f, (int)(power * 0.5f), enemy, splitTravelId == -1, effectColor);

        powerball.setEvil(evil);
        initBallAnimations(powerball);
        powerball.setTimescale(doesTimescale());
        powerball.recomputePosition();
        //if(!evil)
        //    powerball.setSoundManager(soundManager);
        effectGroup.addActor(powerball);
    }

    protected void initBallAnimations(Powerball ball) {
        if(splittedExplosion != null) //usa particles
            ball.setExplosionEffect(splittedExplosion);
        else
            if(splitTravelId != -1) //usa animation
                if(getStage() == null) return;

                ball.useAnimations(
                        assetManager.get(Utils.sheetEffectScmlPath(splittedEffect), SpriterData.class),
                        getStage().getBatch(),
                        splitTravelId,
                        splitTravelTime,
                        splitExplodeId,
                        splitExplodeTime,
                        splitOriginalWidth,
                        splitDrawWidth,
                        splitDrawHeight
                );
    }

    public void useSmallBallAnimations(int originalWidth, int drawW, int drawH, int travelId, float travelTime, int explodeId, float explodeTime) {
        splitOriginalWidth = originalWidth;
        splitDrawWidth = drawW;
        splitDrawHeight = drawH;
        splitTravelId = travelId;
        splitTravelTime = travelTime;
        splitExplodeId = explodeId;
        splitExplodeTime = explodeTime;
    }

    /**@param splittedRange quando si splitta, qual è il raggio in cui cercare nemici da attaccare? (in metri) (metterlo negativo se non si vuole splitting)
     * @param power potere minimo
     * @param powerMax potere massimo*/
    public void init(String effect, Vector2 spawnPoint, float speed, int power, Vector2 direction, int powerMax, String splittedEffect, float splittedRange, Group effectGroup, Group enemiesGroup, boolean useParticles, Pools.PEffectColor color) {
        super.init(effect, spawnPoint, speed, power, direction, powerMax, useParticles, color);
        exploded = false;
        this.splittedEffect = splittedEffect;
        this.splittedRange = splittedRange;
        this.effectGroup = effectGroup;
        this.enemiesGroup = enemiesGroup;
        splitTravelId = splitExplodeId = -1;
    }
}
