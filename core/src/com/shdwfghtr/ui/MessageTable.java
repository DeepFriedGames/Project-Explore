package com.shdwfghtr.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.shdwfghtr.asset.PaletteService;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.explore.GdxGame;

public class MessageTable extends Table {
    private static final TextureRegion msgBox = GdxGame.textureAtlasService.findUIRegion("message_box");
    private static final float messageDuration = 3.0f;
    private final Array<String> messages = new Array<>();
    private final MessageTimer msgTimer = new MessageTimer();

    public MessageTable(Skin skin) {
        super(skin);
        addAction(new MessageAction());
        setBackground(new TextureRegionDrawable(new TextureRegion(msgBox)));
        setTouchable(Touchable.disabled);
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if(stage != null) {
            setBounds(stage.getWidth() / 3, stage.getHeight()
                    , stage.getWidth() / 3, HUDTable.HEIGHT);
        }
    }

    private class MessageAction extends Action {
        @Override
        public boolean act(float delta) {
            if(messages.size > 0 && msgTimer.isComplete()) {
                actor.addAction(Actions.moveBy(0, - HUDTable.HEIGHT, 0.5f));
                ((Table) actor).add(new Label(messages.first(), getSkin(), "font", PaletteService.getPalette("ui")[17]));
                msgTimer.reset();
            }
            return true;
        }
    }

    private class MessageTimer extends TimeService.Timer{
        public MessageTimer() {
            super(messageDuration);
        }

        @Override
        public boolean onCompletion() {
            clearChildren();
            messages.removeIndex(0);
            addAction(Actions.moveBy(0, HUDTable.HEIGHT, 0.5f));
            return true;
        }
    }

}
