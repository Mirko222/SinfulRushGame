package com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.attacks.Armory;
import com.pizzaroof.sinfulrush.attacks.ButtonPowerball;
import com.pizzaroof.sinfulrush.attacks.FollowingPowerball;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.screens.BossGameScreen;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.ScoreButton;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.util.PerlinNoise;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.io.BufferedReader;
import java.io.IOException;

/**boss
 * animazioni: fly, attack, hurt, die
 *
 * sulla 5° riga di info.txt:
 * danni_attacco sfera_attacco spawn_powerball_x spawn_powerball_y powerball_speed powerball_radius
 * */
public class Boss extends Enemy {
    protected int FLY_ID, ATTACK_ID, HURT_ID, DIE_ID;

    protected BossGameScreen myScreen;

    /**danno dell'attacco*/
    protected int damage;

    /**powerball lanciata*/
    protected String powerballPath, explosionPath;
    protected com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor powerballColor;

    //emettiamo sangue a 75%, 50% e 25% di vita (oltre che alla morte)
    protected static final float BLOOD_EMISSIONS[] = {0.75f, 0.5f, 0.25f};

    protected static final float STANDARD_SPEED_MUL = 1.5f;
    protected static final float ESCAPE_SPEED_MUL = 2.3f; //speed multiplier per quando si scappa (vogliamo che sia rapido)
    protected static final float SAME_MOV_SPEED_MUL = 1.2f; //speed multiplier per quando si va nello stesso verso del giocatore (e quindi ci vuole di più a distanziarlo) [NB: è un multiplier aggiuntivo]

    protected static final float MAX_DISTANCE_RANGE = 5; //massima distanza tra player e boss
    protected static final float MIN_DISTANCE_RANGE = 1.8f; //minima distanza tra player e boss
    protected static final float SWITCH_TIME = 0.9f; //dopo quanto tempo si cambia la distanza tra player e boss?

    /**massimo danno che può prendere*/
    protected static final int MAX_DAMAGE_POSSIBLE = 160;

    /**coordinate di spawn per la powerball*/
    protected float spawnPowX, spawnPowY;

    /**quanti secondi dura la lotta?*/
    protected static final float FIGHT_DUR = 25.f; //12sec

    /**secondi di pausa tra un attacco e l'altro? (si inizia con pausa)*/
    protected static final float ATTACK_TIME = 3.85f; //impostato in modo da fare 6 attacchi
    /**di quanto può variare il tempo di ricarica?*/
    protected static final float DELTA_ATTACK_TIME = 0.2f; //impostato in modo da fare 6 attacchi

    /**tempo aspettato per attaccare*/
    protected float waitedToAttack;

    /**delta per attacco?*/
    protected float actualDeltaAttack;

    /**ha attaccato?*/
    protected boolean attacked;

    /**tempo passato da quando è spawnato*/
    protected float timePassed;

    /**noise usato per calcolare distanceFromPlayer*/
    protected com.pizzaroof.sinfulrush.util.PerlinNoise noise;

    /**cambia periodicamente la distanza dal player, seguendo il noise*/
    protected float timePassedWithSameDistance;

    /**quanto deve essere distante dal giocatore? (rispetto a y)*/
    protected float distanceFromPlayer;

    /**destinazione da raggiungere su x*/
    protected float destinationX;

    protected float powerballSpeed, powerballRadius;

    protected boolean disappeared; //abbiamo già comunicato la sparizione?

    protected Vector2 offsetDamage = new Vector2();

    protected com.pizzaroof.sinfulrush.actors.ScoreButton scoreButton;
    protected Group hudGroup;

    /**quale tipo di esplosione usare?*/
    private int explosionType;

    private Armory armory;

    public Boss(World2D world, SoundManager soundManager, Vector2 initPosition, String dir, AssetManager asset, Stage stage, Group effectGroup, Group backgroundGroup, BossGameScreen screen, Armory armory, Shape... shapes) {
        super(world, soundManager, BodyDef.BodyType.KinematicBody, 0, 0, 0, initPosition, dir, asset, stage, effectGroup, backgroundGroup, shapes);
        myScreen = screen;
        this.armory = armory;
        noise = new PerlinNoise();
        startFight();
        explosionType = 0;
    }

    public void setExplosionType(int type) {
        explosionType = type;
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);

