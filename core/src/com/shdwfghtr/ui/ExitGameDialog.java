package com.shdwfghtr.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.shdwfghtr.explore.GdxGame;

public class ExitGameDialog extends Dialog {
    public static final float WIDTH = 256, HEIGHT = 144;
    public static final String TITLE = "Exit Game?";

    public ExitGameDialog() {
        super(TITLE, GdxGame.uiService.getSkin());
        text("Are you sure you want \n to exit the game?")
                .button("Yes", 0)
                .button("No", 1);
        setSize(WIDTH, HEIGHT);
    }

    @Override
    public Dialog show(Stage stage) {
        setPosition((stage.getWidth() - getWidth()) / 2, (stage.getHeight() - getHeight()) / 2);
        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        if(object.equals(0))
            Gdx.app.exit();
        remove();
    }
}
