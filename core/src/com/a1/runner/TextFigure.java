package com.a1.runner;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class TextFigure extends Figure {
    public String text;
    public BitmapFont font;

    private boolean blink;
    private int blinkInterval = 10;
    private int blinkCounter;
    private int counter;

    public TextFigure(BitmapFont font){
        this.font = font;
    }

    public void setBlinking(int blinkInterval, int blinkCounts){
        blink = true;
        this.blinkInterval = blinkInterval;
        blinkCounter = blinkCounts;
        counter = blinkInterval;
    }

    public void setText(String text, GlyphLayout gl){
        this.text = text;
        gl.setText(font, text);
        boundingBox.width = gl.width;
        boundingBox.height = gl.height;
    }

    public void centerAlign(int viewportWidth, int viewportHeight, boolean h, boolean v){
        if (h){
            boundingBox.x = (viewportWidth - boundingBox.width) / 2;
        }
        if (v){
            boundingBox.x = (viewportHeight + boundingBox.height) / 2;
        }
    }

    @Override
    public void tick(long ticks){

        if (blink) {
            if (blinkCounter > 0) {
                counter--;
                isVisible = counter >= 0;
                if (counter < -blinkInterval) {
                    counter = blinkInterval;
                    blinkCounter--;
                }
            }
        }
    }
}
