package com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;

/**nemico con attacchi corpo a corpo, è organizzato a livello fisico come un Enemy generico.*/
public class MeleeEnemy extends com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.PlatformEnemy {

    /**tempo di ricarica per l'attacco corpo a corpo e range rinchiesto*/
    protected float attackRechargeTime, attackRangeNeeded;

    /**danno dell'attacco corpo a corpo*/
    protected int attackDamage;

    /**tempo passato dall'ultimo attacco*/
    protected float timePassed;

    /**flag per sapere se abbiamo già fatto danno con l'attacco corrente o no*/
    protected boolean damageDone;

    /**
     * sull'ultima riga del file info.txt (la quinta) aggiungiamo informazioni necessarie al nemico:
     * danno_attacco ricarica_attacco distanza_per_attaccare
     * */
    protected MeleeEnemy(com.pizzaroof.sinfulrush.actors.physics.World2D world, SoundManager soundManager, Stage stage, float density, float friction, float restitution, String directory, AssetManager asset, Vector2 initPosition, Group behindGroup, Group effectGroup, Shape... shapes) {
        super(world, soundManager, stage, density, friction, restitution, directory, asset, initPosition, behindGroup, effectGroup,shapes);
        damageDone = false;
    }

    @Override
    protected void onSpriterAnimationExecuting(int id, int actualFrame, int totFrames) {
        super.onSpriterAnimationExecuting(id, actualFrame, totFrames);

        if(id == ATTACK_ANIM && !damageDone && 4*actualFrame >= totFrames) //esattamente a un quarto dell'animazione d'attacco facciamo danno al giocatore
            attack();
    }

    /**effettua veramente l'attacco*/
    protected void attack() {
        player.takeDamage(attackDamage, this);
        damageDone = true;
    }

    @Override
    protected void onSpriterAnimationEnded(int id) {
        super.onSpriterAnimationEnded(id);

        if(id == ATTACK_ANIM) { //ho finito di attaccare... vado in idle perchè dovrò aspettare il tempo di ricarica
            setSpriterAnimation(IDLE_ANIM);
            damageDone = false; //non ho fatto danni con l'attacco successivo
            timePassed = 0; //resetto il tempo da aspettare
        }
    }

