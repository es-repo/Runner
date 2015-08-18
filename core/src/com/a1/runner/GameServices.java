package com.a1.runner;

public interface GameServices {

    boolean getSignedIn();

    void login();

    void login(boolean showLeaderboardAfterLogging);

    void submitScore(int score);

    void unlockAchievement(String achievementId);

    void showLeaderboard();

    void getAchievements();

    void setGameServicesEnabled(boolean v);

    boolean getGameServicesEnabled();
}
