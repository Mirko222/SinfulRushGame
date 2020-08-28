package com.pizzaroof.sinfulrush.actors.physics.game_actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.MeleeEnemy;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.DamageFlashText;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.util.PlayerPower;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.io.IOException;

/**un player in cui aggiungiamo qualche dettaglio juicy*/
public class JuicyPlayer extends Player {

    public static final int DEF_VIBRATE_MILLIS = 300;

    public static final float INIT_COOLDOWN = 0.7f;

    /**chi è l'ultimo melee enemy che ci ha attaccato e quante volte di fila l'ha fatto*/
    protected int lastMeleeEnemyAttacked, numHitFromLastMelee;

    /**tra due vibrazioni devono passare almeno MIN_TIME_TO_VIBRATE sec*/
    protected static final float MIN_TIME_TO_VIBRATE = 0.6f;
    protected float timePassedSinceVibration;

    protected boolean canVibrate;

    /**all'inizio facciamo che non può muoversi*/
    protected boolean canMove;
    /**tempo passato immobile*/
    protected float idleTimePassed;

    protected SoundManager soundManager;

    private DamageFlashText damageFlashText;
    private Vector2 damageFlashOffset;

    private AssetManager assetManager;

    private Group effectGroup;

    /**
     * @param world
     * @param stage
     * @param density
     * @param initPosition
     * @param speed        velocità del player
     * @param directory    ogni giocatore è organizzato in una directory: la directory contiene un file scml con tutte le animazioni di cui il giocatore ha bisogno
     *                     contiene inoltre un file .txt con delle informazioni aggiuntive sul giocatore:
     *                     il file .txt è organizzato cosi:
     *                     Original_width_in_Spriter Draw_Width Draw_Height Width Height //(tenendo conto della dimensione virtuale dello schermo) [NB: width height sono le dimensioni vere dello sprite, le due drawing servono solo per stampare] [la larghezza in spriter, serve a ridimensionare le animazioni di spriter]
     *                     running_id running_duration start_jumping_id start_jumping_duration jumping_id jumping_duration falling_id falling_duration //coppie id, durata per ogni animazione in spriter
     *                     idle_id idle_duration hurt_id hurt_duration dying_id dying_duration (sono sulla stessa riga delle animazioni di sopra)
     *                     contiene inoltre un file shape.txt contente informazioni sullo shape del personaggio (organizzato in modo che possa essere letto da Utils.getShapesFromFile)
     * @param assetManager
     * @param shapes
     */
    protected JuicyPlayer(com.pizzaroof.sinfulrush.actors.physics.World2D world, Stage stage, SoundManager soundManager, float density, Vector2 initPosition, float speed, String directory, AssetManager assetManager, PlayerPower powers, boolean canVibrate, Group effectGroup, Shape... shapes) {
        super(world, stage, density, initPosition, speed, directory, assetManager, powers, shapes);
        this.soundManager = soundManager;
        this.assetManager = assetManager;
        this.effectGroup = effectGroup;
        damageFlashOffset = new Vector2();

        lastMeleeEnemyAttacked = -1;
        numHitFromLastMelee = -1;
        timePassedSinceVibration = MIN_TIME_TO_VIBRATE + 1;
        this.canVibrate = canVibrate;
        canMove = false;
        idleTimePassed = 0;
    }

    @Override
    public void playerUpdate(float delta) {
        if(canMove)
            super.playerUpdate(delta);
        else
            setSpriterAnimation(IDLE_ID);
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);

