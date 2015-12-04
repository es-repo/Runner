package com.a1.runner;

public interface IAdsController {

    void showInterstitialAd ();
    void requestInterstitialAdLoading(EventHandler onLoaded);

    void showBannerAd();
    void hideBannerAd();
}
