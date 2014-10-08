package com.doubi.exception;

import android.app.Application;

public class MyApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		CustomCrashHandler mCustomCrashHandler = CustomCrashHandler.getInstance();  
        mCustomCrashHandler.setCustomCrashHanler(getApplicationContext());
	}
}
