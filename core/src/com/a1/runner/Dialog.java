package com.a1.runner;

public class Dialog extends ComposedFigure {

    private EventHandler closeHandler;
    protected ComposedFigure frame;

    public Dialog(GameAssets assets,
                  int viewportWidth, int viewportHeight,
                  String frameTextureName, int frameWidth, int frameHeight){
        close();
        Sprite blackMask = new Sprite();
        blackMask.texture = assets.textures.get("blackmask");
        blackMask.boundingBox.width = viewportWidth;
        blackMask.boundingBox.height = viewportHeight;
        blackMask.setClickHandler(new EventHandler() {
            @Override
            public void action(int value) {
            }
        });
        figures.add(blackMask);

        frame = new ComposedFigure();
        frame.boundingBox.width = frameWidth;
        frame.boundingBox.height = frameHeight;
        frame.boundingBox.x = (viewportWidth - frame.boundingBox.width) / 2;
        frame.boundingBox.y = (viewportHeight - frame.boundingBox.height) / 2;

        Sprite frameTexture = new Sprite();
        frameTexture.texture = assets.textures.get(frameTextureName);
        frameTexture.boundingBox.width = frameWidth;
        frameTexture.boundingBox.height = frameHeight;
        frame.figures.add(frameTexture);

        figures.add(frame);
    }

    public void setCloseHandler(EventHandler handler){
        closeHandler = handler;
    }

    public void show(){
        isVisible = true;
    }

    public void close(){
        close(0);
    }

    void close(int value){
        isVisible = false;
        if (closeHandler != null)
            closeHandler.action(value);
    }
}
