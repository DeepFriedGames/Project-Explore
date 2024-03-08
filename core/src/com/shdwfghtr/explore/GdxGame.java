package com.shdwfghtr.explore;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Rectangle;
import com.shdwfghtr.asset.AssetManagerService;
import com.shdwfghtr.asset.AudioService;
import com.shdwfghtr.asset.ControllerService;
import com.shdwfghtr.asset.OptionsService;
import com.shdwfghtr.asset.PaletteService;
import com.shdwfghtr.asset.ParticleService;
import com.shdwfghtr.asset.TextureAtlasService;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.asset.UserInterfaceService;
import com.shdwfghtr.screens.GameScreen;
import com.shdwfghtr.screens.Splash;

@SuppressWarnings("ALL")
public class GdxGame extends com.badlogic.gdx.Game {
    private static ActionResolver ACTION_RESOLVER;
	public static AssetManagerService assetService;
	public static TextureAtlasService textureAtlasService;
	public static UserInterfaceService uiService;
	public static AudioService audioService;
	public static ParticleService particleService;
	public static Color fadeColor = Color.BLACK;

	private GLProfiler profiler;
//  private float tickPeriod = 1/60f; //seconds per frame
//	private double accumulator = 0;

	public GdxGame(ActionResolver actionResolver) {
		ACTION_RESOLVER = actionResolver;
	}

	public static GameCamera getCamera() {
		if(Gdx.app.getApplicationListener() instanceof GdxGame){
			GdxGame game = (GdxGame) Gdx.app.getApplicationListener();
			if(game.getScreen() instanceof GameScreen){
				GameScreen screen = (GameScreen) game.getScreen();
				return screen.camera;
			}
		}
		return new GameCamera();
	}

	@Override
	public void create () {
		//Generates the loading screen which will load all assets asynchronously
		this.assetService = new AssetManagerService();
		this.particleService = new ParticleService();

		if(OptionsService.IsFullScreen())
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

		setScreen(new Splash());
		profiler = new GLProfiler(Gdx.graphics);
		profiler.enable();
	}

	@Override
	public void render () {
//		accumulator += Gdx.graphics.getRawDeltaTime();
//		if (accumulator >= tickPeriod) {
		//waits to update frames every 1/60th of a second
		float delta = Gdx.graphics.getRawDeltaTime();
		//update the game clock
		TimeService.Update(delta);
		if(uiService != null) uiService.update(delta);

		//clear the display with blackness that slowly fades to world Color
		//why? because it's cool, that's why.
		float r, g, b;
		r = (float) Math.sin(Math.toRadians(TimeService.GetTime() * 10)) * fadeColor.r / 10;
		g = (float) Math.sin(Math.toRadians(TimeService.GetTime() * 10)) * fadeColor.g / 10;
		b = (float) Math.sin(Math.toRadians(TimeService.GetTime() * 10)) * fadeColor.b / 10;
		Gdx.gl.glClearColor(r, g, b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//draws the current screen to the display
		getScreen().render(delta); //tickPeriod);
		if(audioService != null) audioService.update(delta);//tickPeriod);
//
//			System.out.println(Gdx.graphics.isContinuousRendering() + ", " + ( 1 / Gdx.graphics.getRawDeltaTime()));
//			System.out.println("texture bindings: " + GLProfiler.textureBindings);
		profiler.reset();
//			accumulator -= tickPeriod;
	}
//	}

	@Override
	public void dispose() {
		super.dispose();
		uiService.dispose();
		assetService.dispose();
	}
}
