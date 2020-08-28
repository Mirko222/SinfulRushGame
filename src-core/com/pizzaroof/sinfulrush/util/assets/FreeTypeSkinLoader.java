package com.pizzaroof.sinfulrush.util.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class FreeTypeSkinLoader extends AsynchronousAssetLoader<FreeTypeSkin, FreeTypeSkinLoader.SkinParameter> {
    public FreeTypeSkinLoader (FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, SkinParameter parameter) {
        Array<AssetDescriptor> deps = new Array<>();
        if (parameter == null || parameter.textureAtlasPath == null)
            deps.add(new AssetDescriptor<>(file.pathWithoutExtension() + ".atlas", TextureAtlas.class));
        else if (parameter.textureAtlasPath != null) deps.add(new AssetDescriptor<>(parameter.textureAtlasPath, TextureAtlas.class));
        return deps;
    }

    @Override
    public void loadAsync (AssetManager manager, String fileName, FileHandle file, SkinParameter parameter) {
    }

    @Override
    public FreeTypeSkin loadSync (AssetManager manager, String fileName, FileHandle file, SkinParameter parameter) {
        String textureAtlasPath = file.pathWithoutExtension() + ".atlas";
        ObjectMap<String, Object> resources = null;
        if (parameter != null) {
            if (parameter.textureAtlasPath != null){
                textureAtlasPath = parameter.textureAtlasPath;
            }
            if (parameter.resources != null){
                resources = parameter.resources;
            }
        }
        TextureAtlas atlas = manager.get(textureAtlasPath, TextureAtlas.class);
        FreeTypeSkin skin = new FreeTypeSkin(atlas);
        if (resources != null) {
            for (ObjectMap.Entry<String, Object> entry : resources.entries()) {
                skin.add(entry.key, entry.value);
            }
        }
        skin.load(file);
        return skin;
    }

    static public class SkinParameter extends AssetLoaderParameters<FreeTypeSkin> {
        public final String textureAtlasPath;
        public final ObjectMap<String, Object> resources;

        public SkinParameter () {
            this(null, null);
        }

        public SkinParameter(ObjectMap<String, Object> resources){
            this(null, resources);
        }

        public SkinParameter (String textureAtlasPath) {
            this(textureAtlasPath, null);
        }

        public SkinParameter (String textureAtlasPath, ObjectMap<String, Object> resources) {
            this.textureAtlasPath = textureAtlasPath;
            this.resources = resources;
        }
    }
}
