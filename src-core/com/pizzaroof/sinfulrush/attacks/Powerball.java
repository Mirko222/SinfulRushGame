package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.brashmonkey.spriter.gdx.SpriterData;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.ParticleActor;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.FriendEnemy;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;
import com.pizzaroof.sinfulrush.actors.stage.TimescaleStage;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.util.LinkedList;

/**In sostanza una sfera (di fuoco/ghiaccio/quello che vuoi)
 * che va in una certa direzione*/
public class Powerball extends ParticleActor {

    /**potere della powerball*/
    protected int power;

    /**velocità powerball in metri al secondo*/
    protected float speed;

    /**true sse è una powerball lanciara da un nemico*/
    protected boolean evil;

    /**effetto per l'esplosione (opzionale)... presene solo se le esplosioni sono con effetti particellari*/
    protected String explosionEffect;

    protected com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor effectColor, explosionEffectColor;

    /**salviamo l'attore con cui è entrata in collisione, e processiamo dopo la collisione,
     * in modo da non interferire con il world step*/
    protected LinkedList<PhysicSpriteActor> collidedWith;

    /**attore che ignora se colpisce*/
    protected Actor ignore;

    protected Vector2 direction;

    /**fa timescale?*/
    protected boolean timescale;

    /**sfera lanciata automaticamente?*/
    protected boolean automaticBall;

    /**rimuove effetto della sfera prima dell'esplosione*/
    protected boolean removeEffectBeforeExplosion;

    /**id delle animazioni di movimento ed explode... se si vuole usare uno sprite insieme/invece degli effetti particellari*/
    protected int TRAVEL_ID, EXPLODE_ID;

    private boolean rageOn;

    protected boolean bossball;

    protected SoundManager soundManager;

    /**crea powerball
     * @param spawnPoint punto da dove inizia la traiettoria
     * @param speed velocità della powerball in m/s
     * @param effect effetto particellare della powerball (può non esserci e usiamo sprite)
     * @param power potere della powerball
     * @param direction direzione della powerball*/
    public Powerball(World2D world, String effect, Vector2 spawnPoint, float speed, int power, float radius, Vector2 direction, com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor color) {
        super(world, BodyDef.BodyType.DynamicBody, 0, 0, 0, spawnPoint, true, com.pizzaroof.sinfulrush.Constants.POWERBALL_CATEGORY_BITS,
                com.pizzaroof.sinfulrush.util.Utils.maskToNotCollideWith(com.pizzaroof.sinfulrush.Constants.POWERBALL_CATEGORY_BITS, com.pizzaroof.sinfulrush.Constants.PARTICLES_CATEGORY_BITS), true, com.pizzaroof.sinfulrush.util.Utils.getCircleShape(radius));
        body.setGravityScale(0); //non affetto da gravità
        collidedWith = new LinkedList<>();
        init(effect, spawnPoint, speed, power, direction, color);
        automaticBall = false;
    }

    /**chiamare init se usi questo!!!*/
    public Powerball(World2D world, float radius) {
        super(world, BodyDef.BodyType.DynamicBody, 0, 0, 0, Vector2.Zero, true, com.pizzaroof.sinfulrush.Constants.POWERBALL_CATEGORY_BITS,
                com.pizzaroof.sinfulrush.util.Utils.maskToNotCollideWith(com.pizzaroof.sinfulrush.Constants.POWERBALL_CATEGORY_BITS, com.pizzaroof.sinfulrush.Constants.PARTICLES_CATEGORY_BITS), true, com.pizzaroof.sinfulrush.util.Utils.getCircleShape(radius));
        body.setGravityScale(0);
        collidedWith = new LinkedList<>();
        timescale = true;
        TRAVEL_ID = EXPLODE_ID = -1;
    }

    @Override
    public void actSkipTolerant(float delta) {
        if(!doesTimescale() && isOnStage())
            super.actSkipTolerant(delta / ((TimescaleStage)getStage()).getTimeMultiplier());
        else
            super.actSkipTolerant(delta);

        dealWithTimescale();

        processCollisions();

        if(isOnStage() && effectColor != null) { //questa cosa è un po' imprecisa... per ogni effetto dovremmo veramente vedere il colore... funziona perché siamo in un caso particolare
            boolean rage = ((ShaderStage) getStage()).isRageModeOn();
            if(shouldBeTintedWhiteDuringRage(effectColor)) {
                if (!rageOn && rage)
                    colorEffects(com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor.WHITE);
                if (rageOn && !rage)
                    colorEffects(effectColor);
            }

            rageOn = rage;
        }

        if(isOnStage() && effects.size() == 0 && TRAVEL_ID == -1) //quando l'effetto è completato (quindi è svanito), rimuovi l'attore (e non ha animazioni...)
            removeAndFreeIfPossible();

        //quando esci fuori inizia a svanire (casi in cui non ha colpito nessuno)
        if (isOnStage() && !isInCameraView()) {
            if(effects.size() > 0)
                effects.get(0).allowCompletion();
            else
                removeAndFreeIfPossible();
            body.setLinearVelocity(0, 0);
            body.setActive(false);
        }
    }

