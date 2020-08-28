package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.FriendEnemy;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;

import java.util.HashMap;
import java.util.Map;

/**attacco che permette di strisciare il dito sullo schermo e tagliare quello che trova*/
public class Sword extends Attack {

    /**nemici necessari per attivare la rage*/
    public final static int NEEDED_TO_RAGE = 20;

    /**danno*/
    private int damage;

    /**gruppo dove aggiungere vari effetti causati dalla sword*/
    private Group effectGroup;

    private AssetManager assetManager;

    /**key=finger pointer, value=swing associato*/
    private HashMap<Integer, SwordSwing> swings;

    /**massimo numero di dita che si possono usare con la spada*/
    private int maxFingers;

    /**questa spada ha il rage? ... non tutte ce l'hanno*/
    private boolean canRage;

    /**quanti nemici devo uccidere ancora per attivare la rage mode?*/
    public int remainingToRage;

    /**quanto tempo è passato dalla rage mode?*/
    private float rageTimePassed;

    /**quanto dura la rage mode?*/
    public static final float RAGE_DURATION = 4;

    /**colore delle lame*/
    private Color bladeColor;

    public Sword(Stage stage, World2D world, SoundManager soundManager, AssetManager assetManager, int damage, Group effectGroup) {
        super(stage, world, soundManager);
        this.assetManager = assetManager;
        this.damage = damage;
        this.effectGroup = effectGroup;

        swings = new HashMap<>();
        remainingToRage = NEEDED_TO_RAGE;
        canRage = false;
        maxFingers = 1;
    }

    @Override
    public void actSkipTolerant(float delta) {
        /*if(Gdx.input.isKeyJustPressed(Input.Keys.R)) { //DEBUG
            remainingToRage = 0;
            activateRage();
        }*/

        super.actSkipTolerant(delta);
        if(rageTimePassed > RAGE_DURATION) //rage esaurita
            resetRage();

        if(remainingToRage <= 0)
            rageTimePassed += delta;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        for(Map.Entry<Integer, SwordSwing> e : swings.entrySet()) { //stampa tutti gli swing

            if(remainingToRage <= 0) {
                e.getValue().setColor(Color.RED);
                e.getValue().setDamage(Constants.INFTY_HP); //one shot di tutto
            } else {
                e.getValue().setColor(bladeColor);
                e.getValue().setDamage(damage);
            }
            e.getValue().draw(batch, stage.getCamera());
        }

    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(!swings.containsKey(pointer)) swings.put(pointer, getSwordSwing());

        worldPoint = toWorldPoint(screenX, screenY);
        if(pointer < maxFingers)
            swings.get(pointer).addNewPoint(new Vector2(worldPoint.x, worldPoint.y));

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(!swings.containsKey(pointer)) swings.put(pointer, getSwordSwing());

        swings.get(pointer).clearPoints();
        return false;
    }


    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(!swings.containsKey(pointer)) swings.put(pointer, getSwordSwing());

        worldPoint = toWorldPoint(screenX, screenY);
        if(pointer < maxFingers)
            swings.get(pointer).addNewPoint(new Vector2(worldPoint.x, worldPoint.y));

        return false;
    }

    public void setRage(boolean rg) {
        canRage = rg;
    }

    public boolean canRage() {
        return canRage;
    }

    public void setMaxFingers(int fingers) {
        maxFingers = fingers;
    }

    public int getMaxFingers() {
        return maxFingers;
    }

    public void resetRage() {
        remainingToRage = NEEDED_TO_RAGE;
        rageTimePassed = 0;
        if(stage instanceof ShaderStage)
            ((ShaderStage) stage).deactivateRageMode();
    }

    /**resetta parzialmente il rage: se era in corso lo resetta, se invece mancano ancora dei nemici, lo lascia cosi*/
    public void partialRageReset() {
        if(remainingToRage <= 0)
            resetRage();
    }

    public int getRemainingToRage() {
        return remainingToRage;
    }

    public float getRageTimePassed() {
        return rageTimePassed;
    }

    public void setBladeColor(Color color) {
        bladeColor = color;
    }

    public void clearPoints() {
        for(Map.Entry<Integer, SwordSwing> e : swings.entrySet())
            e.getValue().clearPoints();
    }

    @Override
    public void resetY(float maxy) {
        for(Map.Entry<Integer, SwordSwing> e : swings.entrySet())
            e.getValue().resetY(maxy / world.getPixelPerMeter());
    }

    private SwordSwing getSwordSwing() {
        return new SwordSwing(world, assetManager, effectGroup, damage, this) {
            @Override
            protected void damageToEnemy(Enemy e) {
                super.damageToEnemy(e);
                if(e.getHp() <= 0 && canRage() && !(e instanceof FriendEnemy)) { //ucciso il nemico
                    remainingToRage--;
                    if(remainingToRage == 0) //appena iniziato rage mode
                        activateRage();
                }
            }
        };
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    private void activateRage() {
        rageTimePassed = 0;
        if(stage instanceof ShaderStage)
            ((ShaderStage)stage).activateRageMode();
    }
}
