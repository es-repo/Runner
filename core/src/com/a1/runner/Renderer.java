package com.a1.runner;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Renderer {

    private SpriteBatch batch;
    private int viewportWidth;
    private int viewportHeight;

    public Renderer(SpriteBatch batch, int viewportWidth, int viewportHeight){
        this.batch = batch;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public void drawScene(Scene scene){
        int s = scene.figures.size();
        for (int i = 0; i < s; i++)
            drawFigure(scene.figures.get(i));
    }

    public void drawFigure(Figure f){
        drawFigure(f, f.boundingBox.x, f.boundingBox.y);
    }

    private void drawFigure(Figure f, float x, float y){
        if (!f.isVisible)
            return;

        if (f instanceof ComposedFigure){
            int s = ((ComposedFigure)f).figures.size();
            for (int i = 0; i < s; i++) {
                Figure pf = ((ComposedFigure)f).figures.get(i);
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
        drawTexture(s.texture, x, y, s.boundingBox.width, s.boundingBox.height);
    }

    private void drawTile(Tile t, float x, float y){
        float tileWidth = t.getTileWidth();
        float tileHeight = t.getTileHeight();
        int countV = t.getCountV();
        int countH = t.getCountH();
        for (float i = 0; i < countV; i++, y -= tileHeight) {
            for (float j = 0, xj = x; j < countH; j++, xj += tileWidth) {
                drawTexture(t.texture, xj, y, tileWidth, tileHeight);
            }
        }
    }

    private void drawTextFigure(TextFigure f, float x, float y){
        f.font.draw(batch, f.text, x, y + f.boundingBox.height);
    }

    private void drawTexture(Texture t, float x, float y, float width, float height){
        if (x + width < 0 || x >= viewportWidth || y + height < 0 || y >= viewportHeight)
            return;

        batch.draw(t, x, y, width, height);
    }
}
