package com.shdwfghtr.explore.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.shdwfghtr.explore.DesktopServices;
import com.shdwfghtr.explore.GdxGame;

class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = GdxGame.WIDTH;
		config.height = GdxGame.HEIGHT;
		new LwjglApplication(new GdxGame(new DesktopServices()), config);
	}
}
