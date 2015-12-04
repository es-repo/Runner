package com.a1.runner.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.a1.runner.IAnalyticsNotifier;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;

import com.a1.runner.ApplicationController;
import com.a1.runner.EventHandler;
import com.a1.runner.GameServices;
import com.a1.runner.IAdsController;
import com.a1.runner.RunnerGame;

public class AndroidLauncher extends AndroidApplication implements IAdsController, IAnalyticsNotifier,
		ApplicationController, GameHelper.GameHelperListener, GameServices {

	Tracker analyticsTracker;

	InterstitialAd interstitialAd;
	AdView bannerAdView;
	GameHelper gameHelper;
	boolean gameServicesEnabled = true;
	boolean showLeaderboardAfterLogging;
	int bestScore;
	boolean submitBestScore;
	boolean bannerAdEnabled = true;
	
	EventHandler onLoginSucceed;
	EventHandler onLoginFailed;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// запретить отключение экрана без использования дополнительных
		// разрешений (меньше разрешений – больше доверие к приложению)
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		RelativeLayout layout = new RelativeLayout(this);
		layout.addView(createGameView());

		RelativeLayout.LayoutParams adParams =
				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		bannerAdView = createAdView();
		layout.addView(bannerAdView, adParams);

		setContentView(layout);

		initGameServices();
		initInterstitialAds();

		RunnerApplication app = (RunnerApplication) getApplication();
		analyticsTracker = app.getAnalyticsTracker();
	}

	synchronized private Tracker getAnalyticsTrackerTracker() {
		if (analyticsTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			analyticsTracker = analytics.newTracker(R.xml.global_tracker);
		}
		return analyticsTracker;
	}

	private View createGameView(){
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;

		View view = initializeForView(new RunnerGame(this, this, this, this), config);
		return view;
	}

	private AdView createAdView(){
		AdView adView = new AdView(this);
		adView.setAdSize(AdSize.BANNER);
		String adId = getResources().getString(R.string.banner_ad_unit_id);
		adView.setAdUnitId(adId);
		adView.setBackgroundColor(Color.TRANSPARENT);
		final AndroidLauncher al = this;
		adView.setAdListener(new AdListener() {
			@Override
			public void onAdOpened() {
				al.setScreenName("banner_ad_clicked");
			}
		});

		return adView;
	}

	@Override
	protected void onStart() {
		super.onStart();
		gameHelper.onStart(this);
		try{
			GoogleAnalytics.getInstance(this).reportActivityStart(this);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		gameHelper.onStop();
		try{
			GoogleAnalytics.getInstance(this).reportActivityStop(this);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	private void initGameServices(){
		// CLIENT_ALL указывет на использование API всех клиентов
		gameHelper = new GameHelper(this, GameHelper.CLIENT_ALL);
		// выключить автоматический вход при запуске игры
		gameHelper.setConnectOnStart(false);
		//gameHelper.enableDebugLog(true);
		gameHelper.setup(this);
	}

	private boolean isOnline() {
		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			return netInfo != null && netInfo.isConnectedOrConnecting();
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void initInterstitialAds() {
		interstitialAd = new InterstitialAd(getApplication());
		String adId = getResources().getString(R.string.interstitial_ad_unit_id);
		interstitialAd.setAdUnitId(adId);
	}

	@Override
	public void showInterstitialAd() {

		if (!isOnline())
			return;

		final AndroidLauncher al = this;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (interstitialAd.isLoaded()) {
						interstitialAd.show();
						al.setScreenName("interstitial_ad");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void showBannerAd(){
		if (!isOnline())
			return;

		if (bannerAdEnabled) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					bannerAdView.setEnabled(true);
					bannerAdView.setVisibility(View.VISIBLE);
					try {
						if (!bannerAdView.isLoading()) {
							AdRequest adRequest = new AdRequest.Builder().build();
							bannerAdView.loadAd(adRequest);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void hideBannerAd(){

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				bannerAdView.setEnabled(true);
				bannerAdView.setVisibility(View.VISIBLE);
				try {
					bannerAdView.setEnabled(false);
					bannerAdView.setVisibility(View.GONE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void requestInterstitialAdLoading(final EventHandler onLoaded){

		if (!isOnline())
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
						AdRequest ad = builder.build();
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
	public void login(final EventHandler onLoginSucceed, final EventHandler onLoginFailed) {		
		
		if (!getGameServicesEnabled())
			return;
		
		this.onLoginFailed = onLoginFailed;
		this.onLoginSucceed = onLoginSucceed;
		final AndroidLauncher ad = this;

		try {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					try{
						gameHelper.beginUserInitiatedSignIn();
					}
					catch (Exception e){
						if (ad.onLoginFailed != null) {
							ad.onLoginFailed.action(0);
						}
						ad.onLoginFailed = null;
						ad.onLoginSucceed = null;
						e.printStackTrace();
					}

				}
			});
		} catch (Exception e) {
			if (ad.onLoginFailed != null) {
				ad.onLoginFailed.action(0);
			}
			ad.onLoginFailed = null;
			ad.onLoginSucceed = null;
			e.printStackTrace();
		}
	}

	@Override
	public void login(int bestScore, boolean submitBestScore, boolean showLeaderboardAfterLogging,
					  final EventHandler onLoginSucceed, final EventHandler onLoginFailed){
		this.showLeaderboardAfterLogging = true;
		this.bestScore = bestScore;
		this.submitBestScore = submitBestScore;
		login(onLoginSucceed, onLoginFailed);
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
			startActivityForResult(Games.Leaderboards.getLeaderboardIntent(gameHelper.getApiClient(), leaderBoardId), 100);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSignInSucceeded() {

		if (onLoginFailed != null) {
			onLoginSucceed.action(0);
		}
		this.onLoginFailed = null;
		this.onLoginSucceed = null;

		if (showLeaderboardAfterLogging) {
			showLeaderboardAfterLogging = false;
			if (submitBestScore) {
				submitScore(bestScore);
				submitBestScore = false;
			}
			showLeaderboard();
		}
	}

	@Override
	public void onSignInFailed() {
		showLeaderboardAfterLogging = false;
		if (onLoginFailed != null) {
			onLoginFailed.action(0);
		}
		this.onLoginFailed = null;
		this.onLoginSucceed = null;
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

	public void setScreenName(String name){
		try {
			analyticsTracker.setScreenName(name);
			analyticsTracker.send(new HitBuilders.AppViewBuilder().build());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
