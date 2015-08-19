package com.a1.runner;

import java.util.ArrayList;

public class Scene {
    public ArrayList<Figure> figures = new ArrayList<Figure>();

    public void tick(long ticks){
        for (int i = 0; i < figures.size(); i++){
            figures.get(i).tick(ticks);
        }
    }
}
