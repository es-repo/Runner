package com.a1.runner;

import java.util.ArrayList;

public class ComposedFigure extends Figure {
    public ArrayList<Figure> figures = new ArrayList<Figure>();

    @Override
    public void tick(long ticks){
        super.tick(ticks);
        for (int i = 0; i < figures.size(); i++)
            figures.get(i).tick(ticks);
    }
}
