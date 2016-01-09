package com.a1.runner;

public interface GameServices {

    boolean getSignedIn();

    void login(final EventHandler onLoginSucceed, final EventHandler onLoginFailed);

    void login(int bestScore, boolean submitBestScore, boolean showLeaderboardAfterLogging,
               final EventHandler onLoginSucceed, final EventHandler onLoginFailed, final EventHandler onScoreSubmitted);

    boolean submitScore(int score);

    void showLeaderboard();

    void setGameServicesEnabled(boolean v);

    boolean getGameServicesEnabled();
}
