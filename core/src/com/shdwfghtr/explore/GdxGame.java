package com.shdwfghtr.explore;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.shdwfghtr.asset.AssetManagerService;
import com.shdwfghtr.asset.AudioService;
import com.shdwfghtr.asset.ControllerService;
import com.shdwfghtr.asset.OptionsService;
import com.shdwfghtr.asset.ParticleService;
import com.shdwfghtr.asset.TextureAtlasService;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.asset.UserInterfaceService;
import com.shdwfghtr.screens.GameScreen;
import com.shdwfghtr.screens.SplashScreen;
import com.shdwfghtr.ui.GoToScreenAction;

@SuppressWarnings("ALL")
public class GdxGame extends com.badlogic.gdx.Game {
	public static final int WIDTH = 960;
	public static final int HEIGHT = 540;
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
		try {
			if(Gdx.app.getApplicationListener() instanceof GdxGame){
				GdxGame game = (GdxGame) Gdx.app.getApplicationListener();
				if(game.getScreen() instanceof GameScreen){
					GameScreen screen = (GameScreen) game.getScreen();
					return screen.camera;
				}
			}
		} catch (Exception ex){
			System.out.println("Bad getCamera call");
		}
		return new GameCamera(0, 0);
	}

	public static void goToScreen(final Screen screen){
		ApplicationListener app = Gdx.app.getApplicationListener();
		if(app instanceof Game){
			final Game game = (Game) app;
			GdxGame.uiService.getStage().addAction(Actions.sequence(
					Actions.fadeOut(0.5f)
					, new GoToScreenAction(game, screen)
					, Actions.fadeIn(0.5f)
			));
		}
	}

	@Override
	public void create () {
		//Generates the loading screen which will load all assets asynchronously
		this.assetService = new AssetManagerService();
		this.particleService = new ParticleService();
		ControllerService.initializeController();

		if(OptionsService.IsFullScreen())
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

		setScreen(new SplashScreen());
		profiler = new GLProfiler(Gdx.graphics);
		profiler.enable();
	}

	@Override
	public void render () {
		//update the game clock
		float delta = Gdx.graphics.getRawDeltaTime();
		TimeService.Update(delta);

		if(audioService != null) audioService.update(delta);

		//clear the display with blackness that slowly fades to world Color
		//why? because it's cool, that's why.
		float r = MathUtils.sin(TimeService.GetTime() % MathUtils.PI2) * fadeColor.r / 25f;
		float g = MathUtils.sin(TimeService.GetTime() % MathUtils.PI2) * fadeColor.g / 25f;
		float b = MathUtils.sin(TimeService.GetTime() % MathUtils.PI2) * fadeColor.b / 25f;
		Gdx.gl.glClearColor(r, g, b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//draws the current screen to the display
		getScreen().render(delta);
		if(particleService != null && OptionsService.AreParticlesEnabled()) {
			particleService.update(delta);
			particleService.draw();
		}
		if(uiService != null) {
			uiService.update(delta);
			uiService.draw();
		}
		profiler.reset();
	}

	@Override
	public void dispose() {
		super.dispose();
		uiService.dispose();
		assetService.dispose();
	}
}
