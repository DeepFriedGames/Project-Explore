package com.shdwfghtr.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.shdwfghtr.explore.Asset;

public class KeyboardHandler extends InputHandler {

    @Override
    public boolean isInputDown(String input) {
        try {
            return Gdx.input.isKeyPressed(Asset.CONTROLS.getInteger(input));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

	@Override
	public boolean keyDown(InputEvent event, int keycode) {		
		return super.inputDown(event, keycode);
	}

	@Override
	public boolean keyUp(InputEvent event, int keycode) {
		return super.inputUp(event, keycode);
	}

}