    /**usato per indicare se la sfera è lanciata da un nemico o dal player*/
    public void setEvil(boolean evil) {
        this.evil = evil;
    }

    public void setIgnoreActor(Actor actor) {
        ignore = actor;
    }

    public void setRemoveEffectBeforeExplosion(boolean removeEffectBeforeExplosion) {
        this.removeEffectBeforeExplosion = removeEffectBeforeExplosion;
    }

    /**processa la collisioni... non le facciamo subito perché potrebbe dare problemi rimuovere/aggiungere nuovi corpi*/
    protected void processCollisions() {
        if(collidedWith.size() > 0) {
            for(PhysicSpriteActor actor : collidedWith) {
                if(ignore != null && actor.hashCode() == ignore.hashCode()) //attore da ignorare
                    continue;

                if (actor instanceof Player && evil) { //entrato in collisione col giocatore
                    ((Player) actor).takeDamage(power, this); //facciamo danno al player (se la sfera non è malvagia, lo si ignora semplicemente)
                    explode(actor);
                    getBody().setLinearVelocity(0, 0);
                }

                if (actor instanceof Enemy) { //il target era un nemico...
                    if(((Enemy) actor).getHp() <= 0 || (automaticBall && actor instanceof FriendEnemy)) //i friend enemy non possono essere colpiti da palle automatiche... è uno svantaggio ingiusto
                        continue;

                    if (evil) //se è malvagia, cura il nemico
                        ((Enemy) actor).heal(power);
                    else { //altrimenti gli fa danno
                        Vector2 hitPoint = actor.centerPosition();
                        Vector2 dir = new Vector2();
                        dir.setToRandomDirection();
                        float mag = actor.getWidth() * com.pizzaroof.sinfulrush.util.Utils.randFloat(0.6f, 0.7f) * Math.abs(dir.x) + actor.getHeight() * com.pizzaroof.sinfulrush.util.Utils.randFloat(0.6f, 0.7f) * Math.abs(dir.y); //float mag = Utils.randFloat(Math.max(actor.getWidth(), actor.getHeight()) * 0.5f, Math.max(actor.getWidth(), actor.getHeight()));
                        hitPoint.x += mag * dir.x;
                        hitPoint.y += mag * dir.y;
                        if(isOnStage()) {
                            if (hitPoint.x < 0) hitPoint.x = 0;
                            if (hitPoint.x > getStage().getCamera().viewportWidth)
                                hitPoint.x = getStage().getCamera().viewportWidth - 50;
                            float topY = getStage().getCamera().viewportHeight * 0.5f + getStage().getCamera().position.y;
                            float bottomY = -getStage().getCamera().viewportHeight * 0.5f + getStage().getCamera().position.y;
                            if (hitPoint.y < bottomY) hitPoint.y = bottomY;
                            if (hitPoint.y > topY) hitPoint.y = topY - 50;
                        }

                        ((Enemy) actor).takeDamage(power, hitPoint, Color.WHITE, Mission.BonusType.SCEPTRE); //sfera che deve essere stata lanciata da uno scettro
                    }
                    explode(actor);
                    getBody().setLinearVelocity(0, 0);
                }
            }
        }
        collidedWith.clear();
    }

    @Override
    public void onSpriterAnimationEnded(int id) {
        if(id == EXPLODE_ID) { //esplosa... togliamola
            removeAndFreeIfPossible();
        }
    }

    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    /**chiamata quando la sfera deve esplodere, perché entrata in collisione con @actor*/
    protected void explode(PhysicSpriteActor actor) {
        if(soundManager != null) {
            if (!evil) {
                soundManager.sceptreExplosion();
            } else if (bossball) {
                soundManager.bossBallExplosion();
            } else if(actor instanceof Player) { //sfera nemico entrata in collisione con player
                soundManager.enemyExplosion();
            }
        }

        if(effects.size() > 0) {
            effects.get(0).allowCompletion(); //permetti all'effetto di terminare
            if(removeEffectBeforeExplosion) {
                effects.get(0).free();
                effects.remove(0);
            }
        }

        if(explosionEffect != null) { //effetto particellare
            Pools.PEffectColor color = (explosionEffectColor == null) ? effectColor : explosionEffectColor;
            if(rageOn && shouldBeTintedWhiteDuringRage(color))
                color = com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor.WHITE;
            addEffect(explosionEffect, color);
        } else //niente effetto particellare
            if(EXPLODE_ID != -1)
                setSpriterAnimation(EXPLODE_ID);
        body.setActive(false);
    }

