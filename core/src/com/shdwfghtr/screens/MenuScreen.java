package com.shdwfghtr.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.shdwfghtr.explore.GdxGame;

class MenuScreen extends Group implements Screen {
	private ParticleEffectPool.PooledEffect stars;

	@Override
	protected void setStage(Stage stage) {
		super.setStage(stage);

		if(stage != null) {
			stage.setKeyboardFocus(this);
			addListener(new MenuInputListener());
			setBounds(0, 0, stage.getWidth(), stage.getHeight());

			stars = GdxGame.particleService.obtain("stars", false);
			stars.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
			float starsWidth = stars.getEmitters().first().getSpawnWidth().getHighMax();
			stars.scaleEffect(Gdx.graphics.getWidth() / starsWidth);
			GdxGame.particleService.add(stars);
		}
	}

	@Override
	public void show() {
		GdxGame.uiService.getStage().addActor(this);
	}

	@Override
	public void render(float delta) {
	}

	void goToScreen(Screen screen) {
		try {
			((GdxGame) Gdx.app.getApplicationListener()).setScreen(screen);
			GdxGame.particleService.remove(stars);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	void goToPreviousScreen() {
		System.out.println("goToPreviousScreen needs to be overridden");
	}

	@Override
	public void resize(int width, int height) {
		GdxGame.uiService.getStage().getViewport().update(width, height);

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
		this.clear();
		this.remove();
	}

	private class MenuInputListener extends InputListener {
		@Override
		public boolean keyUp(InputEvent event, int keycode) {
			if(keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE){
				goToPreviousScreen();
				return true;
			}
			return false;
		}
	}
}
