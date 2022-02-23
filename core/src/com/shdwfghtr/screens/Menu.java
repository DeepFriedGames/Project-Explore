package com.shdwfghtr.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.GdxGame;

class Menu implements Screen {
	private final SpriteBatch BATCH = new SpriteBatch();
	private ParticleEffectPool.PooledEffect STARS;
	final Table table = new Table(Asset.getSkin());
	String headerText = "";
	boolean backButtonPressed = false;

	Menu(String headerText) {
		this.headerText = headerText;
	}

	@Override
	public void show() {
		Asset.getStage().addActor(table);
		Asset.getStage().setKeyboardFocus(table);
		table.setBounds(0, 0, Asset.getStage().getWidth(), Asset.getStage().getHeight());
		table.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if(keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
					backButtonPressed = true;
					return true;
				}

				return false;
			}
		});

		//adds the header to the stage
		Asset.GLYPH.setText(Asset.getHeaderFont(), headerText);
		Actor header = new Actor() {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				Asset.getHeaderFont().draw(batch, headerText, getX(), getY());
			}
		};
		header.setBounds(0, table.getHeight(), table.getWidth(), Asset.GLYPH.height);
		table.addActor(header);

		//scale the stars to the right size
		STARS = Asset.getParticles().obtain("stars", false);
		STARS.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		STARS.scaleEffect(Gdx.graphics.getWidth() / Asset.CAM_WIDTH);
		Asset.getParticles().add(STARS);

	}

	@Override
	public void render(float delta) {
		Asset.getStage().act(delta);
		Asset.getParticles().update(delta);

		BATCH.setProjectionMatrix(Asset.getStage().getCamera().combined);
		BATCH.begin();
		Asset.getParticles().draw(BATCH);
		BATCH.end();

		Asset.getStage().draw();

		if(backButtonPressed) {
			backButtonPressed = false;
			goToPreviousScreen();
		}
	}

	void goToScreen(Screen screen) {
		try {
			((GdxGame) Gdx.app.getApplicationListener()).setScreen(screen);
			Asset.getParticles().remove(STARS);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	void goToPreviousScreen() {
		System.out.println("goToPreviousScreen needs to be overridden");
	}

	@Override
	public void resize(int width, int height) {
		Asset.getStage().getViewport().update(width, height);

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		dispose();

	}

	@Override
	public void dispose() {
		Controllers.clearListeners();
		table.clear();
		table.remove();
		Asset.getStage().getRoot().clearChildren();

	}

	public class MenuButton extends TextButton {
		Drawable icon;

		MenuButton(String text, Skin skin) {
			super(text, skin);
		}

		@Override
		public void draw(Batch batch, float parentAlpha) {
			super.draw(batch, parentAlpha);
			if(icon != null)
				icon.draw(batch, getX() + (getWidth() - icon.getMinWidth())/2, getY() + (getHeight() - icon.getMinHeight())/2,
						icon.getMinWidth(), icon.getMinHeight());
		}

		@Override
		public void toggle() {
			Asset.getMusicHandler().playSound("select");
		}

	}
}
