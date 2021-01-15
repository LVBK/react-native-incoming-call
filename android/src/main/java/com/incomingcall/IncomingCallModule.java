package com.incomingcall;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.WindowManager;
import android.content.Context;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

public class IncomingCallModule extends ReactContextBaseJavaModule {

    public static ReactApplicationContext reactContext;
    public static Activity mainActivity;
    private static IncomingCallModule sInstance = null;

    private static final String TAG = "RNIC:IncomingCallModule";
    private WritableMap headlessExtras;
    private Handler timeRhandler;
    private Runnable runnable;

    public IncomingCallModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        mainActivity = getCurrentActivity();
        sInstance = this;
    }

    public static IncomingCallModule getInstance() {
        return sInstance;
    }

    @Override
    public String getName() {
        return "IncomingCall";
    }

    @ReactMethod
    public void display(String uuid, String name, String avatar, String info, int timeout) {
        if (UnlockScreenActivity.active) {
            return;
        }
        if (reactContext != null) {
            Bundle bundle = new Bundle();
            bundle.putString("uuid", uuid);
            bundle.putString("name", name);
            bundle.putString("avatar", avatar);
            bundle.putString("info", info);
            Intent i = new Intent(reactContext, UnlockScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED +
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD +
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            
            i.putExtras(bundle);
            reactContext.startActivity(i);

            if (timeout > 0) {
                timeRhandler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // this code will be executed after timeout seconds
                        UnlockScreenActivity.dismissIncoming();
                    }
                };
                timeRhandler.postDelayed(runnable, timeout);
//                new Timer().schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        // this code will be executed after timeout seconds
//                        UnlockScreenActivity.dismissIncoming();
//                    }
//                }, timeout);
            }
        }
    }

    public void clearTimer() {
        if(timeRhandler != null && runnable != null) {
            timeRhandler.removeCallbacks(runnable);
        }
    }

    @ReactMethod
    public void dismiss() {
        // final Activity activity = reactContext.getCurrentActivity();

        // assert activity != null;
        clearTimer();
        UnlockScreenActivity.dismissIncoming();

        return;
    }

    private Context getAppContext() {
        return this.reactContext.getApplicationContext();
    }

    @ReactMethod
    public void backToForeground() {
        Context context = getAppContext();
        String packageName = context.getApplicationContext().getPackageName();
        Intent focusIntent = context.getPackageManager().getLaunchIntentForPackage(packageName).cloneFilter();
        Activity activity = getCurrentActivity();
        boolean isOpened = activity != null;
        Log.d(TAG, "backToForeground, app isOpened ?" + (isOpened ? "true" : "false"));

        if (isOpened) {
            focusIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(focusIntent);
        }
    }

    @ReactMethod
    public void openAppFromHeadlessMode(String uuid) {
        Context context = getAppContext();
        String packageName = context.getApplicationContext().getPackageName();
        Intent focusIntent = context.getPackageManager().getLaunchIntentForPackage(packageName).cloneFilter();
        Activity activity = getCurrentActivity();
        boolean isOpened = activity != null;

        if (!isOpened) {
            focusIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            final WritableMap response = new WritableNativeMap();
            response.putBoolean("isHeadless", true);
            response.putString("uuid", uuid);

            this.headlessExtras = response;

            getReactApplicationContext().startActivity(focusIntent);
        }
    }

    @ReactMethod
    public void getExtrasFromHeadlessMode(Promise promise) {
        if (this.headlessExtras != null) {
            promise.resolve(this.headlessExtras);

            this.headlessExtras = null;

            return;
        }

        promise.resolve(null);
    }
}
