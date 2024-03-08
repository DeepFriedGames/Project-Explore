package com.shdwfghtr.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.asset.UserInterfaceService;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.screens.TravelScreen;

public class LeavePlanetDialog extends Dialog {
    public static final String TITLE = "Leave Planet?";
    private final UserInterfaceService uiService;

    public LeavePlanetDialog(UserInterfaceService uiService) {
        super(TITLE, GdxGame.uiService.getSkin());
        this.uiService = uiService;

        this.text("Are you sure you want\nto leave this Planet?")
                .button("Yes", 1)
                .button("No", 0);
    }

    @Override
    public Dialog show(Stage stage) {
        stage.addActor(this);
        this.toFront();

        return this;
    }

    @Override
    protected void result(Object object) {
        if (object.equals(1)) {
            uiService.getCurtain().setBounds(0, 0
                    , uiService.getStage().getWidth(), uiService.getStage().getHeight());
            uiService.getStage().addActor(uiService.getCurtain());
            uiService.getCurtain().addAction(Actions.fadeIn(1.0f));
            TimeService.addTimer(new TimeService.Timer(1.0f) {
                @Override
                public boolean onCompletion() {
                    ((GdxGame) Gdx.app.getApplicationListener()).setScreen(new TravelScreen());
                    return true;
                }
            });
        }
        remove();
    }
}