        if(damageFlashText != null) { //se ce ne sono più di uno, alcuni non verranno trasportati... ma tanto spariranno a breve
            Vector2 center = centerPosition();
            damageFlashText.setPosition(center.x + damageFlashOffset.x, center.y + damageFlashOffset.y);
        }
        if(timePassedSinceVibration <= MIN_TIME_TO_VIBRATE) //appena supera min, non ci interessa mantenere il vero valore, quindi ci fermiamo
            timePassedSinceVibration += delta;
        if(idleTimePassed <= INIT_COOLDOWN) {
            idleTimePassed += delta;
            if(idleTimePassed > INIT_COOLDOWN)
                canMove = true;
        }
    }

    @Override
    public void heal(int v) {
        int hp1 = getHp();
        super.heal(v);

        if(hp1 < getHp()) { //ha curato veramente qualcosa
            //aggiungiamo un effetto particellare per le cure
            addEffect(com.pizzaroof.sinfulrush.Constants.HEAL_EFFECT);
            printDamage(v, com.pizzaroof.sinfulrush.util.Utils.randomDamagePosition(this), Color.GREEN);
            soundManager.healthPotion();
        }
    }

    @Override
    public void hurt() {
        super.hurt();
        soundManager.playerHurt();
    }

    /*@Override
    protected void jump(Platform actual, Platform next) {
        super.jump(actual, next);
        soundManager.jump();
    }*/

    @Override
    public void die() {
        super.die();
        soundManager.playerDeath();
    }

    @Override
    public void takeDamage(int dmg, PhysicSpriteActor attacker) {
        int prevHp = getHp();

        super.takeDamage(dmg, attacker);

        if(getHp() < prevHp) {
            Vector2 hitPoint;
            if(attacker == null)
                hitPoint = com.pizzaroof.sinfulrush.util.Utils.randomDamagePosition(this);
            else {
                Vector2 attPos = attacker.centerPosition();
                Vector2 myPos = centerPosition();
                hitPoint = new Vector2( attPos.x * 0.4f + myPos.x * 0.6f, myPos.y + getHeight() * com.pizzaroof.sinfulrush.util.Utils.randFloat(0.5f, 0.7f));
            }

            printDamage(prevHp - getHp(), hitPoint, Color.RED);
        }

        //---- motivazione della vibrazione: ricordiamo all'utente che c'è un nemico che lo sta attaccando (a volte ci si scorda dei melee)
        if(attacker instanceof MeleeEnemy) {  //IDEA BASE: ogni due hit dallo stesso melee enemy, facciamo vibrare il cellulare
            int hash = attacker.hashCode();
            if(hash != lastMeleeEnemyAttacked || numHitFromLastMelee < 0) { //prima volta che questo nemico ci attacca
                lastMeleeEnemyAttacked = hash;
                numHitFromLastMelee = 1;
            }
            else //stesso melee ha attaccato più volte
                numHitFromLastMelee++;

            if(numHitFromLastMelee%2 == 1 && canVibrate) { //ogni 2 hit, vibriamo
                Gdx.input.vibrate(numHitFromLastMelee > 2 ? (int)(1.2f * DEF_VIBRATE_MILLIS) : DEF_VIBRATE_MILLIS);
            }
        }

        if(timePassedSinceVibration < MIN_TIME_TO_VIBRATE)
            return;

        //----- motivazione: ricordiamo all'utente che sta per morire
        for(int i=0; i<=2; i++)
            if(canVibrate && prevHp > 5 + 10*i && getHp() <= 5 + 10*i) { //da 25 hp, ogni 10 hp persi vibriamo (cioè vibriamo a 25hp, 15hp, 5hp)
                Gdx.input.vibrate(DEF_VIBRATE_MILLIS);
                timePassedSinceVibration = 0;
                break; //non vogliamo vibrare più volte
            }
    }

    protected void printDamage(int dmg, Vector2 hitPoint, Color color) {
        if(damageFlashText == null || damageFlashText.isDecreasing() || !com.pizzaroof.sinfulrush.util.Utils.sameColorRGB(damageFlashText.getColor(), color)) {

            Vector2 center = centerPosition();
            damageFlashOffset.set(hitPoint.x - center.x, hitPoint.y - center.y);
            damageFlashText = null;
            //damageFlashText = new DamageFlashText(assetManager.get(Constants.DEFAULT_SKIN_PATH), null);
            damageFlashText = Pools.obtainFlashText(assetManager.get(Constants.DEFAULT_SKIN_PATH, FreeTypeSkin.class));
            damageFlashText.setPosition(hitPoint.x, hitPoint.y);
            damageFlashText.setDuration(com.pizzaroof.sinfulrush.util.Utils.randFloat(0.6f, 1.f));
            damageFlashText.setColor(color);

            final short id = damageFlashText.getId();
            damageFlashText.setOnRemoveCallback(new Runnable() {
                @Override
                public void run() {
                    if(damageFlashText != null && damageFlashText.getId() == id)
                        damageFlashText = null;
                }
            });
            effectGroup.addActor(damageFlashText);
        }

        damageFlashText.increaseDamage(dmg);
    }


    /**factory method per creare un giocatore dalla sua cartella*/
    public static JuicyPlayer createPlayer(String directory, World2D world, float density, Vector2 initPosition, float speed, AssetManager assetManager, Stage stage, boolean canVibrate, SoundManager soundManager, Group effectGroup, PlayerPower powers, String soundtrackName) throws IOException {
        String strs[] = com.pizzaroof.sinfulrush.util.Utils.getInternalReader(com.pizzaroof.sinfulrush.util.Utils.playerInfoPath(directory)).readLine().split(" "); //legge dimensioni player dal file info
        float bbw = Float.parseFloat(strs[1]); //in realtà leggiamo le dimensioni di drawing
        float bbh = Float.parseFloat(strs[2]);
        Shape shapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(Utils.playerShapePath(directory), bbw, bbh, world.getPixelPerMeter()); //crea shapes
        return new JuicyPlayer(world, stage, soundManager, density, initPosition, speed, directory, assetManager, powers, canVibrate, effectGroup, shapes); //ora possiamo creare player
    }
}
