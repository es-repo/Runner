package com.a1.runner.android;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class RunnerApplication extends Application {

    private Tracker analyticsTracker;

    synchronized public Tracker getAnalyticsTracker() {
        if (analyticsTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            analyticsTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return analyticsTracker;
    }
}
