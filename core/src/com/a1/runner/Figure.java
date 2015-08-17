package com.a1.runner;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class Figure {
    public String id;
    public Rectangle boundingBox = new Rectangle();
    public Vector3 velocity = Vector3.Zero;
    public Color color = Color.WHITE;
    public boolean isVisisble = true;
}
