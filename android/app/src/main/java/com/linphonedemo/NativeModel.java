package com.linphonedemo;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class NativeModel extends ReactContextBaseJavaModule {

    private ReactApplicationContext mContext;

    public NativeModel(@NonNull ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "RNTONATIVE";
    }

    @ReactMethod
    public void startActivity() {
        Intent intent = new Intent(mContext,CallActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
