package com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.attacks.FollowingPowerball;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.io.BufferedReader;
import java.io.IOException;

/**nemico che sta sulla piattaforma e lancia sfere.
 * linea aggiuntiva:
 * powerball_path powerball_speed(m/s) powerball_radius(metri) offset_x_spawn_ball(su drawingWidth x drawingHeight) offset_y_spawn_ball effetto_esplosione colore_effetto*/
public class PlatformSniperEnemy extends MeleeEnemy {

    //vari attributi della sfera
    protected String powerballPath;
    protected float powerballSpeed, powerballRadius;
    protected int powerballOffsetX, powerballOffsetY;
    protected String powerballExplosionPath;
    protected com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor effectColor;

    protected PhysicSpriteActor target;
    protected Vector2 backupDirection;

    protected PlatformSniperEnemy(com.pizzaroof.sinfulrush.actors.physics.World2D world, SoundManager soundManager, Stage stage, float density, float friction, float restitution, String directory, AssetManager asset, Vector2 initPosition, Group backgroundGroup, Group effectGroup, Shape... shapes) {
        super(world, soundManager, stage, density, friction, restitution, directory, asset, initPosition, backgroundGroup, effectGroup, shapes);
    }

    @Override
    protected void attack() { //in sostanza genera una powerball da lanciare al target
        Vector2 spawn;
        if(getHorDirection().equals(originalDirection)) //non serve flippare l'offset
            spawn = new Vector2((getX() + powerballOffsetX) / world.getPixelPerMeter(), (getY() + powerballOffsetY) / world.getPixelPerMeter());
        else //bisogna flippare l'offset su x
            spawn = new Vector2((getX() + getDrawingWidth() - powerballOffsetX) / world.getPixelPerMeter(), (getY() + powerballOffsetY) / world.getPixelPerMeter());

        FollowingPowerball powerball = new FollowingPowerball(world, powerballPath, spawn, powerballSpeed, attackDamage, powerballRadius, target, backupDirection, effectColor);
        powerball.setExplosionEffect(powerballExplosionPath);
        effectGroup.addActor(powerball);

        damageDone = true; //serve per non rilanciare subito un'altra powerball
    }

    @Override
    protected void updateWhenPlayerHasntArrived(float delta) {
        computeTarget();
        if(getCurrentSpriterAnimation() != ATTACK_ANIM)
            timePassed += delta;
        super.updateWhenPlayerHasntArrived(delta);
    }

    @Override
    protected void moveAlongPlatform() {
        if(getCurrentSpriterAnimation() != ATTACK_ANIM && !tryToAttack()) { //prova prima ad attaccare... poi muoviti
            if(getActualState().equals(EnemyState.FIGHTING)) //se prima ha attaccato, adesso dobbiamo tornare a muoverci
                setState(EnemyState.MOVING);
            super.moveAlongPlatform();
        }
    }

    @Override
    protected float getMySafeX(float playerLandingX, boolean leftSide) {
        return playerLandingX + (getWidth() + player.getWidth() * .5f) * (leftSide ? 1 : -1);
    }

    @Override
    protected void makeSpaceForPlayer() {
        if(!getActualState().equals(EnemyState.FIGHTING) || !tryToAttack()) { //dai priorità a fare spazio, ma se stai in fightning puoi attaccare
            if(getCurrentSpriterAnimation() == ATTACK_ANIM) { //situazione speciale: potremmo essere interrotti mentre attaccavamo... consideriamo l'attacco consluso
                //if(!damageDone) attack(); //se non avevamo fatto in tempo a fare danno... attacca e basta, consideralo concluso (altro modo di vederla: se non hai fatto in tempo ad attaccare, abortisci l'attacco...)
                if(damageDone) { //abbiamo attaccato veramente, quindi resettiamo
                    damageDone = false;
                    timePassed = 0;
                }
            }
            super.makeSpaceForPlayer();
        }
    }

