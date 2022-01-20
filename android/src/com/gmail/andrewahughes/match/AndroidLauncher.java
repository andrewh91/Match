package com.gmail.andrewahughes.match;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.gmail.andrewahughes.match.MyGdxGame;
import de.golfgl.gdxgamesvcs.GpgsClient;
import de.golfgl.gdxgamesvcs.IGameServiceIdMapper;

public class AndroidLauncher extends AndroidApplication {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		MyGdxGame game = new MyGdxGame();
		game.gsClient = new GpgsClient(){
			@Override
			public boolean submitEvent(String eventId, int increment) {
				return super.submitEvent(com.gmail.andrewahughes.match.GpgsMappers.mapToGpgsEvent(eventId), increment);
			}
		}.setGpgsAchievementIdMapper(new IGameServiceIdMapper<String>() {
			@Override
			public String mapToGsId(String independantId) {
				return com.gmail.andrewahughes.match.GpgsMappers.mapToGpgsAchievement(independantId);
			}
		}).setGpgsLeaderboardIdMapper(new IGameServiceIdMapper<String>() {
			@Override
			public String mapToGsId(String independantId) {
				return com.gmail.andrewahughes.match.GpgsMappers.mapToGpgsLeaderboard(independantId);
			}
		}).initialize(this, true);
		initialize(game, config);

	}
}
