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
    private Tile bottomLeft;
    private Tile bottomRight;

    public Platform(GameAssets assets, int kind, int level){

//        topLeft = new Sprite();
//        topLeft.boundingBox.width = blockWidth;
//        topLeft.boundingBox.height = blockHeight;

        top = new Tile(blockWidth, blockHeight);
        top.boundingBox.height = blockHeight;
        top.boundingBox.x = 0;//topLeft.boundingBox.width;

//        topRight = new Sprite();
//        topRight.boundingBox.width = blockWidth;
//        topRight.boundingBox.height = blockHeight;

        String id = "platform." + String.valueOf(kind) + "." + String.valueOf(level) + ".";

        top.texture = assets.textures.get(id + "top");
        //topLeft.texture = assets.textures.get(path + "top_left" + suf);
        //topRight.texture = assets.textures.get(path + "top_right" + suf);


        //figures.add(topLeft);
        //figures.add(topRight);

        if (level >=0 ) {
            float bottomTileHeight = 32;
            int v = 5 - level;
            float by = -2* top.boundingBox.height + 2;
//            bottomLeft = new Tile(blockWidth, bottomTileHeight);
//            bottomLeft.texture = assets.textures.get(path + "bottom_left" + suf);
//            bottomLeft.setCountV(v);
//            bottomLeft.boundingBox.y = by;

            bottom = new Tile(blockWidth, bottomTileHeight);
            bottom.texture = assets.textures.get(id + "bottom");
            bottom.setCountV(v);
            bottom.boundingBox.y = by;
            bottom.boundingBox.x = 0;//bottomLeft.boundingBox.width;

//            bottomRight = new Tile(blockWidth, bottomTileHeight);
//            bottomRight.texture = assets.textures.get(path + "bottom_right" + suf);
//            bottomRight.setCountV(v);
//            bottomRight.boundingBox.y = by;

            //figures.add(bottomLeft);
            figures.add(bottom);
            //figures.add(bottomRight);
        }
        figures.add(top);

        setBlocksCount(minBlocksCount);
        boundingBox.height = top.boundingBox.height;
    }

    public void setBlocksCount(int v){
        top.setCountH(v);
//        topRight.boundingBox.x = top.boundingBox.x + top.boundingBox.width;

        if (bottom != null) {
            bottom.setCountH(v);
//            bottomRight.boundingBox.x = bottom.boundingBox.x + bottom.boundingBox.width;
        }

        boundingBox.width = top.boundingBox.width;// + topLeft.boundingBox.width + topRight.boundingBox.width;
    }
}
