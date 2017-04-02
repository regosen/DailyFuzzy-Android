package com.regosen.dailyfuzzy;

import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;

import io.fabric.sdk.android.Fabric;

public class DailyFuzzyApp extends Application implements Application.ActivityLifecycleCallbacks {
    private static Activity sCurrentActivity = null;

    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FacebookSdk.sdkInitialize(getApplicationContext());
        registerActivityLifecycleCallbacks(this);
    }

    public void onActivityCreated(Activity activity, Bundle bundle) {
        sCurrentActivity = activity;
    }

    public void onActivityDestroyed(Activity activity) {
        sCurrentActivity = null;
    }

    public void onActivityPaused(Activity activity) {
        sCurrentActivity = null;
    }

    public void onActivityResumed(Activity activity) {
        sCurrentActivity = activity;
    }

    public void onActivitySaveInstanceState(Activity activity,
                                            Bundle outState) {
        Log.e("","onActivitySaveInstanceState:" + activity.getLocalClassName());
    }

    public void onActivityStarted(Activity activity) {
        Log.e("","onActivityStarted:" + activity.getLocalClassName());
    }

    public void onActivityStopped(Activity activity) {
        Log.e("","onActivityStopped:" + activity.getLocalClassName());
    }

    // So we can get resources from non-activity classes like PostFetcher
    public static Resources getAppResources() { return sCurrentActivity.getResources(); }
  }