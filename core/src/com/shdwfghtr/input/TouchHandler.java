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
import com.shdwfghtr.explore.Asset;

import java.util.HashMap;

public class TouchHandler extends InputHandler {
    private static final Button ARROW_RIGHT = new Button(Asset.getSkin()), ARROW_UP = new Button(Asset.getSkin()),
            ARROW_LEFT = new Button(Asset.getSkin()),	ARROW_DOWN = new Button(Asset.getSkin()),
            CIRCLE_DOWN = new Button(Asset.getSkin()), CIRCLE_RIGHT = new Button(Asset.getSkin()),
            CIRCLE_LEFT = new Button(Asset.getSkin()), CIRCLE_UP = new Button(Asset.getSkin()),
            BAR_RIGHT = new Button(Asset.getSkin()), BAR_LEFT = new Button(Asset.getSkin());
    private static final Touchpad TOUCHPAD = new Touchpad(0, Asset.getSkin());

    private final HashMap<Integer, String> pointers = new HashMap<Integer, String>(5);
    private final Table table = new Table();

    public static final Array<Button> BUTTONS = new Array<Button>(new Button[]{ARROW_UP, ARROW_DOWN, ARROW_LEFT, ARROW_RIGHT, CIRCLE_UP, CIRCLE_DOWN, CIRCLE_LEFT, CIRCLE_RIGHT, BAR_LEFT, BAR_RIGHT});

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        String name = event.getTarget().getName();
        if(name != null && Asset.CONTROLS.contains(name)) {
            pointers.put(pointer, name);
            return inputDown(event, Asset.CONTROLS.getInteger(name));
        }
        return false;
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if(pointers.containsKey(pointer)) {
            inputUp(event, Asset.CONTROLS.getInteger(pointers.get(pointer)));
            pointers.remove(pointer);
        }
    }

    @Override
    public boolean isInputDown(String input) {
        float sensitivity = Asset.CONTROLS.getFloat("touch_sensitivity", 0.5f);
        if (input.matches("left"))
            if(Asset.CONTROLS.getBoolean("use_axis"))
                return TOUCHPAD.getKnobPercentX() < -sensitivity;
        if (input.matches("right"))
            if(Asset.CONTROLS.getBoolean("use_axis"))
                return TOUCHPAD.getKnobPercentX() > sensitivity;
        if(input.matches("down"))
            if(Asset.CONTROLS.getBoolean("use_axis"))
                return TOUCHPAD.getKnobPercentY() < -sensitivity;
        if(input.matches("up"))
            if(Asset.CONTROLS.getBoolean("use_axis"))
                return TOUCHPAD.getKnobPercentY() > sensitivity;

        return pointers.containsValue(input);
    }

    @Override
    public void toStage(Stage stage) {
        float size = Asset.CONTROLS.getFloat("touch_size", 64f),
                padding = Asset.CONTROLS.getFloat("touch_padding", 1f),
                border = Asset.CONTROLS.getFloat("touch_border", 5f),
                position = Asset.CONTROLS.getFloat("touch_position", 5f);

        table.setBounds(0, 0, stage.getWidth(), (size + 2 * padding) * 3);

        for(int i=0; i<BUTTONS.size; i++) {
            BUTTONS.get(i).clearListeners();
            BUTTONS.get(i).addAction(Actions.alpha(0.5f));

            //setting the button's input value to be that of the controller mapping
            BUTTONS.get(Asset.CONTROLS.getInteger(Asset.INPUT_LIST[i])).setName(Asset.INPUT_LIST[i]);
            stage.addActor(BUTTONS.get(i));
        }

        TOUCHPAD.setSize(size * 2, size * 2);
        TOUCHPAD.addAction(Actions.alpha(0.5f));
        TOUCHPAD.setName("touchpad");
        TOUCHPAD.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String name = "";
                float sensitivity = Asset.CONTROLS.getFloat("touch_sensitivity", 0.3f);
                if (TOUCHPAD.getKnobPercentX() < -sensitivity)
                    inputDown(new InputEvent(), Asset.CONTROLS.getInteger("left"));
                if (TOUCHPAD.getKnobPercentX() > sensitivity)
                    inputDown(new InputEvent(), Asset.CONTROLS.getInteger("right"));
                if (TOUCHPAD.getKnobPercentY() < -sensitivity)
                    inputDown(new InputEvent(), Asset.CONTROLS.getInteger("down"));
                if (TOUCHPAD.getKnobPercentY() > sensitivity)
                    inputDown(new InputEvent(), Asset.CONTROLS.getInteger("up"));
                if (Math.abs(TOUCHPAD.getKnobPercentX()) < sensitivity) {
                    inputUp(new InputEvent(), Asset.CONTROLS.getInteger("left"));
                    inputUp(new InputEvent(), Asset.CONTROLS.getInteger("right"));
                }
                if (Math.abs(TOUCHPAD.getKnobPercentY()) < sensitivity) {
                    inputUp(new InputEvent(), Asset.CONTROLS.getInteger("up"));
                    inputUp(new InputEvent(), Asset.CONTROLS.getInteger("down"));
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

        if(Asset.CONTROLS.getBoolean("use_axis"))
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
