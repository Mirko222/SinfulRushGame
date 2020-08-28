package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.screens.GameplayScreen;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.stage.DoubleActActor;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;
import com.pizzaroof.sinfulrush.util.PlayerPower;


/**armeria: classe che si occupa di gestire le armi dell'utente*/
public class Armory extends DoubleActActor implements InputProcessor {

    protected int PUNCH_MIN_DAMAGE = 10, PUNCH_MAX_DAMAGE = 40;
    protected int GLOVE_MIN_DAMAGE = 20, GLOVE_MAX_DAMAGE = 50; //NB: vengono usati per distinguere tra pugno e guanto: falli diversi (si... fa schifo, ma per ora va bene cosi)

    private int SWORD_DAMAGE = 30;
    private int RAGESWORD_DAMAGE = 35;
    private int DOUBLE_SWORD_BOOST = 5;
    private int SCEPTRE_MIN_DAMAGE = 60, SCEPTRE_MAX_DAMAGE = 120; //45, 105
    private int SCEPTRE_SPLIT_MIN_DAMAGE = 80, SCEPTRE_SPLIT_MAX_DAMAGE = 160;

    /**attacco attualmente in uso dall'utente*/
    protected com.pizzaroof.sinfulrush.attacks.Attack actualAttack;

    //informazioni standard... passate praticamente ovunque
    private Stage stage;
    private AssetManager assetManager;
    private World2D world2D;
    private GameplayScreen screen;

    /**manteniamo un riferimento all'attacco spada, in modo da conservare parametri*/
    private Sword swordAttack;

    /**attacco dello scettro (non lo ricreiamo ogni volta)*/
    private Sceptre sceptreAttack;

    /**ci salviamo l'ultimo touch-down.... può tornare utile*/
    private Vector2 lastDown;

    public Armory(Stage stage, AssetManager assetManager, World2D world2D, Group effectGroup, Group enemiesGroup, GameplayScreen screen, PlayerPower playerPower) {
        PUNCH_MIN_DAMAGE = (int)Math.ceil(PUNCH_MIN_DAMAGE * playerPower.getPunchDamageMultiplier());
        GLOVE_MIN_DAMAGE = (int)Math.ceil(GLOVE_MIN_DAMAGE * playerPower.getPunchDamageMultiplier());
        PUNCH_MAX_DAMAGE = (int)Math.ceil(PUNCH_MAX_DAMAGE * playerPower.getPunchDamageMultiplier());
        GLOVE_MAX_DAMAGE = (int)Math.ceil(GLOVE_MAX_DAMAGE * playerPower.getPunchDamageMultiplier());

        SWORD_DAMAGE = (int)Math.ceil(SWORD_DAMAGE * playerPower.getSwordDamageMultiplier());
        RAGESWORD_DAMAGE = (int)Math.ceil(RAGESWORD_DAMAGE * playerPower.getSwordDamageMultiplier());
        DOUBLE_SWORD_BOOST = (int)Math.ceil(DOUBLE_SWORD_BOOST * playerPower.getSwordDamageMultiplier());

        SCEPTRE_MIN_DAMAGE = (int)Math.ceil(SCEPTRE_MIN_DAMAGE * playerPower.getSceptreDamageMultiplier());
        SCEPTRE_MAX_DAMAGE = (int)Math.ceil(SCEPTRE_MAX_DAMAGE * playerPower.getSceptreDamageMultiplier());
        SCEPTRE_SPLIT_MIN_DAMAGE = (int)Math.ceil(SCEPTRE_SPLIT_MIN_DAMAGE * playerPower.getSceptreDamageMultiplier());
        SCEPTRE_SPLIT_MAX_DAMAGE = (int)Math.ceil(SCEPTRE_SPLIT_MAX_DAMAGE * playerPower.getSceptreDamageMultiplier());


        this.stage = stage;
        this.assetManager = assetManager;
        this.world2D = world2D;
        this.screen = screen;

        lastDown = new Vector2();

        swordAttack = new Sword(stage, world2D, screen.getSoundManager(), assetManager, SWORD_DAMAGE, effectGroup);
        sceptreAttack = new com.pizzaroof.sinfulrush.attacks.Sceptre(stage, world2D, effectGroup, enemiesGroup, assetManager, SCEPTRE_MIN_DAMAGE, SCEPTRE_MAX_DAMAGE, screen.getSoundManager());
        //sceptreAttack = new Sceptre(stage, world2D, effectGroup, enemiesGroup, 0, 0);


        actualAttack = null;
    }

    /**setta la spada come attacco*/
    public void setSwordAttack(boolean canRage, int fingers, Color bladeColor) {
        if(!(actualAttack instanceof Sword) || !swordAttack.canRage() || !canRage) //prima avevo un'arma senza rage o un'arma che non era una spada -> resetta rage; sto passando a un'arma senza rage -> resettalo
            swordAttack.partialRageReset();
        actualAttack = swordAttack;
        swordAttack.setRage(canRage);
        swordAttack.setMaxFingers(fingers);
        swordAttack.setBladeColor(bladeColor);
        swordAttack.setDamage(canRage ? RAGESWORD_DAMAGE : SWORD_DAMAGE);
        if(fingers > 1) swordAttack.setDamage(swordAttack.getDamage() + DOUBLE_SWORD_BOOST);
        swordAttack.clearPoints();
        if(!canRage && stage instanceof ShaderStage)
            ((ShaderStage) stage).deactivateRageMode();
        screen.onWeaponChanged();
    }

