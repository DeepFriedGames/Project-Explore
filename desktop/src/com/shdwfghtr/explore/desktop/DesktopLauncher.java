package com.shdwfghtr.explore.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.DesktopServices;
import com.shdwfghtr.explore.GdxGame;

class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		int scale = 3;
		config.width = Asset.CAM_WIDTH * scale;
		config.height = Asset.CAM_HEIGHT * scale;
		new LwjglApplication(new GdxGame(new DesktopServices()), config);
	}
}
