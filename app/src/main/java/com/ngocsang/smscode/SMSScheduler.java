package com.ngocsang.smscode;

import android.app.Application;
import android.content.Context;


public class SMSScheduler extends Application {
    private static Context context;
    private boolean applicationInForeground = false;

    public void onCreate() {
        super.onCreate();
        SMSScheduler.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return SMSScheduler.context;
    }

    public boolean isApplicationInForeground() {
        return applicationInForeground;
    }

    public void setApplicationInForeground(boolean applicationInForeground) {
        this.applicationInForeground = applicationInForeground;
    }
}
