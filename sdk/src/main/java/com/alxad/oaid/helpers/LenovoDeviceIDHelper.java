package com.alxad.oaid.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.alxad.oaid.interfaces.AlxLenovoIDInterface;


/****************************
 * on 2019/10/29
 * 获取联想 OAID
 ****************************
 */
public class LenovoDeviceIDHelper {

  private Context mContext;
  AlxLenovoIDInterface lenovoIDInterface;

  public LenovoDeviceIDHelper(Context ctx) {
    mContext = ctx;
  }

  public void getIdRun(AlxDevicesIDsHelper.AppIdsUpdater _listener) {
    String result = null;
    String pkgName = mContext.getPackageName();
    Intent intent = new Intent();
    intent.setClassName("com.zui.deviceidservice", "com.zui.deviceidservice.DeviceidService");
    boolean seu = mContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    if (seu) {
      if (lenovoIDInterface != null) {
        String oaid = lenovoIDInterface.a();
        String udid = lenovoIDInterface.b();
        String aaid = lenovoIDInterface.b(pkgName);
        String vaid = lenovoIDInterface.b(pkgName);

        if (_listener != null) {
          _listener.OnIdsAvalid(oaid);
        }
      }
    }
  }

  ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      lenovoIDInterface = new AlxLenovoIDInterface.len_up.len_down(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
  };
}
