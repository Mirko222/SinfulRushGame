package com.pizzaroof.sinfulrush.util.assets;


import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.HashMap;

public class VisualList<T> extends List<T> {

    protected HashMap<T, Sprite> images = new HashMap<>();

    protected int alignment;

    public VisualList(Skin skin) {
        super(skin);
    }

    public VisualList(Skin skin, String styleName) {
        super(skin, styleName);
    }

    public VisualList(ListStyle style) {
        super(style);
    }

    @Override
    public void setAlignment(int alignment) {
        super.setAlignment(alignment);
        this.alignment = alignment;
    }

    @Override
    protected GlyphLayout drawItem (Batch batch, BitmapFont font, int index, T item, float x, float y, float width) {
        if(images.containsKey(item)) {
            Sprite sprite = images.get(item);
            sprite.setPosition(x, y - sprite.getHeight() * sprite.getScaleY() * 0.7f);
            sprite.setAlpha(font.getColor().a);
            sprite.draw(batch);
        }

        String string = toString(item);
        return font.draw(batch, string, x, y, 0, string.length(), width, alignment, false, "...");
    }

    public void addImage(T item, TextureRegion img, float w, float h) {
        Sprite tmp = new Sprite(img);
        tmp.setOrigin(0, 0);
        tmp.setScale(w / tmp.getWidth(), h / tmp.getHeight());
        images.put(item, tmp);
    }
}
