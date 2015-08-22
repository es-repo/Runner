package com.a1.runner;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Renderer {

    private SpriteBatch batch;

    public Renderer(SpriteBatch batch){
        this.batch = batch;
    }

    public void drawScene(Scene scene){
        for (Figure f : scene.figures)
            drawFigure(f);
    }

    public void drawFigure(Figure f){

        if (!f.isVisible)
            return;

        if (f instanceof ComposedFigure){
            for (Figure pf : ((ComposedFigure) f).figures)
                drawFigure(pf);
        }
        else if (f instanceof Tile)
            drawTile((Tile) f);
        else if (f instanceof Sprite)
            drawSprite((Sprite)f);
        else if (f instanceof TextFigure)
            drawTextFigure((TextFigure)f);
    }

    public void drawSprite(Sprite s){
        batch.draw(s.texture, s.boundingBox.x, s.boundingBox.y, s.boundingBox.width, s.boundingBox.height);
    }

    public void drawTile(Tile t){
        float tileWidth = t.get_tileWidth();
        for (double k = 0, x = t.boundingBox.x; k <  t.countH; k++, x += tileWidth) {
            batch.draw(t.texture, (int) x, t.boundingBox.y, tileWidth, t.boundingBox.height);
        }
    }

    public void drawTextFigure(TextFigure f){
        f.font.draw(batch, f.text, f.boundingBox.x, f.boundingBox.y + f.boundingBox.height);
    }
}
