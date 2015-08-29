package com.a1.runner;

public class Tile extends Sprite {

    public float tileWidth;
    public float tileHeight;
    private int countH = 1;
    private int countV = 1;

    public Tile(float tileWidth, float tileHeight){
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.boundingBox.width = tileWidth;
        this.boundingBox.height = tileHeight;
    }

    public int getCountH(){ return countH; }
    public void setCountH(int v){
        countH = v;
        this.boundingBox.width = countH * tileWidth;
    }

    public int getCountV(){ return countV; }
    public void setCountV(int v){
        countV = v;
        this.boundingBox.height = countV * tileHeight;
    }
}
