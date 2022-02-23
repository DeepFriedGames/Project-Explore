package com.shdwfghtr.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.GdxGame;

public class Splash implements Screen{
	private int numDots;
	private final BitmapFont font = new BitmapFont(Gdx.files.internal("skin/font-export.fnt"));
	private final SpriteBatch batch = new SpriteBatch();
	
	@Override
	public void render(float delta) {
		if(Asset.getManager().update()) {
			((GdxGame) Gdx.app.getApplicationListener()).asset.initializeResources();
			Asset.initialize();
            numDots++;

			//Changes the screen to be the main menu
            ((GdxGame) Gdx.app.getApplicationListener()).setScreen(new TravelScreen());
		}

		Asset.GLYPH.setText(font, "Loading");
		numDots = Math.round(Asset.getManager().getProgress() * 3);
		batch.begin();
		font.draw(batch, "Loading", 10, Asset.GLYPH.height + 10);
		for(int i=0; i<numDots; i++)
			font.draw(batch, ".", (Asset.GLYPH.width + 4) + i*10, Asset.GLYPH.height + 10);
		batch.end();
	}

	@Override
	public void resize(int width, int height) {	
		if(Asset.getStage() != null)
			Asset.getStage().getViewport().update(width, height);
	}

	@Override
	public void show() {
		Gdx.input.setCatchMenuKey(true);
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void pause() { }

	@Override
	public void resume() { }

	@Override
	public void dispose() {	}

}
