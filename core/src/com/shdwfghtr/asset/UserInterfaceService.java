package com.shdwfghtr.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;
import com.shdwfghtr.ui.MessageTable;

public class UserInterfaceService implements Disposable {
    private final Viewport viewport;
    private final Stage stage;
    private final Skin uiSkin;
    private final CursorData cursor = new CursorData();
    private final Image uiCurtain = new Image(new Texture(1, 1, Pixmap.Format.RGB565));
    private final MessageTable messageTable;

    public UserInterfaceService() {
        this.viewport = new ScreenViewport();
        this.stage = new Stage(this.viewport);
        Gdx.input.setInputProcessor(stage);

        this.uiSkin = new Skin(Gdx.files.internal("skin/neutralizer-ui.json"));
        if(!VisUI.isLoaded()) VisUI.load(uiSkin);

        messageTable = new MessageTable(uiSkin);
        stage.addActor(messageTable);
        stage.setDebugAll(true); //TODO remove this
    }

    public void update(float delta) {
        stage.act(delta);

        //changes the cursor if it is over a touchable interface
        cursor.position.set(Gdx.input.getX(), Gdx.input.getY());
        cursor.position.set(stage.screenToStageCoordinates(cursor.position));
        Actor hit = stage.hit(cursor.position.x, cursor.position.y, true);
        if (hit != null && hit.isTouchable() && hit.isVisible())
            cursor.updateCursor("hand");
        else
            cursor.updateCursor("arrow");
    }

    public void draw() {
        viewport.apply();
        stage.draw();
        stage.getBatch().setColor(Color.WHITE);
    }

    public void addMessage(String message) {
        messageTable.addMessage(message);
    }

    public Stage getStage() {
        return stage;
    }

    public Skin getSkin() {
        return uiSkin;
    }

    public Image getCurtain() {
        return uiCurtain;
    }

    public BitmapFont getBodyFont() {
        return uiSkin.getFont("font");
    }

    public BitmapFont getHeaderFont() {
        return uiSkin.getFont("title");
    }

    public void fadeOutCurtain(float duration) {
        stage.addActor(uiCurtain);
        uiCurtain.addAction(Actions.fadeOut(duration));
    }

    public Button createButton() {
        return new Button(uiSkin);
    }

    public Touchpad createTouchPad() {
        return new Touchpad(0, uiSkin);
    }

    public void resize(int width, int height) {
        if(stage != null)
            stage.getViewport().update(width, height);
    }

    public void DropCurtain(float duration) {
        uiCurtain.setBounds(0, 0, stage.getWidth(), stage.getHeight());
        uiCurtain.addAction(Actions.alpha(1));
        uiCurtain.setName("Curtain");
        uiCurtain.setTouchable(Touchable.disabled);
        stage.addActor(uiCurtain);
        uiCurtain.addAction(Actions.fadeOut(duration));
        TimeService.addTimer(new TimeService.Timer(duration) {
            @Override
            public boolean onCompletion() {
                uiCurtain.remove();
                return true;
            }
        });
    }

    private class CursorData {
        String name = "";
        Vector2 position =  new Vector2();

        void updateCursor(String name){
            if(this.name.matches(name)) return;

            this.name = name;
            Cursor cursor = Gdx.graphics.newCursor(
                    new Pixmap(Gdx.files.internal("cursor/" + this.name + ".png")),
                    0, 0);
            Gdx.graphics.setCursor(cursor);
        }
    }

    @Override
    public void dispose() {
        uiSkin.dispose();
        stage.dispose();
    }

}
