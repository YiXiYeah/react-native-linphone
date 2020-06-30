package com.linphonedemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.facebook.react.ReactActivity;

import org.linphone.core.tools.Log;

import java.util.ArrayList;

public class MainActivity extends ReactActivity {

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "linphoneDemo";
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRequestCallPermissions();
    }

    private void checkAndRequestCallPermissions() {
        ArrayList<String> permissionsList = new ArrayList<>();

        // Some required permissions needs to be validated manually by the user
        // Here we ask for record audio and camera to be able to make video calls with sound
        // Once granted we don't have to ask them again, but if denied we can
        int recordAudio =
                getPackageManager()
                        .checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName());
        int camera =
                getPackageManager().checkPermission(Manifest.permission.CAMERA, getPackageName());

        if (recordAudio != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.RECORD_AUDIO);
        }

        if (camera != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.CAMERA);
        }

        permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionsList.size() > 0) {
            String[] permissions = new String[permissionsList.size()];
            permissions = permissionsList.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

}
