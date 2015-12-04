package com.a1.runner;

public interface GameServices {

    boolean getSignedIn();

    void login();

    void login(int bestScore, boolean submitBestScore, boolean showLeaderboardAfterLogging);

    void submitScore(int score);

    void showLeaderboard();

    void setGameServicesEnabled(boolean v);

    boolean getGameServicesEnabled();
}
