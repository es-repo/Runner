package com.a1.runner;

public class Platform extends ComposedFigure {

    public static int blockWidth = 16;
    public static int blockHeight = 16;
    public static int maxBlocksCount = 12;
    public static int minBlocksCount = 3;
    public static float maxDistance = blockWidth * 8;
    public static float minDistance = blockWidth * 3;

    private Tile top;
    private Tile bottom;

    public  Platform(GameAssets assets, int level){
        top = new Tile(blockWidth, blockHeight);

        if (level < 0) {
            top.texture = assets.textures.get("platform.1.top");
        }
        else{
            top.texture = assets.textures.get("platform.1.topl" + String.valueOf(level));
        }

        top.boundingBox.height = blockHeight;
        top.setCountH(3);
        boundingBox.height = top.boundingBox.height;
        figures.add(top);

        if (level >=0 ) {
            float bottomTileHeight = 32;
            bottom = new Tile(blockWidth, bottomTileHeight);
            bottom.texture = assets.textures.get("platform.1.bottoml" + String.valueOf(level));
            bottom.boundingBox.y = - bottomTileHeight;
            bottom.setCountV(5 - level);
            figures.add(bottom);
        }

        setBlocksCount(minBlocksCount);
    }

    public void setBlocksCount(int v){
        for (Figure f : figures) {
            Tile t = (Tile)f;
            t.setCountH(v);
            boundingBox.width = t.boundingBox.width;
        }
    }
}