    /**setta il pugno come attacco principale*/
    public void setPunchAttack() {
        actualAttack = new Punch(world2D, stage, screen.getSoundManager(), PUNCH_MIN_DAMAGE, PUNCH_MAX_DAMAGE, 4, 0.18f, //0.35
                        assetManager.get(com.pizzaroof.sinfulrush.Constants.PUNCH_ATLAS), 0.2f, "1", "2", "3", "4"); //Punch.fromFile(Constants.PUNCH_DIR_LVL1, world2D, stage, assetManager); //attacco attuale
        actualAttack.setDrawingWidth(100);
        actualAttack.setDrawingHeight(100);
        if(stage instanceof ShaderStage)
            ((ShaderStage) stage).deactivateRageMode();
        screen.onWeaponChanged();
    }

    /**setta il guanto come attacco principale*/
    public void setGloveAttack() {
        actualAttack = new Punch(world2D, stage, screen.getSoundManager(), GLOVE_MIN_DAMAGE, GLOVE_MAX_DAMAGE, 4, 0.18f,
                assetManager.get(Constants.PUNCH_ATLAS), 0.2f, "11", "21", "31", "41"); //Punch.fromFile(Constants.PUNCH_DIR_LVL1, world2D, stage, assetManager); //attacco attuale
        actualAttack.setDrawingWidth(100); //100
        actualAttack.setDrawingHeight(100); //100
        if(stage instanceof ShaderStage)
            ((ShaderStage) stage).deactivateRageMode();
        screen.onWeaponChanged();
    }

    public void setSceptreAttack(boolean canSplit) {
        sceptreAttack.touchDown((int)lastDown.x, (int)lastDown.y, 0, -1); //potremmo aver saltato il touch down perché abbiamo appena preso lo scettro, quindi facciamolo a mano

        actualAttack = sceptreAttack;
        sceptreAttack.setCanSplit(canSplit);
        if(stage instanceof ShaderStage)
            ((ShaderStage) stage).deactivateRageMode();

        sceptreAttack.setPowers(canSplit ? SCEPTRE_SPLIT_MIN_DAMAGE : SCEPTRE_MIN_DAMAGE, canSplit ? SCEPTRE_SPLIT_MAX_DAMAGE : SCEPTRE_MAX_DAMAGE);

        screen.onWeaponChanged();
    }

    public Attack getActualAttack() {
        return actualAttack;
    }

    public Sword getSwordAttack() {
        return swordAttack;
    }

    public int getNeededToRage() {
        return Sword.NEEDED_TO_RAGE;
    }

    public int getRemainingToRage() {
        return swordAttack.getRemainingToRage();
    }

    public float getRageDuration() {
        return Sword.RAGE_DURATION;
    }

    public float getRageTimePassed() {
        return swordAttack.getRageTimePassed();
    }

    /**attualmente si sta utilizzando la spada? (con eventualmente rage)*/
    public boolean isUsingSword(boolean rage) {
        return (actualAttack instanceof Sword && swordAttack.canRage() == rage);
    }

    /**sta usando una spada qualsiasi? (con o senza rage)*/
    public boolean isUsingSword() {
        return isUsingSword(true) || isUsingSword(false);
    }

    /**quante dita può usare con la spada?*/
    public int getFingerSword() {
        if(!isUsingSword()) return 0;
        return swordAttack.getMaxFingers();
    }

    public boolean isUsingSceptre(boolean split) {
        return (actualAttack instanceof Sceptre && sceptreAttack.canSplit() == split);
    }

    /**sta usando scettro? sia con split che senza*/
    public boolean isUsingSceptre() {
        return isUsingSceptre(true) || isUsingSceptre(false);
    }

    public boolean isUsingPunch() {
        return (actualAttack instanceof Punch && ((Punch)actualAttack).getMinDamage() == PUNCH_MIN_DAMAGE) || actualAttack == null;
    }

    public boolean isUsingGlove() {
        return actualAttack instanceof Punch && ((Punch)actualAttack).getMinDamage() > PUNCH_MIN_DAMAGE;
    }

    /**usato per resettare y quando diventa troppo grande*/
    public void resetY(float maxy) {
        if(actualAttack != null)
            actualAttack.resetY(maxy);
    }

    @Override
    public void actSkipTolerant(float delta) {
        if(actualAttack != null)
            actualAttack.actSkipTolerant(delta);
    }

    @Override
    public void actFrameDependent(float delta) {
        if(actualAttack != null)
            actualAttack.actFrameDependent(delta);
    }

    @Override
    public void draw(Batch batch, float alpha) {
        if(actualAttack != null)
            actualAttack.draw(batch, alpha);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(screen.isInPause() || screen.player.getHp() <= 0) return false;

        lastDown.set(screenX, screenY);

        if(actualAttack != null)
            actualAttack.touchDown(screenX, screenY, pointer, button);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(screen.isInPause() || screen.player.getHp() <= 0) return false;

        if(actualAttack != null)
            actualAttack.touchUp(screenX, screenY, pointer, button);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(screen.isInPause() || screen.player.getHp() <= 0) return false;

        if(actualAttack != null)
            actualAttack.touchDragged(screenX, screenY, pointer);
        return false;
    }

    public void cleanPools() {
        sceptreAttack.cleanPools();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
