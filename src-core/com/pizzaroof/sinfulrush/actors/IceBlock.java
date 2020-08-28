package com.pizzaroof.sinfulrush.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.brashmonkey.spriter.gdx.SpriterData;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;
import com.pizzaroof.sinfulrush.util.Utils;

/**blocco di ghiaccio (può in realtà essere adattato ad altre cose)*/
public class IceBlock extends SpriterAnimActor {

    public static final Color MIN_ICE_COLOR = new Color(0.65f, 0.9f, 1.f, 0.8f), MAX_ICE_COLOR = new Color(0.5f, 0.89f, 1.f, 0.9f);

    private int SPAWN_ANIM, EXPLODE_ANIM, MELT_ANIM;

    /**l'ice block si forma intorno a questo attore che viene ghiacciato*/
    private Enemy frozenActor;

    /**indica se l'attore è già stato congelato*/
    private boolean frozen;

    /**durata del freeze in secondi (negativo = infinito)*/
    private float freezeDuration;
    /**tempo passato in freeze*/
    private float freezePassed;

    /**ci stiamo sciogliendo? è importante perché non possiamo fare affidamento solo sull'id dell'animazione
     * in quanto melt e explode potrebbero essere uguali*/
    private boolean melting;

    /**valore alpha originale (può subire variazioni dovuti a shader)*/
    private float originalAlpha;

    public IceBlock(SpriterData spriterData, Batch batch, int spawnAnim, float spawnDur, int explodeAnim, float explodeDur, int meltAnim, float meltDur, float originalWidth, float originalHeight, float freezeDuration) {
        setSpriterData(spriterData, batch);
        SPAWN_ANIM = spawnAnim;
        EXPLODE_ANIM = explodeAnim;
        MELT_ANIM = meltAnim;
        setOriginalWidth(originalWidth);
        setOriginalHeight(originalHeight);

        this.freezeDuration = freezeDuration;

        addSpriterAnimation(spawnAnim, spawnDur, Animation.PlayMode.NORMAL);
        addSpriterAnimation(explodeAnim, explodeDur, Animation.PlayMode.NORMAL);
        addSpriterAnimation(meltAnim, meltDur, Animation.PlayMode.NORMAL);

        originalAlpha = Utils.randFloat(0.7f, 0.8f);
        setColor(1, 1, 1, originalAlpha);
        init();
    }

    /**setta l'attore che va inserito nel blocco di ghiaccio*/
    public void setFrozenActor(Enemy actor) {
        frozenActor = actor;
        setHorDirection(actor.getHorDirection());

        setDrawingWidth(actor.getIceWidth());
        setDrawingHeight(actor.getIceHeight());
        setWidth(getDrawingWidth());
        setHeight(getDrawingHeight());

        recomputePosition();
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);
        recomputePosition();
        if(isFrozen() && getCurrentSpriterAnimation() == SPAWN_ANIM) {
            freezePassed += delta;
            if(freezeDuration > 0 && freezePassed >= freezeDuration)
                melt();
        }

        if(getStage() instanceof ShaderStage && ((ShaderStage) getStage()).isRageModeOn())
            setColor(1, 0, 0, originalAlpha * 0.75f);
        else
            setColor(1, 1, 1, originalAlpha);
    }

    protected void recomputePosition() {
        if(frozenActor != null) {
            Vector2 tmp = frozenActor.aliveCenterPosition();
            setPositionFromCenter(tmp.x + frozenActor.getIceOffsetX(), tmp.y + frozenActor.getIceOffsetY());
        }
    }

    public void freeze() {
        if(getCurrentSpriterAnimation() != SPAWN_ANIM) {
            setSpriterAnimation(SPAWN_ANIM);
            freezePassed = 0;
            melting = false;
            setVisible(true);
        }
    }

    public void explode() {
        if(getCurrentSpriterAnimation() != EXPLODE_ANIM) {
            setSpriterAnimation(EXPLODE_ANIM);
            melting = false;
            if(frozenActor != null) frozenActor.setVisible(false);
        }
    }

    public void melt() {
        if(getCurrentSpriterAnimation() != MELT_ANIM) {
            setSpriterAnimation(MELT_ANIM);
            melting = true;
        }
    }

    @Override
    protected void onSpriterAnimationExecuting(int id, int actframe, int totframes) {
        if(id == SPAWN_ANIM && actframe >= totframes * 0.3f) //ho superato il 30% dell'animazione di congelamento: mi considero freezato
            frozen = true;
        if(melting && actframe >= totframes * 0.3f)
            frozen = false;
    }

    @Override
    protected void onSpriterAnimationEnded(int id) {
        if(id == EXPLODE_ANIM && !melting) { //quando esplode, rimuoviamo l'attore
            if(frozenActor != null) frozenActor.remove();
            frozenActor = null;
            remove();
        }
        if(melting) {
            remove();
        }
    }

    /**è congelato?*/
    public boolean isFrozen() {
        return frozen;
    }

    /**come isFrozen(), ma considera anche l'inizio del congelamento*/
    public boolean isFreezing() {
        return getCurrentSpriterAnimation() == SPAWN_ANIM || isFrozen();
    }

    public void init() {
        frozen = false;
        setVisible(false);
        setSpriterAnimation(-1);
        melting = false;
    }
}
