package com.shdwfghtr.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.shdwfghtr.asset.AudioService;
import com.shdwfghtr.asset.DataService;
import com.shdwfghtr.asset.InventoryService;
import com.shdwfghtr.asset.TextureAtlasService;
import com.shdwfghtr.asset.UserInterfaceService;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.GdxGame;

public class SplashScreen implements Screen{
	private int numDots;
	private final BitmapFont font = new BitmapFont(Gdx.files.internal("skin/font-export.fnt"));
	private final SpriteBatch batch = new SpriteBatch();
	
	@Override
	public void render(float delta) {
		if(GdxGame.assetService.update()) {
			GdxGame.textureAtlasService = new TextureAtlasService(GdxGame.assetService);
			GdxGame.uiService = new UserInterfaceService();
			GdxGame.audioService = new AudioService(GdxGame.assetService);
			DataService.load(Player.CURRENT);
			InventoryService.initialize();
            numDots++;

			//Changes the screen to be the main menu
            ((GdxGame) Gdx.app.getApplicationListener()).setScreen(new TravelScreen());
		}

		GlyphLayout glyphLayout = new GlyphLayout();
		glyphLayout.setText(font, "Loading");
		numDots = Math.round(GdxGame.assetService.getProgress() * 3);
		batch.begin();
		font.draw(batch, "Loading", 10, glyphLayout.height + 10);
		for(int i=0; i<numDots; i++)
			font.draw(batch, ".", (glyphLayout.width + 4) + i*10, glyphLayout.height + 10);
		batch.end();
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void show() {
		Gdx.input.setCatchKey(82, true); //82 is MENU keycode
		Gdx.input.setCatchKey(4, true); //4 is BACK keycode
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
