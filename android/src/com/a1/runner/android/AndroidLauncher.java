package com.a1.runner.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.a1.runner.IAdsController;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.a1.runner.RunnerGame;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class AndroidLauncher extends AndroidApplication implements IAdsController {

	private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-5519384153835422/6795093799";

	InterstitialAd interstitialAd;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;
		initialize(new RunnerGame(this), config);
		setupAds();
	}

	public void setupAds() {
		interstitialAd = new InterstitialAd(this);
		interstitialAd.setAdUnitId(INTERSTITIAL_AD_UNIT_ID);
		AdRequest.Builder builder = new AdRequest.Builder();
		AdRequest ad = builder.build();
		interstitialAd.loadAd(ad);
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

}
