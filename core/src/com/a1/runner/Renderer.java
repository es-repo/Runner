package com.a1.runner;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Renderer {

    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private float fontSymbolWidth;
    private float fontSymbolHeight;
    private int viewportWidth;
    private int viewportHeight;

    public Renderer(SpriteBatch batch, BitmapFont font, int viewportWidth, int viewportHeight){
        this.batch = batch;
        this.font = font;
        this.glyphLayout = new GlyphLayout();
        this.glyphLayout.setText(font, "1");
        fontSymbolWidth = glyphLayout.width;
        fontSymbolHeight = glyphLayout.height;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public void drawText(String text, float x, float y, boolean alignH, boolean alignV, boolean alignR){
        drawText(text, x, y, alignH, alignV, alignR, 1);
    }

    public void drawText(String text, float x, float y, boolean alignH, boolean alignV, boolean alignR, float scale){

          float sx = 1, sy = 1;
            if (scale != 1) {
                sx = font.getScaleX();
                sy = font.getScaleX();
                font.getData().setScale(sx * scale, sy * scale);
            }

            float textWidth = text.length() * fontSymbolWidth * scale;
            float textHeight = fontSymbolHeight * scale;

            float posX = x;
            float posY = y;
            if (alignH){
                posX = (viewportWidth - x - textWidth) / 2;
            }

            if (alignV){
                posY = (viewportHeight - y - textHeight) / 2;
            }

            if (alignR){
                posX = x - textWidth;
        }

        glyphLayout.setText(font, text);
        float w = glyphLayout.width;
        font.draw(batch, glyphLayout, posX, posY);

        if (scale != 0){
            font.getData().setScale(sx, sy);
        }
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
        drawTexture(s.texture, x, y, s.boundingBox.width, s.boundingBox.height, s.brightness);
    }

    private void drawTile(Tile t, float x, float y){
        float tileWidth = t.getTileWidth();
        float tileHeight = t.getTileHeight();
        int countV = t.getCountV();
        int countH = t.getCountH();
        for (float i = 0; i < countV; i++, y -= tileHeight) {
            for (float j = 0, xj = x; j < countH; j++, xj += tileWidth) {
                drawTexture(t.texture, xj, y, tileWidth, tileHeight, t.brightness);
            }
        }
    }

    private void drawTextFigure(TextFigure f, float x, float y){
        f.font.draw(batch, f.text, x, y + f.boundingBox.height);
    }

    private void drawTexture(Texture t, float x, float y, float width, float height, float brightness){
        if (x + width < 0 || x >= viewportWidth || y + height < 0 || y >= viewportHeight)
            return;

        if (brightness != 1) {
            batch.setColor(brightness, brightness, brightness, 1f);
        }

        batch.draw(t, x, y, width, height);

        if (brightness != 1) {
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }
}
