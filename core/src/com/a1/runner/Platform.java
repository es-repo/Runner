package com.a1.runner;

public class Platform extends ComposedFigure {

    public static int blockWidth = 16;
    public static int blockHeight = 16;
    public static int maxBlocksCount = 12;
    public static int minBlocksCount = 3;
    public static float maxDistance = blockWidth * 8;
    public static float minDistance = blockWidth * 3;

    private int level;

    private GameAssets assets;

    private Tile top;
    private Sprite topLeft;
    private Sprite topRight;

    private Tile bottom;
    private Tile bottomLeft;
    private Tile bottomRight;

    public Platform(GameAssets assets, int level){

        this.assets = assets;
        this.level = level;

        topLeft = new Sprite();
        topLeft.boundingBox.width = blockWidth;
        topLeft.boundingBox.height = blockHeight;

        top = new Tile(blockWidth, blockHeight);
        top.boundingBox.height = blockHeight;
        top.boundingBox.x = topLeft.boundingBox.width;

        topRight = new Sprite();
        topRight.boundingBox.width = blockWidth;
        topRight.boundingBox.height = blockHeight;

        if (level >=0 ) {
            float bottomTileHeight = 32;
            int v = 5 - level;
            float by = -2* top.boundingBox.height;
            bottomLeft = new Tile(blockWidth, bottomTileHeight);
            bottomLeft.setCountV(v);
            bottomLeft.boundingBox.y = by;

            bottom = new Tile(blockWidth, bottomTileHeight);
            bottom.setCountV(v);
            bottom.boundingBox.y = by;
            bottom.boundingBox.x = bottomLeft.boundingBox.width;

            bottomRight = new Tile(blockWidth, bottomTileHeight);
            bottomRight.setCountV(v);
            bottomRight.boundingBox.y = by;

            figures.add(bottomLeft);
            figures.add(bottom);
            figures.add(bottomRight);
        }

        figures.add(topLeft);
        figures.add(topRight);
        figures.add(top);

        setBlocksCount(minBlocksCount);
        boundingBox.height = top.boundingBox.height;
        setKind(0);

        for (int i = 0; i < figures.size(); i++){
            ((Sprite)figures.get(i)).brightness = 1 - (3 - level)*0.15f;
        }
    }

    public void setBlocksCount(int v){
        top.setCountH(v - 2);
        topRight.boundingBox.x = top.boundingBox.x + top.boundingBox.width;

        if (bottom != null) {
            bottom.setCountH(v - 2);
            bottomRight.boundingBox.x = bottom.boundingBox.x + bottom.boundingBox.width;
        }

        boundingBox.width = top.boundingBox.width + topLeft.boundingBox.width + topRight.boundingBox.width;
    }

    public void setKind(int kind){
        String id = "platform." + String.valueOf(kind) + ".";

        top.texture = assets.textures.get(id + "top");
        topLeft.texture = assets.textures.get(id + "top_left");
        topRight.texture = assets.textures.get(id + "top_right");

        if (bottom != null){
            bottomLeft.texture = assets.textures.get(id + "bottom_left");
            bottom.texture = assets.textures.get(id + "bottom");
            bottomRight.texture = assets.textures.get(id + "bottom_right");

            int dy = kind < 3 ? 2 : 0;
            float by = -2* top.boundingBox.height + dy;
            bottomLeft.boundingBox.y = by;
            bottom.boundingBox.y = by;
            bottomRight.boundingBox.y = by;
        }
    }
}