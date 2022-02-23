package com.shdwfghtr.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.input.GamepadHandler;
import com.shdwfghtr.input.TouchHandler;

import java.util.HashMap;

public class ControlsMenu extends Menu {
	private final Table touchTable = new Table(Asset.getSkin()), dPadTable = new Table();
	private final HashMap<String, TextButton> inputButtons = new HashMap<String, TextButton>();
	private final Array<Button> touchButtons = new Array<Button>(TouchHandler.BUTTONS);
	private final Array<Slider> touchSliders = new Array<Slider>(5);
	private final Touchpad touchPad = new Touchpad(0, Asset.getSkin());
	private final Dialog inputDialog = new Dialog("Reset Input", Asset.getSkin());
	private String inputKey = null;
	private CheckBox checkBox;
	private Cell<Table> touchCell;
	private Cell dPadCell;
	private ControllerAdapter adapter;

	ControlsMenu() {
		super("Controls");
	}

	@Override
	public void render(float delta) {
		if(inputButtons.size() > 0) {
			inputButtons.get("up").setDisabled(Asset.CONTROLS.getBoolean("use_axis"));
			inputButtons.get("down").setDisabled(Asset.CONTROLS.getBoolean("use_axis"));
			inputButtons.get("left").setDisabled(Asset.CONTROLS.getBoolean("use_axis"));
			inputButtons.get("right").setDisabled(Asset.CONTROLS.getBoolean("use_axis"));
			inputButtons.get("x-axis").setDisabled(!Asset.CONTROLS.getBoolean("use_axis")
					|| Asset.CONTROLS.getString("controller").matches("touch"));
			inputButtons.get("y-axis").setDisabled(!Asset.CONTROLS.getBoolean("use_axis")
					|| Asset.CONTROLS.getString("controller").matches("touch"));

			for(TextButton b : inputButtons.values()) {
				if(b.isDisabled())
					b.setTouchable(Touchable.disabled);
				else
					b.setTouchable(Touchable.enabled);
				b.setVisible(!b.isDisabled());
				table.findActor(b.getName() + " label").setVisible(!b.isDisabled());
			}
		}
		if(touchSliders.size > 0) {
			Slider[] sliders = touchSliders.toArray(Slider.class);
			for(Slider slider : sliders)
				slider.setVisible(Asset.CONTROLS.getString("controller").matches("touch"));
		}
		if(checkBox != null)
			if(Asset.CONTROLS.getString("controller").matches("gamepad")
					|| Asset.CONTROLS.getString("controller").matches("touch")) {
				checkBox.setVisible(true);
				checkBox.setDisabled(false);
				checkBox.setTouchable(Touchable.enabled);
			} else {
				checkBox.setVisible(false);
				checkBox.setChecked(false);
				checkBox.setDisabled(true);
				checkBox.setTouchable(Touchable.disabled);
			}
		if(touchTable.hasParent())
			touchTable.setVisible(Asset.CONTROLS.getString("controller").matches("touch"));

		super.render(delta);
	}

	@Override
	public void show() {
		super.show();
		int pad = 5;

		inputDialog.setSize(Asset.getStage().getWidth() / 3, Asset.getStage().getHeight() / 3);
		inputDialog.setPosition((Asset.getStage().getWidth() - inputDialog.getWidth()) / 2,
				(Asset.getStage().getHeight() - inputDialog.getHeight()) / 2);

		//creates a table with a list of possible inputs to choose from, button names, and current values
		//values can be customized by clicking buttons.

		//first the table
		table.setName("Controls Table");
		adapter = new ControllerAdapter() {
			@Override
			public boolean buttonDown(Controller controller, int button) {
				return replaceInput(button);
			}

			@Override
			public boolean axisMoved (Controller controller, int axisCode, float value) {
				return Asset.CONTROLS.getBoolean("use_axis") && replaceInput(axisCode);
			}
		};

		//a Table for the input selections
		Table selectionTable = new Table(Asset.getSkin());

		//first a list of possible input devices to choose from
		Label selectLabel = new Label("Choose Input Device:", Asset.getSkin());
		final SelectBox<String> selectBox = new SelectBox<String>(Asset.getSkin());
		Array<String> items = new Array<String>();
		if(Gdx.input.isPeripheralAvailable(Peripheral.HardwareKeyboard)) items.add(" Keyboard");
		if(Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen)) items.add(" Touch Screen");
		Controller[] controllers = Controllers.getControllers().toArray(Controller.class);
		for(Controller controller : controllers) {
			String name = controller.getName();
			if(name != null) {
				if(name.length() > 13) name = " " + name.substring(0, 13);
				items.add(name);
			}
		}
		selectBox.setItems(items);
		if(Asset.CONTROLS.getString("controller").matches("keyboard")) selectBox.setSelected(" Keyboard");
		if(Asset.CONTROLS.getString("controller").matches("touch")) selectBox.setSelected(" Touch Screen");
		if(Asset.CONTROLS.getString("controller").matches("gamepad")) selectBox.setSelected(items.peek());
		selectBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(selectBox.getSelected().contains("Keyboard"))
					Asset.CONTROLS.putString("controller", "keyboard");
				else if(selectBox.getSelected().contains("Touch Screen"))
					Asset.CONTROLS.putString("controller", "touch");
				else {
					Controller[] controllers = Controllers.getControllers().toArray(Controller.class);
					for(Controller controller : controllers)
						if(controller.getName().contains(selectBox.getSelected().substring(1, selectBox.getSelected().length() -  1))) {
							Asset.CONTROLS.putString("controller", "gamepad");
							GamepadHandler.GAMEPAD = controller;
						}
				}

