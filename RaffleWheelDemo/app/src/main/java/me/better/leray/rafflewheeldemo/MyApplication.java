package me.better.leray.rafflewheeldemo;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by lanusiv on 2016/3/21.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
