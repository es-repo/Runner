package com.a1.runner.android;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;

import com.a1.runner.ApplicationController;
import com.a1.runner.EventHandler;
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

	private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8413752460507538/7397472406";

	InterstitialAd interstitialAd;
	GameHelper gameHelper;
	boolean gameServicesEnabled = true;
	boolean showLeaderboardAfterLogging;
	int bestScore;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setupAds();

		// CLIENT_ALL указывет на использование API всех клиентов
		gameHelper = new GameHelper(this, GameHelper.CLIENT_ALL);
		// выключить автоматический вход при запуске игры
		gameHelper.setConnectOnStart(false);
		//gameHelper.enableDebugLog(true);
		// запретить отключение экрана без использования дополнительных
		// разрешений (меньше разрешений – больше доверие к приложению)
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;
		initialize(new RunnerGame(this, this, this), config);

		gameHelper.setup(this);
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
		interstitialAd = new InterstitialAd(getApplication());
		interstitialAd.setAdUnitId(INTERSTITIAL_AD_UNIT_ID);
	}

	private boolean isWifiConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return (ni != null && ni.isConnected());
	}

	@Override
	public void showInterstitialAd() {

		if (!isWifiConnected())
			return;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (interstitialAd.isLoaded()) {
						interstitialAd.show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void requestInterstitialAdLoading(final EventHandler onLoaded){

		if (!isWifiConnected())
			onLoaded.action(-1);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {

					interstitialAd.setAdListener(new AdListener() {
						@Override
						public void onAdFailedToLoad(int errorCode) {
							onLoaded.action(-1);
						}

						@Override
						public void onAdLoaded() {
							onLoaded.action(0);
						}
					});

					if (!interstitialAd.isLoaded()) {
						AdRequest.Builder builder = new AdRequest.Builder();
						AdRequest ad = builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
						interstitialAd.loadAd(ad);
					} else {
						onLoaded.action(0);
					}
				} catch (Exception e) {
					e.printStackTrace();
					onLoaded.action(-1);
				}
			}
		});
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
	public void login(int bestScore, boolean showLeaderboardAfterLogging){
		this.showLeaderboardAfterLogging = true;
		this.bestScore = bestScore;
		login();
	}

	@Override
	public void submitScore(int score) {

		if (!getGameServicesEnabled())
			return;

		try {
			String leaderBoardId = getResources().getString(R.string.leaderboard_top_scores);
			Games.Leaderboards.submitScore(gameHelper.getApiClient(),leaderBoardId, score);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void showLeaderboard() {

		if (!getGameServicesEnabled())
			return;

		try {
			String leaderBoardId = getResources().getString(R.string.leaderboard_top_scores);
			startActivityForResult(	Games.Leaderboards.getLeaderboardIntent(gameHelper.getApiClient(), leaderBoardId), 100);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSignInSucceeded() {
		if (showLeaderboardAfterLogging) {
			showLeaderboardAfterLogging = false;
			submitScore(bestScore);
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

	public void openAppMarketPage()	{
		try {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String appPackageName = getPackageName();
					try {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
					} catch (android.content.ActivityNotFoundException anfe) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
					}
				}
			});
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}