				Asset.CONTROLS.flush();
			}
		});
		selectBox.setName("Select Box");

		//a checkbox allowing users to use a controller's axis
		checkBox = new CheckBox(" XY Axis", Asset.getSkin());
		checkBox.setChecked(Asset.CONTROLS.getBoolean("use_axis"));
		checkBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Asset.CONTROLS.putBoolean("use_axis", !Asset.CONTROLS.getBoolean("use_axis"));
				Asset.CONTROLS.flush();
				if(Asset.CONTROLS.getBoolean("use_axis"))
					dPadCell.setActor(touchPad);
				else
					dPadCell.setActor(dPadTable);
			}
		});
		checkBox.setName("Check Box");

		//creates touchscreen options, such as use XYaxis, button padding, button size, button position
		float size = Asset.CONTROLS.getFloat("touch_size", 64f),
				padding = Asset.CONTROLS.getFloat("touch_padding", 1f),
				border = Asset.CONTROLS.getFloat("touch_border", 5f),
				position = Asset.CONTROLS.getFloat("touch_position", 5f);

		final Slider sensitivitySlider = new Slider(0, 32, 1, false, Asset.getSkin());
		sensitivitySlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Asset.CONTROLS.putFloat("touch_sensitivity", sensitivitySlider.getValue());
				Asset.CONTROLS.flush();
			}
		});
		sensitivitySlider.setValue(Asset.CONTROLS.getFloat("touch_sensitivity", 0.5f));
		sensitivitySlider.setName("Sensitivity Slider");

		final Slider paddingSlider = new Slider(0, 32, 1, false, Asset.getSkin());
		paddingSlider.addListener(new ChangeListener() {
			final Array<Cell> cells = new Array<Cell>();
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(cells.size <= 0) {
					Table buttonTable = touchTable.findActor("Button Table");
					if (buttonTable != null) cells.addAll(buttonTable.getCells());
					if (dPadTable != null) cells.addAll(dPadTable.getCells());
					if (touchTable != null) cells.addAll(touchTable.getCells());
				}

				Cell[] array = cells.toArray(Cell.class);
				for(Cell cell : array)
					cell.pad(paddingSlider.getValue());

				touchTable.invalidate();

				Asset.CONTROLS.putFloat("touch_padding", paddingSlider.getValue());
				Asset.CONTROLS.flush();
			}
		});
		paddingSlider.setValue(padding);
		paddingSlider.setName("Padding Slider");

		final Slider sizeSlider = new Slider(8, 96, 2, false, Asset.getSkin());
		sizeSlider.addListener(new ChangeListener() {
			final Array<Cell> cells = new Array<Cell>();
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(cells.size <= 0) {
					Table buttonTable = touchTable.findActor("Button Table");
					if (buttonTable != null) cells.addAll(buttonTable.getCells());
					if (dPadTable != null) cells.addAll(dPadTable.getCells());
					if (touchTable != null) cells.addAll(touchTable.getCells());
				}

				Cell[] array = cells.toArray(Cell.class);
				for(Cell cell : array)
					cell.size(sizeSlider.getValue());

				touchTable.invalidate();

				Asset.CONTROLS.putFloat("touch_size", sizeSlider.getValue());
				Asset.CONTROLS.flush();

			}
		});
		sizeSlider.setValue(size);
		sizeSlider.setName("Size Slider");

		final Slider borderSlider = new Slider(0, Gdx.graphics.getWidth() / 3,
				1, false, Asset.getSkin());
		borderSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				touchTable.padLeft(borderSlider.getValue())
						.padRight(borderSlider.getValue());
				touchTable.invalidate();
				Asset.CONTROLS.putFloat("touch_border", borderSlider.getValue());
				Asset.CONTROLS.flush();
			}
		});
		borderSlider.setValue(border);
		borderSlider.setName("Border Slider");

		final Slider positionSlider = new Slider(0,Gdx.graphics.getHeight() - 150,
				2, true, Asset.getSkin());
		positionSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				touchTable.padBottom(positionSlider.getValue());
				touchTable.invalidate();
				Asset.CONTROLS.putFloat("touch_position", positionSlider.getValue());
				Asset.CONTROLS.flush();
			}
		});
		positionSlider.setValue(position);
		positionSlider.setName("Position Slider");

		touchSliders.addAll(sensitivitySlider, sizeSlider, paddingSlider, borderSlider, positionSlider);

		selectionTable.add(selectLabel).right().pad(pad);
		selectionTable.add(selectBox).pad(pad).row();
		selectionTable.add();
		selectionTable.add(checkBox).pad(pad).row();

		Slider[] sliders = touchSliders.toArray(Slider.class);
		for(Slider slider : sliders) {
			selectionTable.add(slider.getName().replace(" Slider", ": "));
			selectionTable.add(slider).pad(pad).row();
		}

		//creates a table with touchScreen input representations
		for(int i=0; i<touchButtons.size; i++) {
			touchButtons.get(i).addAction(Actions.alpha(0.5f));
			touchButtons.get(i).setName("Touch Button " + i);
			touchButtons.get(i).addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float screenX, float screenY, int pointer, int button) {
					int keycode;
					if(touchButtons.contains((Button) event.getTarget(), true)) {
						keycode = touchButtons.indexOf((Button) event.getTarget(), true);
						inputDialog.removeActor(touchTable);
						inputDialog.remove();
						touchCell.setActor(touchTable);
						return replaceInput(keycode);
					}
					return false;
				}
			});
		}

		createDPadTouchTable(size, padding, border, position);

		//finally, all of the inputs received by the game
		//these buttons have their own Table as well
		final Table inputTable = new Table(Asset.getSkin());

		for(final String key : Asset.INPUT_LIST) {
			//along with all of the buttons used to change the input values
			Label buttonLabel = new Label(key.toUpperCase().concat(": "), Asset.getSkin());
			buttonLabel.setName(key + " label");
			final TextButton button = new TextButton(String.valueOf(Asset.CONTROLS.getInteger(key)), Asset.getSkin());
			button.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if(inputKey == null) {
						button.setText("--");
						inputKey = key;

						inputDialog.clear();
						inputDialog.text("Press the desired input \nfor " + key.toUpperCase() + " now.");
						table.addActor(inputDialog);

						if(Asset.CONTROLS.getString("controller").matches("keyboard")) {
							inputDialog.addListener(new InputListener() {
								@Override
								public boolean keyDown(InputEvent event, int keycode) {
									return replaceInput(keycode);
								}
							});
							Asset.getStage().setKeyboardFocus(inputDialog);
						} else if(Asset.CONTROLS.getString("controller").matches("gamepad"))
							Controllers.addListener(adapter);
						else if(Asset.CONTROLS.getString("controller").matches("touch")) {
							inputDialog.setSize(touchTable.getWidth(), touchTable.getHeight() * 2);
							inputDialog.add(touchTable).expandX();
						}
					}
				}
			});
			button.setName(key);
			inputTable.add(buttonLabel).expandX().right().pad(pad);
			inputTable.add(button).width(100).pad(pad).row();
			inputButtons.put(key, button);

		}

		table.add(selectionTable).pad(pad);
		table.add(inputTable).pad(pad).row();
		touchCell = table.add(touchTable).expand().fillX().bottom().colspan(2);
		Asset.getStage().addActor(table);
	}

	private void createDPadTouchTable(float size, float padding, float border, float position) {
		touchPad.setSize(size * 2, size * 2);
		touchPad.addAction(Actions.alpha(0.5f));

		Table buttonTable = new Table();
		buttonTable.add(touchButtons.get(4)).size(size).pad(padding).center().colspan(2).row();
		buttonTable.add(touchButtons.get(6)).size(size).pad(padding);
		buttonTable.add(touchButtons.get(7)).size(size).pad(padding).row();
		buttonTable.add(touchButtons.get(5)).size(size).pad(padding).center().colspan(2);
		buttonTable.setName("Button Table");

		dPadTable.add(touchButtons.get(0)).size(size).pad(padding).center().colspan(2).row();
		dPadTable.add(touchButtons.get(2)).size(size).pad(padding);
		dPadTable.add(touchButtons.get(3)).size(size).pad(padding).row();
		dPadTable.add(touchButtons.get(1)).size(size).pad(padding).center().colspan(2);
		dPadTable.setName("D-Pad Table");

		if(Asset.CONTROLS.getBoolean("use_axis"))
			dPadCell = touchTable.add(touchPad).size(size * 2).pad(padding).center();
		else
			dPadCell = touchTable.add(dPadTable);

		touchTable.add(touchButtons.get(8)).size(size).pad(padding).expandX().center().bottom();
		touchTable.add(touchButtons.get(9)).size(size).pad(padding).expandX().center().bottom();
		touchTable.add(buttonTable);

		touchTable.padRight(border);
		touchTable.padLeft(border);
		touchTable.padBottom(position);

		touchTable.setName("Touch Table");
	}

	private boolean replaceInput(int keycode) {
		if(inputKey != null) {
			Asset.CONTROLS.putInteger(inputKey, keycode);
			Asset.CONTROLS.flush();
			inputButtons.get(inputKey).setText(String.valueOf(keycode));
			inputKey = null;
			Asset.getStage().unfocus(inputDialog);
			inputDialog.remove();
			Controllers.removeListener(adapter);
			return true;
		}
		return false;
	}
	
	@Override
	void goToPreviousScreen() {
		goToScreen(new OptionsMenu());
	}
}

