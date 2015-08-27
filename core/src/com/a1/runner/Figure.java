package com.a1.runner;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class Figure {

    private EventHandler clickHandler;

    public String id;
    public Rectangle boundingBox = new Rectangle();
    public Vector3 velocity = Vector3.Zero;
    public Color color = Color.WHITE;
    public boolean isVisible = true;

    public void setClickHandler(EventHandler handler){
        clickHandler = handler;
    }

    public void tick(long ticks){
    }

    public boolean click(){
        if (clickHandler != null){
            clickHandler.action(0);
            return true;
        }
        return false;
    }
}
