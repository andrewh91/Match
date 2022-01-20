package com.gmail.andrewahughes.match;

import com.badlogic.gdx.Gdx;

public class GpgsMappers {
    public static String mapToGpgsLeaderboard(String leaderboardId) {
        String gpgsId = null;
        Gdx.app.log("MYLOG","trying to map leaderboard, arg="+leaderboardId);
        if (leaderboardId != null) {
            if (leaderboardId.equals(MyGdxGame.LEADERBOARD1))
                gpgsId = "CgkIm4atr4kVEAIQAQ";
                Gdx.app.log("MYLOG","map leaderboard, ="+gpgsId);
        }

        return gpgsId;
    }

    public static String mapToGpgsAchievement(String achievementId) {
        String gpgsId = null;

        if (achievementId != null) {
            if (achievementId.equals(MyGdxGame.ACHIEVEMENT1))
                gpgsId = "";
        }

        return gpgsId;
    }

    public static String mapToGpgsEvent(String eventId) {
        String gpgsId = null;

        if (eventId != null) {
            if (eventId.equals(MyGdxGame.EVENT1))
                gpgsId = "";
        }

        return gpgsId;
    }
}