    protected boolean shouldBeTintedWhiteDuringRage(com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor color) {
        return (color.equals(com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor.DARK) || color.equals(com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor.FIRE));
    }

    public void setTimescale(boolean timescale) {
        this.timescale = timescale;
    }

    public boolean doesTimescale() {
        return timescale;
    }

    public int getPower() {
        return power;
    }

    /**chiamata quando entra in collisione con actor*/
    @Override
    public void onCollisionWith(PhysicSpriteActor actor) {
        collidedWith.add(actor);
    }

    public void setExplosionEffect(String explosionEffect) {
        this.explosionEffect = explosionEffect;
    }

    public void setAutomaticBall(boolean b) {
        automaticBall = b;
    }

    public void setExplosionEffectColor(com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor color) {
        explosionEffectColor = color;
    }

    public boolean isEvil() {
        return evil;
    }

    /**chiama questo metodo per usare una spriter animation invece
     * di usare solo gli effetti particellari.
     * NB: potrebbero esserci problemi se si vuole cambiare lo spriterData di particelle nella stessa pool*/
    public void useAnimations(SpriterData data, Batch batch, int travelId, float travelTime, int explodeId, float explodeTime, int originalWidth, float drawW, float drawH) {
        if(spriterPlayer == null) { //assumiamo che vengano estratte dalla stessa pool
            setSpriterData(data, batch);
            addSpriterAnimation(travelId, travelTime, Animation.PlayMode.LOOP);
            addSpriterAnimation(explodeId, explodeTime, Animation.PlayMode.NORMAL);
        }
        setOriginalWidth(originalWidth);
        setDrawingWidth(drawW);
        setDrawingHeight(drawH);

        TRAVEL_ID = travelId;
        EXPLODE_ID = explodeId;
        recomputePosition();
        recomputeSpriterScale();
        setSpriterAnimation(TRAVEL_ID);

        if(direction != null)
            setSpriterRotation(direction.angle());

        spriterPlayer.speed = 0;
        spriterPlayer.update();
    }

    /**si occupa del timescale*/
    protected void dealWithTimescale() {
        if(doesTimescale()) return; //fa timescale... non dobbiamo fare niente, è già gestito di default

        if(isOnStage() && getBodySpeed() > Constants.EPS) {
            float timeMul = ((TimescaleStage) getStage()).getTimeMultiplier();
            float add = 0; //Math.abs(timeMul - 1) > Constants.EPS ? 0.11f : 0;
            if (direction != null) //aumenta la velocità del body per coprire lo stesso spazio, quando il tempo diminuisce
                getBody().setLinearVelocity(direction.x * speed / (timeMul + add), direction.y * speed / (timeMul + add));
        }
    }

    public void init(String effect, Vector2 spawnPoint, float speed, int power, Vector2 direction, com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor color) {
        init(effect, spawnPoint, speed, power, direction, true, color);
    }

    private void colorEffects(com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor color) {
        for (ParticleEffectPool.PooledEffect e : effects)
            if (e.getEmitters().size >= 2)
                Utils.colorEffect(e, color);
    }

    public void setBossball(boolean b) {
        bossball = b;
    }

    /**da chiamare dopo averlo estratto dalla pool per rinizializzare valori*/
    public void init(String effect, Vector2 spawnPoint, float speed, int power, Vector2 direction, boolean useParticles, Pools.PEffectColor color) {
        this.speed = speed;
        bossball = false;
        rageOn = false;
        clearEffects();
        explosionEffectColor = null;
        if(useParticles) {
            effectColor = color;
            addEffect(effect, color); //aggiungi l'effetto
            setContinuousState(0, true); //metti l'effetto continuo
        }
        removeEffectBeforeExplosion = false;
        this.power = power;
        evil = true; //di base assumiamo sia lanciata da un nemico
        collidedWith.clear();
        explosionEffect = null;
        instantSetPosition(spawnPoint);
        body.setActive(true);
        ignore = null;
        if(direction != null)
            body.setLinearVelocity(speed * direction.x, speed * direction.y);
        this.direction = direction;
        timescale = true;
        TRAVEL_ID = EXPLODE_ID = -1;
        recomputePosition();
    }
}
