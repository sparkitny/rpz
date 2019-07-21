package de.parkitny.fit.myfit.app.utils;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

public class SamsungService extends SAAgent {

    protected SamsungService(String s) {
        super(s);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onServiceConnectionResponse(SAPeerAgent saPeerAgent, SASocket saSocket, int i) {
        super.onServiceConnectionResponse(saPeerAgent, saSocket, i);
    }
}
