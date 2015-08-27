package com.a1.runner;

public class QuitDialog extends Dialog {

    public QuitDialog(GameAssets assets, int viewportWidth, int viewportHeight){
        super(assets, viewportWidth, viewportHeight, "quit_dialog.frame", 200, 120);

        float buttonWidth = 87 * 0.9f;
        float buttonHeight = 30 * 0.9f;
        float y = 20;
        Button yes = new Button(assets.textures.get("quit_dialog.buttons.yes"), assets.textures.get("quit_dialog.buttons.yes_pressed"));
        yes.boundingBox.width = buttonWidth;
        yes.boundingBox.height = buttonHeight;
        yes.boundingBox.y = y;
        yes.boundingBox.x = (100 - yes.boundingBox.width) / 2;
        yes.setClickHandler(new EventHandler() {
            @Override
            public void action(int value) {
                close(1);
                isVisible = true;
            }
        });
        frame.figures.add(yes);

        Button no = new Button(assets.textures.get("quit_dialog.buttons.no"), assets.textures.get("quit_dialog.buttons.no_pressed"));
        no.boundingBox.width = buttonWidth;
        no.boundingBox.height = buttonHeight;
        no.boundingBox.y = y;
        no.boundingBox.x = 100 + (100 - no.boundingBox.width) / 2;
        no.setClickHandler(new EventHandler() {
            @Override
            public void action(int value) {
                close(0);
            }
        });
        frame.figures.add(no);
    }
}