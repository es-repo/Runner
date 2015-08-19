package com.a1.runner;


import com.badlogic.gdx.graphics.Texture;

public class Button extends Sprite {

    private Texture releasedTexture;
    private Texture pressedTexture;
    private int releaseButtonAfterTicks;

    public Button(Texture releasedTexture, Texture pressedTexture){
        this.releasedTexture = releasedTexture;
        this.pressedTexture = pressedTexture;
        texture = releasedTexture;
    }

    @Override
    public void tick(long ticks){
        if (releaseButtonAfterTicks >= 0)
            releaseButtonAfterTicks--;

        if (releaseButtonAfterTicks == 0){
            texture = releasedTexture;
            super.click();
        }
    }

    @Override
    public boolean click(){
        texture = pressedTexture;
        releaseButtonAfterTicks = 10;
        return true;
    }
}
