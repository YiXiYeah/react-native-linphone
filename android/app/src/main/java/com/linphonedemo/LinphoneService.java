package com.linphonedemo;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.LogCollectionState;
import org.linphone.mediastream.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class LinphoneService extends Service {
    private static final String START_LINPHONE_LOGS = " ==== Device information dump ====";
    private static LinphoneService sInstance;

    private Handler mHandler;
    private Timer mTimer;

    private Core mCore;
    private CoreListenerStub mCoreListener;

    public static boolean isReady() {
        return sInstance != null;
    }

    public static LinphoneService getInstance() {
        return sInstance;
    }

    public static Core getCore() {
        return sInstance.mCore;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String basePath = getFilesDir().getAbsolutePath();
        Log.i("MJXZ_APP", "basePath is :" + basePath);
        Factory.instance().setLogCollectionPath(basePath);
        Factory.instance().enableLogCollection(LogCollectionState.Enabled);
        Factory.instance().setDebugMode(true, getString(R.string.app_name));
        dumpDeviceInformation();
        dumpInstalledLinphoneInformation();

        mHandler = new Handler();
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                Toast.makeText(LinphoneService.this, message, Toast.LENGTH_SHORT).show();
                if (state == Call.State.IncomingReceived) {
                    Toast.makeText(LinphoneService.this, "Incoming call received, answering it automatically", Toast.LENGTH_LONG).show();
                    CallParams params = getCore().createCallParams(call);
                    params.enableVideo(true);
                    call.acceptWithParams(params);
                } else if (state == Call.State.Connected) {
                    //TODO  通话接通，通知界面显示视频

                }
            }
        };
        try {
            copyIfNotExist(R.raw.linphonerc_default, basePath + "/.linphonerc");
            copyFromPackage(R.raw.linphonerc_factory, "linphonerc");
        } catch (IOException ioe) {
            Log.i("MJXZ_APP", "IOException is :" + ioe);
        }
        Log.i("MJXZ_APP", "IOException is :==========>");
        mCore = Factory.instance()
                .createCore(basePath + "/.linphonerc", basePath + "/linphonerc", this);

        mCore.addListener(mCoreListener);
        configureCore();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (sInstance != null) {
            return START_STICKY;
        }
        sInstance = this;
//        mCore.start();
        TimerTask lTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(() -> {
                    if (mCore != null) {
                        mCore.iterate();
                    }
                });
            }
        };
        mTimer = new Timer("Linphone scheduler");
        mTimer.schedule(lTask, 0, 20);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mCore.removeListener(mCoreListener);
        mTimer.cancel();
        mCore.stop();
        mCore = null;
        sInstance = null;

        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();

        super.onTaskRemoved(rootIntent);
    }

    private void configureCore() {
        String basePath = getFilesDir().getAbsolutePath();
        String userCerts = basePath + "/user-certs";
        File f = new File(userCerts);
        if (!f.exists()) {
            if (!f.mkdir()) {

            }
        }
        mCore.setUserCertificatesPath(userCerts);
    }

    private void dumpDeviceInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("DEVICE=").append(Build.DEVICE).append("\n");
        sb.append("MODEL=").append(Build.MODEL).append("\n");
        sb.append("MANUFACTURER=").append(Build.MANUFACTURER).append("\n");
        sb.append("SDK=").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Supported ABIs=");
        for (String abi : Version.getCpuAbis()) {
            sb.append(abi).append(", ");
        }
        sb.append("\n");

    }

    private void dumpInstalledLinphoneInformation() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException nnfe) {

        }

        if (info != null) {
            Log.i("MJXZ_APP", "[Service] Linphone version is " + info.versionName + " (" + info.versionCode + ")");
        } else {
            Log.i("MJXZ_APP", "[Service] Linphone version is unknown");
        }
    }

    private void copyIfNotExist(int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(ressourceId, lFileToCopy.getName());
        }
    }

    private void copyFromPackage(int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = openFileOutput(target, 0);
        InputStream lInputStream = getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }
}
