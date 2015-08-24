package com.a1.runner;

public class Background extends Tile {

    private static final float tileWidth = 60;

    public float scrollSpeed = 0.5f;

    public Background(GameAssets assets, int viewportWidth, int viewportHeight){
        super(tileWidth, viewportHeight);
        texture = assets.textures.get("background.main");
        setCountH((int)(viewportWidth / tileWidth) + 2);
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