    @Override
    protected void updateWhenPlayerIsOnPlatform(float delta) {
        //in sostanza: ci avviciniamo e iniziamo ad attaccarlo...

        float myX = getBody().getPosition().x * pixelPerMeter; //posizioni interne
        float playerX = player.getBody().getPosition().x * pixelPerMeter;
        int dir = myX < playerX ? 1 : -1; //direzione di movimento

        boolean firstToAttack = true; //vediamo se siamo i primi ad attaccare (potrebbero esserci nemici davanti a noi sulla piattaforma, che vanno prima)
        if(wantsToMeleeAttack()) {
            for (PlatformEnemy e : myPlatform.getEnemies()) //controlla se qualcuno ancora vivo vuole attaccare primadi noi
                if (e.hashCode() != hashCode() && e.getHp() > 0 && e.getX() * dir > getX() * dir) {
                    firstToAttack = false;
                    break;
                }
        } else //se non vuole attaccare manco controlliamo
            firstToAttack = false;

        if(firstToAttack) { //è il tuo turno di attaccare

            float myBoundingX = myX + getWidth() * 0.5f * dir; //mia x "esterna" (non interna al bounding box)
            float playerBoundingX = playerX + player.getWidth() * 0.5f * dir * -1; //x del giocatore esterna

            //la distanza tra nemico e giocatore è definita come la distanza tra le x esterne
            //NB: non facciamo valore assoluto, in modo da gestire casi in cui i bounding box si scontrano (altrimenti il nemico inizierebbe a fare avanti e indietro all'infinito)
            float distance = (playerBoundingX - myBoundingX) * dir;

            if (distance <= attackRangeNeeded) { //se sono abbastanza vicino per attaccare...
                getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);
                if (timePassed >= attackRechargeTime) //ho aspettato il mio tempo di ricarica... attacco
                    setSpriterAnimation(ATTACK_ANIM);
                else if (getCurrentSpriterAnimation() != HURT_ANIM) //mentre stiamo fermi possiamo fare animazione "hurt"
                    setSpriterAnimation(IDLE_ANIM);
            } else { //non sono abbastanza vicino per attaccare, devo avvicinarmi
                setSpriterAnimation(RUNNING_ANIM);
                getBody().setLinearVelocity(dir * speed, getBody().getLinearVelocity().y);
            }
        }
        else { //non è ancora il tuo turno di attaccare... aspetta
            float destX = dir > 0 ? computeMaxXOnPlatform() : computeMinXOnPlatform(); //vai il più possibile vicino al player
            int dirToDist = myX < destX ? 1 : -1;
            if(Math.abs(myX - destX) > EN_EPS * pixelPerMeter) { //ti devi muovere ancora
                getBody().setLinearVelocity(dirToDist * speed, getBody().getLinearVelocity().y);
                setSpriterAnimation(RUNNING_ANIM);
            }
            else { //arrivato, stai fermo
                getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);
                if(getCurrentSpriterAnimation() != HURT_ANIM) //mentre sei fermo puoi fare animazione hurt
                    setSpriterAnimation(IDLE_ANIM);
            }
        }

        setHorDirection(dir > 0 ? SpriteActor.HorDirection.RIGHT : SpriteActor.HorDirection.LEFT);
        if(getCurrentSpriterAnimation() != ATTACK_ANIM) //non sto attaccando... aumento il tempo che sto passando senza attaccare
            timePassed += delta;
    }

    @Override
    protected float getMySafeX(float playerLandingX, boolean leftSide) {
        return playerLandingX + (getWidth() * 0.5f + attackRangeNeeded + player.getWidth() * .5f) * (leftSide ? 1 : -1);
    }

    @Override
    protected boolean isPlayerArrived() {
        return getCurrentSpriterAnimation() == ATTACK_ANIM || super.isPlayerArrived();
    }

    @Override
    protected void resetAnimationOnPlayerDeath() {
        if(getCurrentSpriterAnimation() == ATTACK_ANIM) return;
        super.resetAnimationOnPlayerDeath();
    }

    public void setAttackDamage(int dmg) {
        attackDamage = dmg;
    }

    /**@param rechargeTime tempo di ricarica in secondi*/
    public void setAttackRechargeTime(float rechargeTime) {
        attackRechargeTime = rechargeTime;
        timePassed = attackRechargeTime;
    }

    /**@param rangeNeeded distanza minima richiesta per attaccare, il pixel*/
    public void setAttackRangeNeeded(float rangeNeeded) {
        attackRangeNeeded = rangeNeeded;
    }

    /**questo nemico vorrebbe attaccare? in questo caso si, sempre... per nemici che ereditano, potrebbe non essere vero*/
    protected boolean wantsToMeleeAttack() {
        return true;
    }

    @Override
    protected void initFromDirectory(String directory, AssetManager asset, Stage stage) {
        super.initFromDirectory(directory, asset, stage);
        try {
            BufferedReader reader = com.pizzaroof.sinfulrush.util.Utils.getInternalReader(com.pizzaroof.sinfulrush.util.Utils.enemyInfoPath(directory));
            for (int i = 0; i < 4; i++) reader.readLine(); //leggo le prime 4 linee che non ci servono in questo caso
            String strs[] = reader.readLine().split(" ");
            setAttackDamage(Integer.parseInt(strs[0])); //setta proprietà attacco
            setAttackRechargeTime(Float.parseFloat(strs[1]));
            setAttackRangeNeeded(Float.parseFloat(strs[2]));
            reader.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**factory method per creare un nemico dalla directory (e altre cose ovvie)*/
    public static MeleeEnemy createEnemy(String directory, SoundManager soundManager, AssetManager assetManager, World2D world, Stage stage, Vector2 initPosition, Group behindGroup, Group effectGroup) {
        try {
            Vector2 dim = com.pizzaroof.sinfulrush.util.Utils.enemyDrawingDimensions(directory);
            Shape shapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(Utils.enemyShapePath(directory), dim.x, dim.y, world.getPixelPerMeter());
            return new MeleeEnemy(world, soundManager, stage, 0, 0, 0, directory, assetManager, initPosition, behindGroup, effectGroup, shapes);
        }catch(IOException e) { //non dovrebbe succedere
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void reset() {
        super.reset();
        timePassed = attackRechargeTime;
        damageDone = false;
    }
}
