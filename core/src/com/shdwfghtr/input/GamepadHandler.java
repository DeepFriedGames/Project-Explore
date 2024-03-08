package com.shdwfghtr.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.shdwfghtr.asset.ControllerService;
import com.shdwfghtr.asset.InventoryService;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.GameState;
import com.shdwfghtr.explore.GdxGame;

public class GamepadHandler extends InputHandler implements ControllerListener {
	public static Controller GAMEPAD;

    @Override
    public boolean isInputDown(String input) {
		if(ControllerService.isUsingAxis()) {
			if (input.matches("left"))
				return GAMEPAD.getAxis(ControllerService.getXAxis()) == -1;
			if (input.matches("right"))
				return GAMEPAD.getAxis(ControllerService.getXAxis()) == 1;
			if(input.matches("down"))
				return GAMEPAD.getAxis(ControllerService.getYAxis()) == -1;
			if(input.matches("up"))
				return GAMEPAD.getAxis(ControllerService.getYAxis()) == 1;
		}
        
        return GAMEPAD.getButton(ControllerService.getInput(input));
    }

	@Override
	public void act(float delta) {
		super.act(delta);
		if(player != null && gameScreen.getState() == GameState.PLAY) {
			if(GAMEPAD.getButton(ControllerService.getInput("left"))
					|| (ControllerService.isUsingAxis() && GAMEPAD.getAxis(ControllerService.getInput("x-axis")) == -1)) {
				getPlayer().left = true;
				getPlayer().DOWN = false;
				if(Math.abs(getPlayer().d.x) < getPlayer().speed) getPlayer().d.x = -getPlayer().speed;
				else if(getPlayer().RUN) getPlayer().d.x -= 0.05f;

			} else if(GAMEPAD.getButton(ControllerService.getInput("right"))
					|| (ControllerService.isUsingAxis() && GAMEPAD.getAxis(ControllerService.getInput("x-axis")) == 1)) {
				getPlayer().left = false;
				getPlayer().DOWN = false;
				if(Math.abs(getPlayer().d.x) < getPlayer().speed) getPlayer().d.x = getPlayer().speed;
				else if(getPlayer().RUN) getPlayer().d.x += 0.05f;

			} if(GAMEPAD.getButton(ControllerService.getInput("shoot")))  {
				if(getPlayer().charge >= Player.MAX_CHARGE) 	
					getPlayer().charge = Player.MAX_CHARGE;
				else if((getPlayer().MISSILE && !getPlayer().MORPH && InventoryService.isActive("charge_missile"))
						|| (getPlayer().MORPH && !getPlayer().MISSILE && InventoryService.isActive("charge_bomb"))
						|| (!getPlayer().MORPH && !getPlayer().MISSILE && InventoryService.isActive("charge_shot")))
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
		GdxGame.uiService.addMessage(controller.getName().toUpperCase() + " DISCONNECTED");
		ControllerService.resetControls();
		GdxGame.uiService.addMessage("USING: " + ControllerService.getCurrentController());
		
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		if(ControllerService.isUsingAxis()) {
			if(value == 1) {
				if(axisCode == ControllerService.getInput("y-axis"))
					return buttonDown(controller, ControllerService.getInput("down"));
				else if(axisCode == ControllerService.getInput("x-axis"))
					return buttonDown(controller, ControllerService.getInput("right"));
			} else if(value == -1) {
				if(axisCode == ControllerService.getInput("y-axis"))
					return buttonDown(controller, ControllerService.getInput("up"));
				else if(axisCode == ControllerService.getInput("x-axis"))
					return buttonDown(controller, ControllerService.getInput("left"));
			} else {
				if(axisCode == ControllerService.getInput("y-axis"))
					return buttonUp(controller, ControllerService.getInput("up")) && buttonUp(controller, ControllerService.getInput("down"));
				else if(axisCode == ControllerService.getInput("x-axis"))
					return buttonUp(controller, ControllerService.getInput("left")) && buttonUp(controller, ControllerService.getInput("right"));
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
