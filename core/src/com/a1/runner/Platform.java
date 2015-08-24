package com.a1.runner;

public class Platform extends ComposedFigure {

    public static int blockWidth = 16;
    public static int blockHeight = 16;
    public static int maxBlocksCount = 12;
    public static int minBlocksCount = 3;
    public static float maxDistance = blockWidth * 8;
    public static float minDistance = blockWidth * 3;

    private Tile top;
    private Sprite topLeft;
    private Sprite topRight;
    private Tile bottom;

    public Platform(GameAssets assets, int level){

        topLeft = new Sprite();
        topLeft.boundingBox.width = blockWidth;
        topLeft.boundingBox.height = blockHeight;

        top = new Tile(blockWidth, blockHeight);
        top.boundingBox.height = blockHeight;
        top.boundingBox.x = topLeft.boundingBox.width;

        topRight = new Sprite();
        topRight.boundingBox.width = blockWidth;
        topRight.boundingBox.height = blockHeight;

        if (level < 0) {
            top.texture = assets.textures.get("platform.2.top");
            topLeft.texture = assets.textures.get("platform.2.top_left");
            topRight.texture = assets.textures.get("platform.2.top_right");
        }
        else{
            top.texture = assets.textures.get("platform.1.topl" + String.valueOf(level));
        }

        figures.add(top);
        figures.add(topLeft);
        figures.add(topRight);

        if (level >=0 ) {
            float bottomTileHeight = 32;
            bottom = new Tile(blockWidth, bottomTileHeight);
            bottom.texture = assets.textures.get("platform.1.bottoml" + String.valueOf(level));
            bottom.boundingBox.y = - bottomTileHeight;
            bottom.setCountV(5 - level);
            figures.add(bottom);
        }

        setBlocksCount(minBlocksCount);
        boundingBox.height = top.boundingBox.height;
    }

    public void setBlocksCount(int v){
        top.setCountH(v - 2);
        topRight.boundingBox.x = top.boundingBox.x + top.boundingBox.width;
        if (bottom != null)
            bottom.setCountH(v - 2);

        boundingBox.width = top.boundingBox.width + topLeft.boundingBox.width + topRight.boundingBox.width;
    }
}