    @Override
    protected void updateWhenPlayerIsOnPlatform(float delta) {
        //se posso attaccare attacco, sennò faccio quello che farebbe un melee

        computeTarget();
        if(!tryToAttack() && getCurrentSpriterAnimation() != ATTACK_ANIM) { //se posso attaccare ok, fai quello
            if(isCloseEnough()) { //non posso attaccare ma sto vicino... allora fermo e basta
                getBody().setLinearVelocity(0, 0);
                lookAtTheTarget();
                if(getCurrentSpriterAnimation() != HURT_ANIM)
                    setSpriterAnimation(IDLE_ANIM);
                timePassed += delta;
            }
            else
                super.updateWhenPlayerIsOnPlatform(delta); //faccio quello che farebbe un melee: gli vado vicino
        }
    }

    @Override
    public void changeToHurtAnimation() {
        if(getCurrentSpriterAnimation() == ATTACK_ANIM) return;
        super.changeToHurtAnimation();
    }

    /**prova ad attaccare, torna true se ci riesce*/
    protected boolean tryToAttack() {
        if(canAttack()) {
            lookAtTheTarget();
            getBody().setLinearVelocity(0, 0);
            if(getCurrentSpriterAnimation() != ATTACK_ANIM)
                setSpriterAnimation(ATTACK_ANIM);
            return true;
        }
        return false;
    }

    /**può iniziare attacco?*/
    protected boolean canAttack() {
        return isCloseEnough() && timePassed >= attackRechargeTime;
    }

    protected boolean isCloseEnough() {
        if(target == null) return false;
        return getBody().getPosition().dst2(target.getBody().getPosition()) <= attackRangeNeeded * attackRangeNeeded;
    }

    /**guarda target*/
    protected void lookAtTheTarget() {
        if(target == null) return;
        if(getBody().getPosition().x < target.getBody().getPosition().x)
            setHorDirection(SpriteActor.HorDirection.RIGHT);
        else
            setHorDirection(SpriteActor.HorDirection.LEFT);
    }

    protected void computeTarget() {
        target = player.getHp() > 0 ? player : null; //attacca sempre il giocatore

        if(target != null)
            backupDirection = target.getBody().getPosition().cpy().sub(getBody().getPosition()).nor();
        else
            if(backupDirection == null)
                backupDirection = new Vector2().setToRandomDirection();
    }

    @Override
    protected void initFromDirectory(String directory, AssetManager asset, Stage stage) {
        super.initFromDirectory(directory, asset, stage);
        try {
            BufferedReader reader = com.pizzaroof.sinfulrush.util.Utils.getInternalReader(com.pizzaroof.sinfulrush.util.Utils.enemyInfoPath(directory));
            for (int i = 0; i < 5; i++) reader.readLine(); //leggo le prime 5 linee che non ci servono in questo caso
            String strs[] = reader.readLine().split(" ");
            powerballPath = strs[0];
            powerballSpeed = Float.parseFloat(strs[1]);
            powerballRadius = Float.parseFloat(strs[2]);
            powerballOffsetX = Integer.parseInt(strs[3]);
            powerballOffsetY = Integer.parseInt(strs[4]);
            powerballExplosionPath = strs[5];
            effectColor = Pools.PEffectColor.valueOf(strs[6]);
            reader.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean wantsToMeleeAttack() { //non vogliamo mai fare attacchi meleee
        return false;
    }

    /**factory method per creare un nemico dalla directory (e altre cose ovvie)*/
    public static PlatformSniperEnemy createEnemy(String directory, SoundManager soundManager, AssetManager assetManager, World2D world, Stage stage, Vector2 initPosition, Group behindGroup, Group effectGroup) {
        try {
            Vector2 dim = com.pizzaroof.sinfulrush.util.Utils.enemyDrawingDimensions(directory);
            Shape shapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(Utils.enemyShapePath(directory), dim.x, dim.y, world.getPixelPerMeter());
            return new PlatformSniperEnemy(world, soundManager, stage, 0, 0, 0, directory, assetManager, initPosition, behindGroup, effectGroup, shapes);
        }catch(IOException e) { //non dovrebbe succedere
            e.printStackTrace();
        }
        return null;
    }
}
