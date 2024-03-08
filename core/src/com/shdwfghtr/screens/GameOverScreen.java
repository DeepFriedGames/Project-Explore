package com.shdwfghtr.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.shdwfghtr.asset.DataService;
import com.shdwfghtr.asset.InventoryService;
import com.shdwfghtr.explore.GdxGame;

public class GameOverScreen implements Screen {

	@Override
	public void show() {
        //permadeath!!!
		DataService.reset();
		InventoryService.reset();
        ((GdxGame) Gdx.app.getApplicationListener()).setScreen(new TravelScreen());

	}

	@Override
	public void render(float delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

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
		GdxGame.uiService.getStage().clear();

	}

}
