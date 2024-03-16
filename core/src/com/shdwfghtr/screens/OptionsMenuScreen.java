package com.shdwfghtr.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.shdwfghtr.asset.ConversionService;
import com.shdwfghtr.asset.DataService;
import com.shdwfghtr.asset.InventoryService;
import com.shdwfghtr.asset.OptionsService;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.Sector;

public class OptionsMenuScreen extends MenuScreen {

    @Override
	public void show() {
		super.show();

        //creates a checkbox to turn particle effects on and off
        CheckBox checkParticles = new CheckBox(" Particles", GdxGame.uiService.getSkin());
        checkParticles.setChecked(OptionsService.AreParticlesEnabled());
        checkParticles.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                OptionsService.toggleParticlesEnabled();
            }
        });

        //creates a checkbox to turn fullscreen on and off
        final CheckBox checkFullscreen = new CheckBox(" Fullscreen", GdxGame.uiService.getSkin());
        checkFullscreen.setChecked(OptionsService.IsFullScreen());
        checkFullscreen.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(checkFullscreen.isChecked())
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                else
                    Gdx.graphics.setWindowedMode(Sector.pWIDTH, Sector.pHEIGHT);

                OptionsService.toggleFullscreen();
            }
        });

        //creates a slider for volume control
        Slider sliderVolume = new Slider(0, 1.0f, 0.01f, false, GdxGame.uiService.getSkin()) {
            @Override
            public void act(float delta) {
                if(isDragging())
                    GdxGame.audioService.setMaxVolume(getValue());
                super.act(delta);
            }
        };
        sliderVolume.setValue(GdxGame.audioService.getMaxVolume());
        
        //creates an input box to type a seed
        final TextField fieldSeed = new TextField(
                ConversionService.toString(DataService.getSeed()),
                GdxGame.uiService.getSkin());
        fieldSeed.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    DataService.setSeed(ConversionService.toLong(fieldSeed.getText()));
                    System.out.println("new seed is " + fieldSeed.getText());
                } catch (Exception e) {
                    System.out.println("seed must be digits only");
                    e.printStackTrace();
                }
            }
        });
        fieldSeed.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());

        //creates buttons for going back to the travel screen, to the controls screen, and to reset the player
        TextButton buttonControls, buttonReset, buttonExit;
        buttonControls = new TextButton("Controls", GdxGame.uiService.getSkin());
        buttonControls.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                goToScreen(new ControlsMenuScreen());

            }
        });
        buttonReset = new TextButton("New Player", GdxGame.uiService.getSkin());
        buttonReset.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DataService.reset();
                InventoryService.reset();
                goToPreviousScreen();
            }
        });
        buttonExit = new TextButton("Exit Game", GdxGame.uiService.getSkin());
        buttonExit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();

            }
        });
        Table table = new Table();
        table.setBounds(0, 0, getStage().getWidth(), getStage().getHeight());
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
        addActor(table);
    }
	
	@Override
	void goToPreviousScreen() {
		goToScreen(new TravelScreen());
	}
	
}
