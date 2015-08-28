package com.a1.runner.android;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.WindowManager;

import com.a1.runner.ApplicationController;
import com.a1.runner.GameServices;
import com.a1.runner.IAdsController;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.a1.runner.RunnerGame;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;

public class AndroidLauncher extends AndroidApplication implements IAdsController, ApplicationController, GameHelper.GameHelperListener, GameServices {

	private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-5519384153835422/6795093799";

	InterstitialAd interstitialAd;
	GameHelper gameHelper;
	boolean gameServicesEnabled = true;
	boolean showLeaderboardAfterLogging;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// CLIENT_ALL указывет на использование API всех клиентов
		gameHelper = new GameHelper(this, GameHelper.CLIENT_ALL);
		// выключить автоматический вход при запуске игры
		gameHelper.setConnectOnStart(false);
		gameHelper.enableDebugLog(true);
		// запретить отключение экрана без использования дополнительных
		// разрешений (меньше разрешений – больше доверие к приложению)
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;
		initialize(new RunnerGame(this, this, this), config);

		gameHelper.setup(this);

		setupAds();
	}

	@Override
	protected void onStart() {
		super.onStart();
		gameHelper.onStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		gameHelper.onStop();
	}

	public void setupAds() {
//		interstitialAd = new InterstitialAd(this);
//		interstitialAd.setAdUnitId(INTERSTITIAL_AD_UNIT_ID);
//		AdRequest.Builder builder = new AdRequest.Builder();
//		AdRequest ad = builder.build();
//		interstitialAd.loadAd(ad);
	}

	private boolean isWifiConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		return (ni != null && ni.isConnected());
	}

	@Override
	public boolean showInterstitialAd(final Runnable then) {

		if (!isWifiConnected())
			return false;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if (then != null) {
					interstitialAd.setAdListener(new AdListener() {
						@Override
						public void onAdClosed() {
							Gdx.app.postRunnable(then);
							AdRequest.Builder builder = new AdRequest.Builder();
							AdRequest ad = builder.build();
							interstitialAd.loadAd(ad);
						}
					});
				}
				interstitialAd.show();
			}
		});

		return true;
	}

	@Override
	public void killApp() {
		finish();
		System.exit(0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// здесь gameHelper принимает решение о подключении, переподключении или
		// отключении от игровых сервисов, в зависимости от кода результата
		// Activity
		gameHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean getSignedIn() {

		if (!getGameServicesEnabled())
			return false;

		// статус подключения
		return gameHelper.isSignedIn();
	}

	@Override
	public void login() {

		if (!getGameServicesEnabled())
			return;

		try {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// инициировать вход пользователя. Может быть вызван диалог
					// входа. Выполняется в UI-потоке
					try{
						gameHelper.beginUserInitiatedSignIn();
					}
					catch (Exception e){
						e.printStackTrace();
					}

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void login(boolean showLeaderboardAfterLogging){
		this.showLeaderboardAfterLogging = true;
		login();
	}

	@Override
	public void submitScore(int score) {

		if (!getGameServicesEnabled())
			return;

		try {
			Games.Leaderboards.submitScore(gameHelper.getApiClient(), "CgkI9Ke0jowDEAIQAQ", score);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unlockAchievement(String achievementId) {
		// открыть достижение с ID achievementId
		Games.Achievements.unlock(gameHelper.getApiClient(), achievementId);

	}

	@Override
	public void showLeaderboard() {

		if (!getGameServicesEnabled())
			return;

		try {
			startActivityForResult(	Games.Leaderboards.getLeaderboardIntent(gameHelper.getApiClient(), "CgkI9Ke0jowDEAIQAQ"), 100);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getAchievements() {

		if (!getGameServicesEnabled())
			return;

		// вызвать Activity с достижениями
		startActivityForResult(
				Games.Achievements.getAchievementsIntent(gameHelper
						.getApiClient()), 101);

	}

	@Override
	public void onSignInSucceeded() {
		if (showLeaderboardAfterLogging) {
			showLeaderboardAfterLogging = false;
			showLeaderboard();
		}
	}

	@Override
	public void onSignInFailed() {
		showLeaderboardAfterLogging = false;
	}

	@Override
	public boolean getGameServicesEnabled(){
		return gameServicesEnabled;
	}

	@Override
	public void setGameServicesEnabled(boolean v){
		gameServicesEnabled = v;
	}
}
