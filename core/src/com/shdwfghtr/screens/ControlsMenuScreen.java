package com.shdwfghtr.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
import com.shdwfghtr.asset.ControllerService;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.input.GamepadHandler;
import com.shdwfghtr.input.TouchHandler;

import java.util.HashMap;

public class ControlsMenuScreen extends MenuScreen {
	private final Table touchTable = new Table(GdxGame.uiService.getSkin()), dPadTable = new Table();
	private final HashMap<String, TextButton> inputButtons = new HashMap<>();
	private final Array<Button> touchButtons = new Array<>(TouchHandler.BUTTONS);
	private final Array<Slider> touchSliders = new Array<>(5);
	private final Touchpad touchPad = new Touchpad(0, GdxGame.uiService.getSkin());
	private final Dialog inputDialog = new Dialog("Reset Input", GdxGame.uiService.getSkin());
	private final Stage stage = GdxGame.uiService.getStage();
	private String inputKey = null;
	private CheckBox checkBox;
	private Cell<Table> touchCell;
	private Cell<Actor> dPadCell;
	private ControllerAdapter adapter;

	@Override
	public void render(float delta) {
		if(inputButtons.size() > 0) {
			if(ControllerService.isUsingAxis()){
				inputButtons.get("up").setDisabled(true);
				inputButtons.get("down").setDisabled(true);
				inputButtons.get("left").setDisabled(true);
				inputButtons.get("right").setDisabled(true);
			} else if(ControllerService.isTouch()) {
				inputButtons.get("x-axis").setDisabled(true);
				inputButtons.get("y-axis").setDisabled(true);
			}

			for(TextButton b : inputButtons.values()) {
				if(b.isDisabled())
					b.setTouchable(Touchable.disabled);
				else
					b.setTouchable(Touchable.enabled);
				b.setVisible(!b.isDisabled());
				this.findActor(b.getName() + " label").setVisible(!b.isDisabled());
			}
		}
		if(touchSliders.size > 0) {
			Slider[] sliders = touchSliders.toArray(Slider.class);
			for(Slider slider : sliders)
				slider.setVisible(ControllerService.isTouch());
		}
		if(checkBox != null)
			if(ControllerService.isGamepad() || ControllerService.isTouch()) {
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
			touchTable.setVisible(ControllerService.isTouch());

		super.render(delta);
	}

	@Override
	public void show() {
		super.show();
		int pad = 5;
		
		inputDialog.setSize(stage.getWidth() / 3, stage.getHeight() / 3);
		inputDialog.setPosition((stage.getWidth() - inputDialog.getWidth()) / 2,
				(stage.getHeight() - inputDialog.getHeight()) / 2);

		//creates a table with a list of possible inputs to choose from, button names, and current values
		//values can be customized by clicking buttons.

		//first the table
		this.setName("Controls");
		adapter = new ControllerAdapter() {
			@Override
			public boolean buttonDown(Controller controller, int button) {
				return replaceInput(button);
			}

			@Override
			public boolean axisMoved (Controller controller, int axisCode, float value) {
				return ControllerService.isUsingAxis() && replaceInput(axisCode);
			}
		};

		//a Table for the input selections
		Table selectionTable = new Table(GdxGame.uiService.getSkin());

		//first a list of possible input devices to choose from
		Label selectLabel = new Label("Choose Input Device:", GdxGame.uiService.getSkin());
		final SelectBox<String> selectBox = new SelectBox<>(GdxGame.uiService.getSkin());
		Array<String> items = new Array<>();
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
		if(ControllerService.isKeyboard()) selectBox.setSelected(" Keyboard");
		if(ControllerService.isTouch()) selectBox.setSelected(" Touch Screen");
		if(ControllerService.isGamepad()) selectBox.setSelected(items.peek());
		selectBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(selectBox.getSelected().contains("Keyboard"))
					ControllerService.setController("keyboard");
				else if(selectBox.getSelected().contains("Touch Screen"))
					ControllerService.setController("touch");
				else {
					Controller[] controllers = Controllers.getControllers().toArray(Controller.class);
					for(Controller controller : controllers)
						if(controller.getName().contains(selectBox.getSelected().substring(1, selectBox.getSelected().length() -  1))) {
							ControllerService.setController("gamepad");
							GamepadHandler.GAMEPAD = controller;
						}
				}
			}
		});
		selectBox.setName("Select Box");

		//a checkbox allowing users to use a controller's axis
		checkBox = new CheckBox(" XY Axis", GdxGame.uiService.getSkin());
		checkBox.setChecked(ControllerService.isUsingAxis());
		checkBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				ControllerService.setUsingAxis(!ControllerService.isUsingAxis());
				if(ControllerService.isUsingAxis())
					dPadCell.setActor(touchPad);
				else
					dPadCell.setActor(dPadTable);
			}
		});
		checkBox.setName("Check Box");

		//creates touchscreen options, such as use XYaxis, button padding, button size, button position
		float size = ControllerService.getTouchSize(),
				padding = ControllerService.getTouchPadding(),
				border = ControllerService.getTouchBorder(),
				position = ControllerService.getTouchPosition();

		final Slider sensitivitySlider = new Slider(0, 32, 1, false, GdxGame.uiService.getSkin());
		sensitivitySlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				ControllerService.setTouchSensitivity(sensitivitySlider.getValue());
			}
		});
		sensitivitySlider.setValue(ControllerService.getTouchSensitivity());
		sensitivitySlider.setName("Sensitivity Slider");

		final Slider paddingSlider = new Slider(0, 32, 1, false, GdxGame.uiService.getSkin());
		paddingSlider.addListener(new ChangeListener() {
			final Array<Cell> cells = new Array<>();
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(cells.size <= 0) {
					Table buttonTable = touchTable.findActor("Button Table");
					if (buttonTable != null) cells.addAll(buttonTable.getCells());
					if (dPadTable != null) cells.addAll(dPadTable.getCells());
					cells.addAll(touchTable.getCells());
				}

				Cell[] array = cells.toArray(Cell.class);
				for(Cell cell : array)
					cell.pad(paddingSlider.getValue());

				touchTable.invalidate();

				ControllerService.setTouchPadding(paddingSlider.getValue());
			}
		});
		paddingSlider.setValue(padding);
		paddingSlider.setName("Padding Slider");

		final Slider sizeSlider = new Slider(8, 96, 2, false, GdxGame.uiService.getSkin());
		sizeSlider.addListener(new ChangeListener() {
			final Array<Cell> cells = new Array<Cell>();
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(cells.size <= 0) {
					Table buttonTable = touchTable.findActor("Button Table");
					if (buttonTable != null) cells.addAll(buttonTable.getCells());
					if (dPadTable != null) cells.addAll(dPadTable.getCells());
					cells.addAll(touchTable.getCells());
				}

				Cell[] array = cells.toArray(Cell.class);
				for(Cell cell : array)
					cell.size(sizeSlider.getValue());

				touchTable.invalidate();

				ControllerService.setTouchSize(sizeSlider.getValue());
			}
		});
		sizeSlider.setValue(size);
		sizeSlider.setName("Size Slider");

		final Slider borderSlider = new Slider(0, Gdx.graphics.getWidth() / 3f,
				1, false, GdxGame.uiService.getSkin());
		borderSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				touchTable.padLeft(borderSlider.getValue())
						.padRight(borderSlider.getValue());
				touchTable.invalidate();
				ControllerService.setTouchBorder(borderSlider.getValue());
			}
		});
		borderSlider.setValue(border);
		borderSlider.setName("Border Slider");

		final Slider positionSlider = new Slider(0,Gdx.graphics.getHeight() - 150,
				2, true, GdxGame.uiService.getSkin());
		positionSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				touchTable.padBottom(positionSlider.getValue());
				touchTable.invalidate();
				ControllerService.setTouchPosition(positionSlider.getValue());
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
		final Table inputTable = new Table(GdxGame.uiService.getSkin());

		for(final String key : ControllerService.INPUT_LIST) {
			//along with all of the buttons used to change the input values
			Label buttonLabel = new Label(key.toUpperCase().concat(": "), GdxGame.uiService.getSkin());
			buttonLabel.setName(key + " label");
			final TextButton button = new TextButton(String.valueOf(ControllerService.getInput(key)), GdxGame.uiService.getSkin());
			button.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if(inputKey == null) {
						button.setText("--");
						inputKey = key;

						inputDialog.clear();
						inputDialog.text("Press the desired input \nfor " + key.toUpperCase() + " now.");
						ControlsMenuScreen.this.addActor(inputDialog);

						if(ControllerService.isKeyboard()) {
							inputDialog.addListener(new InputListener() {
								@Override
								public boolean keyDown(InputEvent event, int keycode) {
									return replaceInput(keycode);
								}
							});
							stage.setKeyboardFocus(inputDialog);
						} else if(ControllerService.isGamepad())
							Controllers.addListener(adapter);
						else if(ControllerService.isTouch()) {
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

		Table table = new Table();
		table.add(selectionTable).pad(pad);
		table.add(inputTable).pad(pad).row();
		touchCell = table.add(touchTable).expand().fillX().bottom().colspan(2);
		this.addActor(table);
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

		if(ControllerService.isUsingAxis())
			dPadCell = touchTable.add((Actor)touchPad).size(size * 2).pad(padding).center();
		else
			dPadCell = touchTable.add((Actor)dPadTable);

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
			ControllerService.setInput(inputKey, keycode);
			inputButtons.get(inputKey).setText(String.valueOf(keycode));
			inputKey = null;
			stage.unfocus(inputDialog);
			inputDialog.remove();
			Controllers.removeListener(adapter);
			return true;
		}
		return false;
	}
	
	@Override
	void goToPreviousScreen() {
		GdxGame.goToScreen(new OptionsMenuScreen());
	}
}

