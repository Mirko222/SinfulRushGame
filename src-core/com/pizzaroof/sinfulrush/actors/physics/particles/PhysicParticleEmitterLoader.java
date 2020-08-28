package com.pizzaroof.sinfulrush.actors.physics.particles;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.pizzaroof.sinfulrush.Constants;

/**loader per emettitore di particelle fisiche*/
public class PhysicParticleEmitterLoader extends AsynchronousAssetLoader<PhysicParticleEmitter, PhysicParticleEmitterLoader.PhysicParticleEmitterParameter> {

    private PhysicParticleEmitter emitter;

    public PhysicParticleEmitterLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, PhysicParticleEmitterParameter parameter) {
        emitter = new PhysicParticleEmitter(fileName, manager.get(parameter.atlasPath, TextureAtlas.class), parameter.minParticleRadius, parameter.maxParticleRadius);
    }

    @Override
    public PhysicParticleEmitter loadSync(AssetManager manager, String fileName, FileHandle file, PhysicParticleEmitterParameter parameter) {
        PhysicParticleEmitter tmp = emitter;
        emitter = null;
        return tmp;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, PhysicParticleEmitterParameter parameter) {
        return null;
    }

    /**parametri per il loader: in sostanza solo l'atlas dove sono i file delle particelle*/
    static public class PhysicParticleEmitterParameter extends AssetLoaderParameters<PhysicParticleEmitter> {
        public String atlasPath = Constants.PHYSIC_PARTICLE_ATLAS;
        public int minParticleRadius, maxParticleRadius;
    }
}
