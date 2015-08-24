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
        drawFigure(f, f.boundingBox.x, f.boundingBox.y);
    }

    private void drawFigure(Figure f, float x, float y){
        if (!f.isVisible)
            return;

        if (f instanceof ComposedFigure){
            for (Figure pf : ((ComposedFigure) f).figures) {
                drawFigure(pf, x + pf.boundingBox.x, y + pf.boundingBox.y);
            }
        }
        else if (f instanceof Tile)
            drawTile((Tile) f, x, y);
        else if (f instanceof Sprite)
            drawSprite((Sprite)f, x, y);
        else if (f instanceof TextFigure)
            drawTextFigure((TextFigure)f, x, y);
    }

    private void drawSprite(Sprite s, float x, float y){
        batch.draw(s.texture, x, y, s.boundingBox.width, s.boundingBox.height);
    }

    private void drawTile(Tile t, float x, float y){
        float tileWidth = t.getTileWidth();
        float tileHeight = t.getTileHeight();
        for (float i = 0; i < t.getCountV(); i++, y -= tileHeight) {
            for (float j = 0, xj = x; j < t.getCountH(); j++, xj += tileWidth) {
                batch.draw(t.texture, xj, y, tileWidth, tileHeight);
            }
        }
    }

    private void drawTextFigure(TextFigure f, float x, float y){
        f.font.draw(batch, f.text, x, y + f.boundingBox.height);
    }
}
