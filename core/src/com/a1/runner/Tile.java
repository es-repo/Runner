package com.a1.runner;

public class Tile extends Sprite {
    public int countH = 1;
    public float get_tileWidth(){
        return boundingBox.width / countH;
    }
}
