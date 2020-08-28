package com.pizzaroof.sinfulrush.actors.physics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.util.ArrayList;

/**attore a cui possiamo aggiungere effetti particellari*/
public class ParticleActor extends PhysicSpriteActor {

    /**lista di tutti gli effetti particellari su quest'attore*/
    protected ArrayList<ParticleEffectPool.PooledEffect> effects = new ArrayList<>();

    /**le particelle seguono il body... se questo dovesse sparire stiamo fermi all'ultima posizione registrata*/
    private Vector2 lastParticlePosition = new Vector2();

    /**effetti da rimuovere*/
    private ArrayList<ParticleEffectPool.PooledEffect> efsToRemove = new ArrayList<>();

    public ParticleActor(World2D world, BodyDef.BodyType bodyType, float density, float friction, float restitution, Vector2 initPosition, boolean isSensor, short categoryBits, short maskBits, boolean disposeShapes, Shape... shapes) {
        super(world, bodyType, density, friction, restitution, initPosition, isSensor, categoryBits, maskBits, disposeShapes, shapes);
    }

    public ParticleActor(World2D world, BodyDef.BodyType bodyType, float density, float friction, float restitution, Vector2 initPosition, boolean isSensor, short categoryBits, short maskBits, Shape... shapes) {
        super(world, bodyType, density, friction, restitution, initPosition, isSensor, categoryBits, maskBits, shapes);
    }

    public ParticleActor(World2D world, BodyDef.BodyType bodyType, float density, float friction, float restitution, Vector2 initPosition, boolean isSensor, Shape... shapes) {
        super(world, bodyType, density, friction, restitution, initPosition, isSensor, shapes);
    }

    public ParticleActor(World2D world, BodyDef.BodyType bodyType, float density, float friction, float restitution, Vector2 initPosition, Shape... shapes) {
        super(world, bodyType, density, friction, restitution, initPosition, shapes);
    }

    public ParticleActor(World2D world, BodyDef.BodyType bodyType, float density, float friction, float restitution, Shape... shapes) {
        super(world, bodyType, density, friction, restitution, shapes);
    }

    @Override
    public void actFrameDependent(float delta) {
        //è frame dependent perché lo vogliamo dopo il world
        super.actFrameDependent(delta);

        updateParticles(delta);

        for(ParticleEffectPool.PooledEffect e : efsToRemove) { //rimuovili dopo il for
            effects.remove(e);
            e.free();
        }
        efsToRemove.clear();
    }

    protected void updateParticles(float delta) {
        if(getBody() != null) {
            Vector2 vt = getBody().getPosition(); //mettili alla posizione del body
            lastParticlePosition.set(vt.x * pixelPerMeter, vt.y * pixelPerMeter);
        }

        for(ParticleEffectPool.PooledEffect effect : effects) { //aggiorna gli effetti particellari
            effect.setPosition(lastParticlePosition.x, lastParticlePosition.y);
            if(effect.isComplete()) //completato... dobbiamo rimuoverlo
                efsToRemove.add(effect);
            effect.update(delta); //aggiornali
        }
    }

    @Override
    public void draw(Batch batch, float alpha) {
        super.draw(batch, alpha);

        for(ParticleEffectPool.PooledEffect effect : effects) //ristampa tutti gli effetti
            effect.draw(batch);
    }

    @Override
    public boolean remove() { //quando rimuoviamo l'attore rilasciamo gli effetti
        boolean r = super.remove();
        clearEffects();
        return r;
    }

    /**permetti alle particelle @index di completarsi*/
    public void allowCompletion(int index) {
        if(effects.size() > index)
            effects.get(index).allowCompletion();
    }

    /**setta la proprietà continuous dell'effetto @effectIndex (indicizzati come vengono inseriti)*/
    public void setContinuousState(int effectIndex, boolean continuous) {
        for(ParticleEmitter em : effects.get(effectIndex).getEmitters())
            em.setContinuous(continuous);
    }

    /**aggiunge un effetto alla lista dei disponibili*/
    public void addEffect(String effect) {
        addEffect(com.pizzaroof.sinfulrush.util.pools.Pools.obtainEffect(effect));
    }

    /**aggiunge effetto con una colorazione (NB: alcuni effetti potrebbero non essere adatti a certe colorazioni)*/
    public void addEffect(String effect, com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor color) {
        addEffect(com.pizzaroof.sinfulrush.util.pools.Pools.obtainColoredEffect(effect, color));
    }

    public void colorEffect(int index, Pools.PEffectColor color) {
        if(index < 0 || index >= effects.size()) return;
        
    }

    public void addEffect(ParticleEffectPool.PooledEffect effect) {
        //effect.start();
        effects.add(effect);
    }

    /**togli tutti effettia ancora in corso*/
    protected void clearEffects() {
        for(ParticleEffectPool.PooledEffect effect : effects)
            effect.free();
        effects.clear();
    }

    @Override
    public void reset() {
        super.reset();
        clearEffects();
    }

    @Override
    public void removeAndFree() {
        clearEffects();
        super.removeAndFree();
    }
}
