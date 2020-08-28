package com.pizzaroof.sinfulrush.actors.stage;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.pizzaroof.sinfulrush.attacks.Armory;
import com.pizzaroof.sinfulrush.audio.SoundManager;

/**stage che gestisce vari effetti grafici con shaders*/
public class ShaderStage extends TimescaleStage {

    /**è attiva la rage mode?*/
    protected boolean rageModeOn;

    protected Armory armory;

    protected float slowTimeDurationTot;

    /**potremmo prendere degli slowtime quando abbiamo già altri slowtime... dobbiamo considerare un offset per non far ripartire lo shader*/
    protected float slowTimeStartOffset;

    protected boolean darkBackground;

    protected SoundManager soundManager;

    public ShaderStage(ShaderProgram screenShader) {
        super();
        rageModeOn = false;
        darkBackground = false;

        getBatch().setShader(screenShader);

        setRoot(new ShaderRoot(screenShader));
        ((ShaderRoot)getRoot()).setStage(this);
        slowTimeStartOffset = 0;
    }

    /**usare questa per aggiungere l'armory, non addActor*/
    public void setArmory(Armory armory) {
        addActor(armory);
        this.armory = armory;
    }

    @Override
    public void setTemporalyTimeMultiplier(float timeMul, float duration) {
        slowTimeStartOffset = getTimeMultiplierDuration() > 0 ? Math.max(0, slowTimeStartOffset + slowTimeDurationTot - getTimeMultiplierDuration()) : 0;

        super.setTemporalyTimeMultiplier(timeMul, duration);
        slowTimeDurationTot = duration;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        float ragePassed = rageModeOn ? armory.getSwordAttack().getRageTimePassed() : 0;
        ((ShaderRoot)getRoot()).setRageTimePassed(ragePassed);
        ((ShaderRoot)getRoot()).setSlowtime(getTimeMultiplierDuration() > 0 ? slowTimeStartOffset + slowTimeDurationTot - getTimeMultiplierDuration() : 0);
        ((ShaderRoot)getRoot()).setDarkbackground(darkBackground);
    }

    public void activateRageMode() {
        rageModeOn = true;
        soundManager.rage();
    }

    public void setSoundManager(SoundManager sm) {
        soundManager = sm;
    }

    public void deactivateRageMode() {
        rageModeOn = false;
    }

    public boolean isRageModeOn() {
        return rageModeOn;
    }

    public void setDarkBackground(boolean dbg) {
        darkBackground = dbg;
    }

    public float getRageTimePassed() {
        return armory.getSwordAttack().getRageTimePassed();
    }
}
