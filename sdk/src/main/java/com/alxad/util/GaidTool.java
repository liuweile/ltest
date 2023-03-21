package com.alxad.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;

import com.alxad.config.AlxConst;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by huangweiwu on 2018/5/16.
 */

public class GaidTool {
    public static final class AdInfo {
        private final String advertisingId;
        private final boolean limitAdTrackingEnabled;

        AdInfo(String advertisingId, boolean limitAdTrackingEnabled) {
            this.advertisingId = advertisingId;
            this.limitAdTrackingEnabled = limitAdTrackingEnabled;
        }

        public String getId() {
            return this.advertisingId;
        }

        public boolean isLimitAdTrackingEnabled() {
            return this.limitAdTrackingEnabled;
        }
    }

    public static AdInfo getAdvertisingIdInfo(Context context) throws Exception {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException(
                    "Cannot be called from the main thread");
        }

        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(AlxConst.GOOGLE_PLAY_APP_PACKAGE_NAME, 0);
        } catch (Exception e) {
            e.printStackTrace();
            // throw e;
        }

        AdvertisingConnection connection = new AdvertisingConnection();
        Intent intent = new Intent(
                "com.google.android.gms.ads.identifier.service.START");
        intent.setPackage("com.google.android.gms");
        try{
            if (context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
                try {
                    AdvertisingInterface adInterface = new AdvertisingInterface(
                            connection.getBinder());
                    AdInfo adInfo = new AdInfo(adInterface.getId(),
                            adInterface.isLimitAdTrackingEnabled(true));
                    return adInfo;
                } catch (Exception exception) {
                    throw exception;
                } finally {
                    context.unbindService(connection);
                }
            }
        }catch (Exception e){
            throw e;
        }
        throw new IOException("Google Play connection failed");
    }

    private static final class AdvertisingConnection implements
            ServiceConnection {
        boolean retrieved = false;
        private final LinkedBlockingQueue<IBinder> queue = new LinkedBlockingQueue<IBinder>(
                1);

        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                this.queue.put(service);
            } catch (InterruptedException localInterruptedException) {
                localInterruptedException.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
        }

        public IBinder getBinder() throws InterruptedException {
            if (this.retrieved) {
                throw new IllegalStateException();
            }
            this.retrieved = true;
            return (IBinder) this.queue.take();
        }
    }

    private static final class AdvertisingInterface implements IInterface {
        private IBinder binder;

        public AdvertisingInterface(IBinder pBinder) {
            binder = pBinder;
        }

        public IBinder asBinder() {
            return binder;
        }

        public String getId() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            String id;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                binder.transact(1, data, reply, 0);
                reply.readException();
                id = reply.readString();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return id;
        }

        public boolean isLimitAdTrackingEnabled(boolean paramBoolean)
                throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            boolean limitAdTracking;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                data.writeInt(paramBoolean ? 1 : 0);
                binder.transact(2, data, reply, 0);
                reply.readException();
                limitAdTracking = 0 != reply.readInt();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return limitAdTracking;
        }
    }
}