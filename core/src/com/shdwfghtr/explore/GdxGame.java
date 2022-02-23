package com.shdwfghtr.explore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.shdwfghtr.screens.Splash;

import java.awt.Desktop;

@SuppressWarnings("ALL")
public class GdxGame extends com.badlogic.gdx.Game {
	public Asset asset = null;
	private final Vector2 cursor = new Vector2();
	private static ActionResolver ACTION_RESOLVER;
	private GLProfiler profiler;
	public static Color fadeColor = Color.BLACK;
//    private float tickPeriod = 1/60f; //seconds per frame
//	private double accumulator = 0;

	public GdxGame(ActionResolver actionResolver) {
		ACTION_RESOLVER = actionResolver;
	}

	@Override
	public void create () {
		//Generates the loading screen which will load all assets asynchronously
		asset = new Asset();
		asset.load();

		if(Asset.CONTROLS.getBoolean("fullscreen", false))
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
		Asset.TIME += delta; //tickPeriod;

		//changes the cursor if it is over a touchable interface
		cursor.set(Gdx.input.getX(), Gdx.input.getY());
		cursor.set(Asset.getStage().screenToStageCoordinates(cursor));
		Actor hit = Asset.getStage().hit(cursor.x, cursor.y, true);
		if (hit != null && hit.isTouchable() && hit.isVisible())
			Asset.Cursors.setCurrent("hand");
		else
			Asset.Cursors.setCurrent("arrow");

		for(Timer t : Asset.getTimers())
			if(t.update() && Asset.TIMERS.contains(t)) Asset.TIMERS.remove(t);

		//clear the display with blackness that slowly fades to world Color
		//why? because it's cool, that's why.
		float r, g, b;
		r = (float) Math.sin(Math.toRadians(Asset.TIME * 10)) * fadeColor.r / 10;
		g = (float) Math.sin(Math.toRadians(Asset.TIME * 10)) * fadeColor.g / 10;
		b = (float) Math.sin(Math.toRadians(Asset.TIME * 10)) * fadeColor.b / 10;
		Gdx.gl.glClearColor(r, g, b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//draws the current screen to the display
		getScreen().render(delta); //tickPeriod);
		Asset.getMusicHandler().update(delta);//tickPeriod);
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
		asset.dispose();
	}
}
