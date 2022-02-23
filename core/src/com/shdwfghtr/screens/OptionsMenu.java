package com.shdwfghtr.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.shdwfghtr.explore.Asset;

public class OptionsMenu extends Menu {

    OptionsMenu() {
        super("Options");
    }

    @Override
	public void show() {
		super.show();

        //creates a checkbox to turn particle effects on and off
        CheckBox checkParticles = new CheckBox(" Particles", Asset.getSkin());
        checkParticles.setChecked(Asset.CONTROLS.getBoolean("particles"));
        checkParticles.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Asset.CONTROLS.putBoolean("particles", !Asset.CONTROLS.getBoolean("particles"));
                Asset.CONTROLS.flush();
            }
        });

        //creates a checkbox to turn fullscreen on and off
        final CheckBox checkFullscreen = new CheckBox(" Fullscreen", Asset.getSkin());
        checkFullscreen.setChecked(Asset.CONTROLS.getBoolean("fullscreen", false));
        checkFullscreen.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(checkFullscreen.isChecked())
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                else
                    Gdx.graphics.setWindowedMode(Asset.CAM_WIDTH * 3, Asset.CAM_HEIGHT * 3);

                Asset.CONTROLS.putBoolean("fullscreen", !Asset.CONTROLS.getBoolean("fullscreen"));
                Asset.CONTROLS.flush();
            }
        });

        //creates a slider for volume control
        Slider sliderVolume = new Slider(0, 1.0f, 0.01f, false, Asset.getSkin()) {
            @Override
            public void act(float delta) {
                if(isDragging())
                    Asset.getMusicHandler().setMaxVolume(getValue());
                super.act(delta);
            }
        };
        sliderVolume.setValue(Asset.getMusicHandler().getMaxVolume());
        
        //creates an input box to type a seed
        final TextField fieldSeed = new TextField(
                Asset.DATA.contains("seed") ? Asset.Convert.toString(Asset.DATA.getLong("seed")) : "",
                Asset.getSkin());
        fieldSeed.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    System.out.println("new seed is " + fieldSeed.getText());
                    Asset.DATA.putLong("seed", Asset.Convert.toLong(fieldSeed.getText()));
                    Asset.DATA.flush();
                } catch (Exception e) {
                    System.out.println("seed must be digits only");
                    e.printStackTrace();
                }
            }
        });
        fieldSeed.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());

        //creates buttons for going back to the travel screen, to the controls screen, and to reset the player
        TextButton buttonControls, buttonReset, buttonExit;
        buttonControls = new TextButton("Controls", Asset.getSkin());
        buttonControls.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                goToScreen(new ControlsMenu());

            }
        });
        buttonReset = new TextButton("New Player", Asset.getSkin());
        buttonReset.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Asset.resetData();
                backButtonPressed = true;

            }
        });
        buttonExit = new TextButton("Exit Game", Asset.getSkin());
        buttonExit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();

            }
        });
        table.add("Seed: ").right().pad(10);
        table.add(fieldSeed).pad(10).row();
        table.add("Volume: ").right().pad(10);
        table.add(sliderVolume).pad(10).row();
        if(Gdx.app.getType() == Application.ApplicationType.Desktop)
            table.add(checkFullscreen).pad(10).colspan(2).row();
        table.add(checkParticles).pad(10).colspan(2).row();
        table.add(buttonControls).fill().pad(10).colspan(2).left().row();
        table.add(buttonReset).fill().pad(10).colspan(2).left().row();
        table.add(buttonExit).fill().pad(10).padTop(40).colspan(2).left();
    }
	
	@Override
	void goToPreviousScreen() {
		goToScreen(new TravelScreen());
	}
	
}
