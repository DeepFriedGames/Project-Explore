package com.shdwfghtr.input;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.shdwfghtr.asset.ControllerService;
import com.shdwfghtr.explore.GdxGame;

import java.util.HashMap;

public class TouchHandler extends InputHandler {
    private static final Button ARROW_RIGHT = GdxGame.uiService.createButton(), ARROW_UP = GdxGame.uiService.createButton(),
            ARROW_LEFT = GdxGame.uiService.createButton(),	ARROW_DOWN = GdxGame.uiService.createButton(),
            CIRCLE_DOWN = GdxGame.uiService.createButton(), CIRCLE_RIGHT = GdxGame.uiService.createButton(),
            CIRCLE_LEFT = GdxGame.uiService.createButton(), CIRCLE_UP = GdxGame.uiService.createButton(),
            BAR_RIGHT = GdxGame.uiService.createButton(), BAR_LEFT = GdxGame.uiService.createButton();
    private static final Touchpad TOUCHPAD = GdxGame.uiService.createTouchPad();

    private final HashMap<Integer, String> pointers = new HashMap<Integer, String>(5);
    private final Table table = new Table();

    public static final Array<Button> BUTTONS = new Array<>(new Button[]{ARROW_UP, ARROW_DOWN, ARROW_LEFT, ARROW_RIGHT, CIRCLE_UP, CIRCLE_DOWN, CIRCLE_LEFT, CIRCLE_RIGHT, BAR_LEFT, BAR_RIGHT});

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        String input = event.getTarget().getName();
        if(input != null && ControllerService.hasInput(input)) {
            pointers.put(pointer, input);
            return inputDown(event, ControllerService.getInput(input));
        }
        return false;
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if(pointers.containsKey(pointer)) {
            inputUp(event, ControllerService.getInput(pointers.get(pointer)));
            pointers.remove(pointer);
        }
    }

    @Override
    public boolean isInputDown(String input) {
        float sensitivity = ControllerService.getTouchSensitivity();
        if(ControllerService.isUsingAxis()){
            if (input.matches("left"))
                    return TOUCHPAD.getKnobPercentX() < -sensitivity;
            if (input.matches("right"))
                    return TOUCHPAD.getKnobPercentX() > sensitivity;
            if(input.matches("down"))
                    return TOUCHPAD.getKnobPercentY() < -sensitivity;
            if(input.matches("up"))
                    return TOUCHPAD.getKnobPercentY() > sensitivity;
        }

        return pointers.containsValue(input);
    }

    @Override
    public void toStage(Stage stage) {
        float size = ControllerService.getTouchSize(),
                padding = ControllerService.getTouchPadding(),
                border = ControllerService.getTouchBorder(),
                position = ControllerService.getTouchPosition();

        table.setBounds(0, 0, stage.getWidth(), (size + 2 * padding) * 3);

        for(int i=0; i<BUTTONS.size; i++) {
            BUTTONS.get(i).clearListeners();
            BUTTONS.get(i).addAction(Actions.alpha(0.5f));
            String input = ControllerService.INPUT_LIST[i];

            //setting the button's input value to be that of the controller mapping
            BUTTONS.get(ControllerService.getInput(input)).setName(input);
            stage.addActor(BUTTONS.get(i));
        }

        TOUCHPAD.setSize(size * 2, size * 2);
        TOUCHPAD.addAction(Actions.alpha(0.5f));
        TOUCHPAD.setName("touchpad");
        TOUCHPAD.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String name = "";
                float sensitivity = ControllerService.getTouchSensitivity();
                if (TOUCHPAD.getKnobPercentX() < -sensitivity)
                    inputDown(new InputEvent(), ControllerService.getInput("left"));
                if (TOUCHPAD.getKnobPercentX() > sensitivity)
                    inputDown(new InputEvent(), ControllerService.getInput("right"));
                if (TOUCHPAD.getKnobPercentY() < -sensitivity)
                    inputDown(new InputEvent(), ControllerService.getInput("down"));
                if (TOUCHPAD.getKnobPercentY() > sensitivity)
                    inputDown(new InputEvent(), ControllerService.getInput("up"));
                if (Math.abs(TOUCHPAD.getKnobPercentX()) < sensitivity) {
                    inputUp(new InputEvent(), ControllerService.getInput("left"));
                    inputUp(new InputEvent(), ControllerService.getInput("right"));
                }
                if (Math.abs(TOUCHPAD.getKnobPercentY()) < sensitivity) {
                    inputUp(new InputEvent(), ControllerService.getInput("up"));
                    inputUp(new InputEvent(), ControllerService.getInput("down"));
                }
            }
        });

        Table circleTable = new Table();
        circleTable.add(CIRCLE_UP).size(size).pad(padding).center().colspan(2).row();
        circleTable.add(CIRCLE_LEFT).size(size).pad(padding);
        circleTable.add(CIRCLE_RIGHT).size(size).pad(padding).row();
        circleTable.add(CIRCLE_DOWN).size(size).pad(padding).center().colspan(2);

        Table dPadTable = new Table();
        dPadTable.add(ARROW_UP).size(size).pad(padding).center().colspan(2).row();
        dPadTable.add(ARROW_LEFT).size(size).pad(padding);
        dPadTable.add(ARROW_RIGHT).size(size).pad(padding).row();
        dPadTable.add(ARROW_DOWN).size(size).pad(padding).center().colspan(2);

        if(ControllerService.isUsingAxis())
            table.add(TOUCHPAD).size(size * 2).pad(padding).center();
        else
            table.add(dPadTable);

        table.add(BAR_LEFT).size(size).pad(padding).expandX().center().bottom();
        table.add(BAR_RIGHT).size(size).pad(padding).expandX().center().bottom();
        table.add(circleTable);

        table.padRight(border);
        table.padLeft(border);
        table.padBottom(position);

        stage.addActor(table);
        super.toStage(stage);

    }
}
