package com.pizzaroof.sinfulrush.spawner.platform.pattern;

import com.badlogic.gdx.math.Vector2;

public class PlatformInfo {
    public String name;
    public int width, height;
    public Vector2 position;

    public PlatformInfo() {}

    public PlatformInfo(String name, int w, int h, float x, float y) {
        this.name = name;
        width = w;
        height = h;
        position = new Vector2(x, y);
    }
}