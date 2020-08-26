package com.example.testapp;

import android.app.Service;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.List;

public class Location extends Service {
    public Location() {
    }

    public final static String ATTITUDE_DATA =
            "com.example.testapp.ATTITUDE_DATA";
    public final static String SERVICE_LOCATION =
            "com.example.testapp.SERVICE_LOCATION";
    public final static String TO_MAIN =
            "com.example.testapp.TO_MAIN";




    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.INTERVAL_DATA);
        intentFilter.addAction(BLEService.ACC_DATA);
        intentFilter.addAction(BLEService.MAGNETIC_DATA);
        intentFilter.addAction(BLEService.GYRO_DATA);
        return intentFilter;
    }

    private void update(){
        Calc.MadgwickAHRSupdate();
        Calc.quaternionToMatrix();
        Calc.vupdate();
        Calc.pupdate();
    }

    // broadcast receiver for BLE service intents
    private final BroadcastReceiver Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case BLEService.INTERVAL_DATA: {
                    byte intervalarr[] = intent.getByteArrayExtra(BLEService.INTERVAL_DATA);
                    Calc.interval = Utils.ByteToFloat(intervalarr,0)*0.001f;
                    update();

                    Intent toMain = new Intent(TO_MAIN);
                    sendBroadcast(toMain);
                    break;
                }
                case BLEService.ACC_DATA: {
                    byte accRaws[] = intent.getByteArrayExtra(BLEService.ACC_DATA);
                    Calc.ax = Utils.ByteToFloat(accRaws, 0);
                    Calc.ay = Utils.ByteToFloat(accRaws, 4);
                    Calc.az = Utils.ByteToFloat(accRaws, 8);
                    break;
                }
                case BLEService.GYRO_DATA: {
                    byte gyroRaws[] = intent.getByteArrayExtra(BLEService.GYRO_DATA);
                    Calc.gx = Utils.ByteToFloat(gyroRaws, 0);
                    Calc.gy = Utils.ByteToFloat(gyroRaws, 4);
                    Calc.gz = Utils.ByteToFloat(gyroRaws, 8);
                    break;
                }
                case BLEService.MAGNETIC_DATA: {
                    byte magRaws[] = intent.getByteArrayExtra(BLEService.MAGNETIC_DATA);
                    Calc.mx = Utils.ByteToFloat(magRaws, 0);
                    Calc.my = Utils.ByteToFloat(magRaws, 4);
                    Calc.mz = Utils.ByteToFloat(magRaws, 8);
                    break;
                }

            }
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Calc.delX = Calc.delY = Calc.delZ = 0.0f;
        Calc.vx = Calc.vy = Calc.vz = 0.0f;

        this.registerReceiver(Receiver, makeIntentFilter());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(Receiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
