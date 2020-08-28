package com.pizzaroof.sinfulrush.util.assets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class FreeTypeSkin extends Skin {

    public FreeTypeSkin(TextureAtlas atlas) {
        super(atlas);
    }

    //Override json loader to process FreeType fonts from skin JSON
    @Override
    protected Json getJsonLoader(final FileHandle skinFile) {
        Json json = super.getJsonLoader(skinFile);
        final Skin skin = this;

        json.setSerializer(FreeTypeFontGenerator.class, new Json.ReadOnlySerializer<FreeTypeFontGenerator>() {
            @Override
            public FreeTypeFontGenerator read(Json json, JsonValue jsonData, Class type) {
                String path = json.readValue("font", String.class, jsonData);
                jsonData.remove("font");

                FreeTypeFontGenerator.Hinting hinting = FreeTypeFontGenerator.Hinting.valueOf(json.readValue("hinting",
                        String.class, "AutoMedium", jsonData));
                jsonData.remove("hinting");

                Texture.TextureFilter minFilter = Texture.TextureFilter.valueOf(
                        json.readValue("minFilter", String.class, "Nearest", jsonData));
                jsonData.remove("minFilter");

                Texture.TextureFilter magFilter = Texture.TextureFilter.valueOf(
                        json.readValue("magFilter", String.class, "Nearest", jsonData));
                jsonData.remove("magFilter");

                FreeTypeFontGenerator.FreeTypeFontParameter parameter = json.readValue(FreeTypeFontGenerator.FreeTypeFontParameter.class, jsonData);
                parameter.characters += "ĂăȘșȚțÂâ∞";
                parameter.hinting = hinting;
                parameter.minFilter = minFilter;
                parameter.magFilter = magFilter;

                /*float ratio = (float)Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth();
                float targetRatio = (float)Constants.VIRTUAL_HEIGHT / (float)Constants.VIRTUAL_WIDTH;
                float targetValue, realValue;
                if(ratio < targetRatio) { //più largo di quanto vorremmo
                    realValue = Gdx.graphics.getWidth();
                    targetValue = Constants.VIRTUAL_WIDTH;
                } else { //più alto di quanto vorremmo
                    realValue = Gdx.graphics.getHeight();
                    targetValue = Constants.VIRTUAL_HEIGHT;
                }*/

                //(int)Math.ceil((parameter.size * realValue) / targetValue);

                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(skinFile.parent().child(path));
                BitmapFont font = generator.generateFont(parameter);
                font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                font.getData().markupEnabled = true;

                skin.add(jsonData.name, font);
                if (parameter.incremental) {
                    generator.dispose();
                    return null;
                } else {
                    return generator;
                }
            }
        });

        return json;
    }
}
