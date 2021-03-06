package com.gmail.andrewahughes.match.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gmail.andrewahughes.match.MyGdxGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = MyGdxGame.HEIGHT;
		config.width = MyGdxGame.WIDTH;
		new LwjglApplication(new MyGdxGame(), config);
	}
}