        if(damageFlashText != null) { //il boss si muove, quindi vogliamo far muovere anche il danno
            damageFlashText.setX(getX() + offsetDamage.x);
            damageFlashText.setY(getY() + offsetDamage.y);
        }

        if(timePassed < FIGHT_DUR && timePassed + delta >= FIGHT_DUR && !disappeared) { //appena supera il tempo, lo consideriamo in disappearing
            myScreen.onBossDisappearing();
            disappeared = true;
        }

        timePassed += delta;

        computeYVelocity(delta);
        computeXVelocity(delta);
        handleAttacks(delta);

        if(timePassed > FIGHT_DUR && isOnStage() && !isInCameraView()) { //è scappato
            if(getHp() > 0) //è scappato e ancora vivo
                myScreen.onBossDisappeared();
            getParent().removeActor(this);
            healthBar.setVisible(false);
        }
    }

    @Override
    public void onDamageCreated(Vector2 hitpoint) {
        offsetDamage.set(hitpoint.x - getX(), hitpoint.y - getY());
    }

    protected void computeYVelocity(float delta) {
        computeDistanceFromPlayer(delta);
        float destY = player.getBody().getPosition().y;
        if(myScreen.isGoingUp())
            destY += distanceFromPlayer;
        else
            destY -= distanceFromPlayer;

        float mul = getBody().getPosition().y < destY ? 1 : -1;
        float speedNeeded = Math.abs(destY - getBody().getPosition().y); //velocità per raggiungere destinazione in un secondo

        float speedMul = timePassed < FIGHT_DUR ? STANDARD_SPEED_MUL : ESCAPE_SPEED_MUL;
        if((myScreen.isGoingUp() && mul > 0) || (!myScreen.isGoingUp() && mul < 0)) speedMul *= SAME_MOV_SPEED_MUL; //stessa direzione del player... aumentiamo velocità
        float maxSpeed = player.getSpeed() * speedMul;

        getBody().setLinearVelocity(getBody().getLinearVelocity().x, mul * Math.min(speedNeeded, maxSpeed));
    }

    /**calcola quanto deve essere distante dal player*/
    protected void computeDistanceFromPlayer(float delta) {
        if(timePassed < FIGHT_DUR) { //siamo ancora in battaglia
            float mul = myScreen.isGoingUp() ? 1 : 1.2f;
            if (timePassedWithSameDistance > SWITCH_TIME) {
                distanceFromPlayer = noise.noise() * (MAX_DISTANCE_RANGE - MIN_DISTANCE_RANGE) * mul + MIN_DISTANCE_RANGE * mul;
                timePassedWithSameDistance = 0;
            } else
                timePassedWithSameDistance += delta;
        } else {
            if(getHp() > 0) {
                distanceFromPlayer = (getStage().getCamera().viewportHeight * 2f) / world.getPixelPerMeter();
            }
            else
                distanceFromPlayer = (MIN_DISTANCE_RANGE + MAX_DISTANCE_RANGE) * 0.5f;
        }
    }

    protected void computeXVelocity(float delta) {
        if(getHp() > 0 && timePassed < FIGHT_DUR) {
            float xspeed = player.getSpeed() * STANDARD_SPEED_MUL;
            float playerx = player.getBody().getPosition().x, myx = getBody().getPosition().x;
            float viewW = getStage().getCamera().viewportWidth;

            if(Math.abs(playerx - myx) <= getWidth() * 0.33f / world.getPixelPerMeter() &&
                Math.abs(myx - destinationX) <= com.pizzaroof.sinfulrush.Constants.EPS * 1000) //il giocatore ci sta sopra e abbiamo pure raggiunto la destinazione vecchia
                if(playerx * world.getPixelPerMeter() < viewW * 0.5f) { //giocatore a sinistra... andiamo a destra
                    float r = viewW / world.getPixelPerMeter() - getWidth() * 0.8f / world.getPixelPerMeter();
                    float l = Math.min(r, playerx + (getWidth() / world.getPixelPerMeter()));
                    destinationX = com.pizzaroof.sinfulrush.util.Utils.randFloat(l, r);
                }
                else { //giocatore a destra, andiamo a sinistra
                    float l = getWidth() * 0.8f / world.getPixelPerMeter();
                    float r = Math.max(playerx - getWidth() / world.getPixelPerMeter(), l);
                    destinationX = com.pizzaroof.sinfulrush.util.Utils.randFloat(l, r);
                }

            float mul = myx < destinationX ? 1 : -1;
            float speedNeeded = Math.abs(destinationX - myx);

            getBody().setLinearVelocity(Math.min(speedNeeded, xspeed) * mul, getBody().getLinearVelocity().y);

            if(getCurrentSpriterAnimation() != ATTACK_ID) {
                if (playerx < myx - getWidth() * 0.5f / world.getPixelPerMeter()) //il player deve stare "abbastanza di lato" per far cambiare la direzione (evita scatti in rapida sequenza)
                    setHorDirection(SpriteActor.HorDirection.LEFT);
                if (playerx > myx + getWidth() * 0.5f / world.getPixelPerMeter())
                    setHorDirection(SpriteActor.HorDirection.RIGHT);
            }
        }
        else
            getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);
    }

    protected void handleAttacks(float delta) {
        if(getCurrentSpriterAnimation() != ATTACK_ID && player.getHp() > 0 && timePassed < FIGHT_DUR && getHp() > 0) {
            waitedToAttack += delta;
            if(waitedToAttack > ATTACK_TIME + actualDeltaAttack) {
                waitedToAttack = 0;
                attacked = false;
                actualDeltaAttack = com.pizzaroof.sinfulrush.util.Utils.randFloat(-1, 1) * DELTA_ATTACK_TIME;
                setHorDirection(player.getBody().getPosition().x < getBody().getPosition().x ? SpriteActor.HorDirection.LEFT : SpriteActor.HorDirection.RIGHT);
                setSpriterAnimation(ATTACK_ID);
            }
        }
    }

    /**da chiamare quando il boss spawna, per iniziare la boss fight*/
    public void startFight() {
        soundManager.bossRoar();
        setSpriterAnimation(FLY_ID);
        timePassed = 0;
        distanceFromPlayer = (MAX_DISTANCE_RANGE + MIN_DISTANCE_RANGE) * 0.5f;
        timePassedWithSameDistance = 0;
        healthBar.setVisible(true);
        disappeared = false;
        waitedToAttack = 0;
        attacked = false;
        actualDeltaAttack = com.pizzaroof.sinfulrush.util.Utils.randFloat(-1, 1) * DELTA_ATTACK_TIME;
    }

    @Override
    public void instantSetPosition(Vector2 pos) {
        super.instantSetPosition(pos);
        destinationX = pos.x;
    }

    @Override
    public void takeDamage(int dmg, Mission.BonusType damageType) {
        if(player.getHp() <= 0) return; //non possiamo subire danni mentre il giocatore è morto

        dmg = Math.min(dmg, (int)Math.ceil(MAX_DAMAGE_POSSIBLE * player.getPlayerPower().getAttackMultiplier(armory)));
        for(int i=0; i<BLOOD_EMISSIONS.length; i++)
            if(getHp() > getMaxHp() * BLOOD_EMISSIONS[i] && getHp() - dmg <= getMaxHp() * BLOOD_EMISSIONS[i]) {
                emitBlood();
                if(getHp() > 0)
                    soundManager.bossHurt();
                cameraController.setIncresingTrauma(com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController.MAX_TRAUMA);
            }

        super.takeDamage(dmg, damageType);

        if(getHp() <= 0)
            cameraController.setIncresingTrauma(CameraController.MAX_TRAUMA);
    }

    @Override
    public void takeDamage(int dmg, Vector2 hitPoint, Color color, Mission.BonusType damageType) {
        super.takeDamage(Math.min(dmg, (int)Math.ceil(MAX_DAMAGE_POSSIBLE * player.getPlayerPower().getAttackMultiplier(armory))), hitPoint, color, damageType);
    }

    @Override
    protected void hurt() {
        super.hurt();
        if(getCurrentSpriterAnimation() != ATTACK_ID)
            setSpriterAnimation(HURT_ID);
    }

    @Override
    protected void dying(Mission.BonusType deathType) {
        super.dying(deathType);
        setSpriterAnimation(DIE_ID);
        soundManager.bossDeath();
        playExplosionSound();
        //emitBlood();
        giveTreasure();
        myScreen.onBossDeath(); //chiamiamo callback
        disappeared = true; //lo consideriamo sparito, se è morto non serve usare le altre callback
    }

    protected void playExplosionSound() {
        if(explosionType == 0)
            soundManager.bossExplosion();
        else
            soundManager.bossExplosion2();
    }

    /**dà una ricompensa all'utente per quando muore*/
    protected void giveTreasure() {
        if(scoreButton != null) {
            ButtonPowerball ball = new ButtonPowerball(world, com.pizzaroof.sinfulrush.Constants.TREASURE_BALL, getBody().getPosition(), 4.5f, 50, 0.05f, Vector2.Zero, com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor.TREASURE) {
                @Override
                public void onCollisionWithButton() {
                    super.onCollisionWithButton();
                    player.increasePlatformMalus(-this.getPower());
                }
            };
            ball.setTargetBtn(scoreButton);
            ball.setExplosionEffect(Constants.TREASURE_EXPLOSION);
            ball.setEvil(false);
            hudGroup.addActorAfter(scoreButton, ball);
        }
    }

    @Override
    public void onSpriterAnimationEnded(int id) {
        if(id == HURT_ID || id == ATTACK_ID) {
            setSpriterAnimation(FLY_ID);
        }

        if(id == DIE_ID) {
            remove();
        }
    }

    @Override
    public void onSpriterAnimationExecuting(int id, int act, int tot) {
        if(id == ATTACK_ID && 2 * act > tot && !attacked) {
            attacked = true;
            float sx = !getHorDirection().equals(originalDirection) ? getDrawingWidth() - spawnPowX : spawnPowX;
            Vector2 spawn = new Vector2((getX() + sx) / world.getPixelPerMeter(), (getY() + spawnPowY) / world.getPixelPerMeter());
            Vector2 backup = new Vector2(getHorDirection().equals(SpriteActor.HorDirection.LEFT) ? -1 : 1, 0);
            FollowingPowerball powerball = new FollowingPowerball(world, powerballPath, spawn, powerballSpeed, damage, powerballRadius, player, backup, powerballColor);
            powerball.setExplosionEffect(explosionPath);
            powerball.setBossball(true);
            powerball.setSoundManager(soundManager);
            effectGroup.addActor(powerball);
            soundManager.bossAttack();
        }
    }

    @Override
    protected void onSpriterAnimationAdded(int id, int num) {
        switch(num) {
            case 0:
                FLY_ID = id;
                setSpriterAnimationMode(id, Animation.PlayMode.LOOP);
            break;
            case 1: ATTACK_ID = id; break;
            case 2: HURT_ID = id; break;
            case 3: DIE_ID = id; break;
        }
    }

    @Override
    protected void initFromDirectory(String directory, AssetManager asset, Stage stage) {
        super.initFromDirectory(directory, asset, stage);
        try {
            BufferedReader reader = com.pizzaroof.sinfulrush.util.Utils.getInternalReader(com.pizzaroof.sinfulrush.util.Utils.enemyInfoPath(directory));
            for(int i=0; i<4; i++) reader.readLine();
            String strs[] = reader.readLine().split(" ");
            damage = Integer.parseInt(strs[0]);
            powerballPath = strs[1];
            explosionPath = strs[2];
            spawnPowX = Float.parseFloat(strs[3]);
            spawnPowY = Float.parseFloat(strs[4]);
            powerballSpeed = Float.parseFloat(strs[5]);
            powerballRadius = Float.parseFloat(strs[6]);
            powerballColor = Pools.PEffectColor.valueOf(strs[7]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**factory method per il boss*/
    public static Boss createBoss(World2D world, SoundManager soundManager, Vector2 initPosition, String directory, AssetManager assetManager, Stage stage, Group effectGroup, Group backgroundGroup, BossGameScreen screen, Armory armory) {
        try {
            Vector2 dim = com.pizzaroof.sinfulrush.util.Utils.enemyDrawingDimensions(directory);
            Shape shapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(Utils.enemyShapePath(directory), dim.x, dim.y, world.getPixelPerMeter());
            return new Boss(world, soundManager, initPosition, directory, assetManager, stage, effectGroup, backgroundGroup, screen, armory, shapes);
        }catch(IOException e) { //non dovrebbe succedere
            e.printStackTrace();
        }
        return null;
    }

    public void setScoreButton(ScoreButton scoreButton) {
        this.scoreButton = scoreButton;
    }

    public void setHudGroup(Group hudGroup) {
        this.hudGroup = hudGroup;
    }

    @Override
    public void freeze() {}

    @Override
    public void blowUp() {}
}
