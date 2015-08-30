package com.a1.runner;

public class Platform extends ComposedFigure {

    public static final int blockWidth = 16;
    public static final int blockHeight = 16;
    public static final int maxBlocksCount = 12;
    public static final int minBlocksCount = 3;
    public static final float maxDistance = blockWidth * 6.5f;
    public static final float minDistance = blockWidth * 3;

    private static final int kindsCount = 5;
    private static final int partsCount = 6;

    private GameAssets assets;

    private Tile top;
    private Sprite topLeft;
    private Sprite topRight;

    private Tile bottom;
    private Tile bottomLeft;
    private Tile bottomRight;

    private String[] textureIds;

    public Platform(GameAssets assets, int level){

        textureIds = new String[kindsCount * partsCount];
        for (int i = 0, k =0; i < textureIds.length; i+=partsCount, k++){
            textureIds[i] = "platform." + String.valueOf(k) + ".top";
            textureIds[i + 1] = "platform." + String.valueOf(k) + ".top_left";
            textureIds[i + 2] = "platform." + String.valueOf(k) + ".top_right";
            textureIds[i + 3] = "platform." + String.valueOf(k) + ".bottom";
            textureIds[i + 4] = "platform." + String.valueOf(k) + ".bottom_left";
            textureIds[i + 5] = "platform." + String.valueOf(k) + ".bottom_right";
        }

        this.assets = assets;

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
            int v = 6 - level;
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

        int i = partsCount * kind;
        top.texture = assets.textures.get(textureIds[i]);
        topLeft.texture = assets.textures.get(textureIds[i + 1]);
        topRight.texture = assets.textures.get(textureIds[i + 2]);

        if (bottom != null){
            bottom.texture = assets.textures.get(textureIds[i + 3]);
            bottomLeft.texture = assets.textures.get(textureIds[i + 4]);
            bottomRight.texture = assets.textures.get(textureIds[i + 5]);

            int dy = kind < 3 ? 2 : 0;
            float by = -2* top.boundingBox.height + dy;
            bottomLeft.boundingBox.y = by;
            bottom.boundingBox.y = by;
            bottomRight.boundingBox.y = by;
        }
    }
}
