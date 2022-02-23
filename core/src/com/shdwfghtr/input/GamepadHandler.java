package com.shdwfghtr.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.GameState;

public class GamepadHandler extends InputHandler implements ControllerListener {
	public static Controller GAMEPAD;

    @Override
    public boolean isInputDown(String input) {
        if (input.matches("left"))
            if(Asset.CONTROLS.getBoolean("use_axis"))
                return GAMEPAD.getAxis(Asset.CONTROLS.getInteger("x-axis")) == -1;
        if (input.matches("right"))
            if(Asset.CONTROLS.getBoolean("use_axis"))
                return GAMEPAD.getAxis(Asset.CONTROLS.getInteger("x-axis")) == 1;
        if(input.matches("down"))
            if(Asset.CONTROLS.getBoolean("use_axis"))
                return GAMEPAD.getAxis(Asset.CONTROLS.getInteger("y-axis")) == -1;
        if(input.matches("up"))
            if(Asset.CONTROLS.getBoolean("use_axis"))
                return GAMEPAD.getAxis(Asset.CONTROLS.getInteger("y-axis")) == 1;
        
        return GAMEPAD.getButton(Asset.CONTROLS.getInteger(input));
    }

	@Override
	public void act(float delta) {
		super.act(delta);
		if(player != null && gameScreen.getState() == GameState.PLAY) {
			if(GAMEPAD.getButton(Asset.CONTROLS.getInteger("left"))
					|| (Asset.CONTROLS.getBoolean("use_axis") && GAMEPAD.getAxis(Asset.CONTROLS.getInteger("x-axis")) == -1)) {
				getPlayer().left = true;
				getPlayer().DOWN = false;
				if(Math.abs(getPlayer().d.x) < getPlayer().speed) getPlayer().d.x = -getPlayer().speed;
				else if(getPlayer().RUN) getPlayer().d.x -= 0.05f;

			} else if(GAMEPAD.getButton(Asset.CONTROLS.getInteger("right"))
					|| (Asset.CONTROLS.getBoolean("use_axis") && GAMEPAD.getAxis(Asset.CONTROLS.getInteger("x-axis")) == 1)) {
				getPlayer().left = false;
				getPlayer().DOWN = false;
				if(Math.abs(getPlayer().d.x) < getPlayer().speed) getPlayer().d.x = getPlayer().speed;
				else if(getPlayer().RUN) getPlayer().d.x += 0.05f;

			} if(GAMEPAD.getButton(Asset.CONTROLS.getInteger("shoot")))  {	
				if(getPlayer().charge >= Player.MAX_CHARGE) 	
					getPlayer().charge = Player.MAX_CHARGE;
				else if((getPlayer().MISSILE && !getPlayer().MORPH && getPlayer().itemActive("charge_missile"))
						|| (getPlayer().MORPH && !getPlayer().MISSILE && getPlayer().itemActive("charge_bomb"))
						|| (!getPlayer().MORPH && !getPlayer().MISSILE && getPlayer().itemActive("charge_shot")))
					getPlayer().charge *= Player.CHARGE_RATE;
			} 
		}
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		return super.inputDown(new InputEvent(), buttonCode);
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {
		return super.inputUp(new InputEvent(), buttonCode);
	}

	@Override
	public void connected(Controller controller) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnected(Controller controller) {
		Asset.MESSAGES.add(controller.getName().toUpperCase() + " DISCONNECTED");
		Asset.resetControls();
		Asset.MESSAGES.add("USING: " + Asset.CONTROLS.getString("controller").toUpperCase());
		
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		if(Asset.CONTROLS.getBoolean("use_axis")) {
			if(value == 1) {
				if(axisCode == Asset.CONTROLS.getInteger("y-axis"))	
					return buttonDown(controller, Asset.CONTROLS.getInteger("down"));
				else if(axisCode == Asset.CONTROLS.getInteger("x-axis")) 
					return buttonDown(controller, Asset.CONTROLS.getInteger("right"));
			} else if(value == -1) {
				if(axisCode == Asset.CONTROLS.getInteger("y-axis"))	
					return buttonDown(controller, Asset.CONTROLS.getInteger("up"));
				else if(axisCode == Asset.CONTROLS.getInteger("x-axis")) 
					return buttonDown(controller, Asset.CONTROLS.getInteger("left"));
			} else {
				if(axisCode == Asset.CONTROLS.getInteger("y-axis"))
					return buttonUp(controller, Asset.CONTROLS.getInteger("up")) && buttonUp(controller, Asset.CONTROLS.getInteger("down"));
				else if(axisCode == Asset.CONTROLS.getInteger("x-axis"))
					return buttonUp(controller, Asset.CONTROLS.getInteger("left")) && buttonUp(controller, Asset.CONTROLS.getInteger("right"));
			}
		}
		return false;
	}
	
	@Override
	public void toStage(Stage stage) {
		if(GAMEPAD == null) GAMEPAD = Controllers.getControllers().first();
		Controllers.addListener(this);
		super.toStage(stage);
	}
}
