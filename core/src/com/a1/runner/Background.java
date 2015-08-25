package com.a1.runner;

public class Background extends Tile {

    private static final float tileWidth = 60;

    public float scrollSpeed = 0.25f;

    public Background(GameAssets assets, int width, int height, String textureName){
        super(tileWidth, height);
        texture = assets.textures.get(textureName);
        setCountH((int)(width / tileWidth) + 2);
    }

    @Override
    public void tick(long ticks){
        scroll();
    }

    public void scroll(){
        boundingBox.x -= scrollSpeed;
        if (boundingBox.x <= -tileWidth)
            boundingBox.x = 0;
    }
}
