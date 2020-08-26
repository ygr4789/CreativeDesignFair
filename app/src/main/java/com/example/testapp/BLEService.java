package com.example.testapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BLEService extends Service {
    public BLEService() {
    }

    private final static String LOG_TAG = BLEService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    private int mConnectionState = STATE_DISCONNECTED;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.testapp.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.testapp.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.testapp.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.testapp.ACTION_DATA_AVAILABLE";
    public final static String ACC_DATA =
            "com.example.testapp.ACTION_ACC_DATA";
    public final static String GYRO_DATA =
            "com.example.testapp.ACTION_GYRO_DATA";
    public final static String MAGNETIC_DATA =
            "com.example.testapp.ACTION_MAGNETIC_DATA";

    private final static UUID UUID_ADC_SERVICE = UUID.fromString(Constants.ADC_SERVICE);
    private final static UUID UUID_ACC = UUID.fromString(Constants.ACC_CHARACTERISTIC);
    private final static UUID UUID_GYRO = UUID.fromString(Constants.GYRO_CHARACTERISTIC);
    private final static UUID UUID_MAGNETIC = UUID.fromString(Constants.MAGNETIC_CHARACTERISTIC);
    private final static UUID UUID_CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR = UUID.fromString(Constants.CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR);

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(LOG_TAG, "Connected to GATT server.");
                Log.d(LOG_TAG, "Attempting to start service discovery.");
                mBluetoothGatt.discoverServices();
            }

            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.e(LOG_TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.d(LOG_TAG, "service discovered, setting notification");
                setAccCharacteristicNotification();
            }

            else {
                Log.w(LOG_TAG, "onServicesDiscovered received : " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(LOG_TAG, "Descriptor write successful!");
                if(UUID_ACC.equals(descriptor.getCharacteristic().getUuid())){
                    setGyroCharacteristicNotification();
                }
                else if(UUID_GYRO.equals(descriptor.getCharacteristic().getUuid())){
                    setMagCharacteristicNotification();
                }
                else if(UUID_MAGNETIC.equals(descriptor.getCharacteristic().getUuid())){
                    Log.d(LOG_TAG, "all descriptor setting completed, finally");
                }
            }
            else {
                Log.w(LOG_TAG, "onDescriptorWrite received : " + status);
            }
        }

    };



    private void setAccCharacteristicNotification() {

        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID_ADC_SERVICE);

        if (mCustomService != null) {
            BluetoothGattCharacteristic mDataCharacteristic = mCustomService.getCharacteristic(UUID_ACC);
            if (mDataCharacteristic != null) {

                if(mBluetoothGatt.setCharacteristicNotification(mDataCharacteristic, true)) {
                    Log.d(LOG_TAG, "Set Characteristic Notification : SUCCESS");
                } else {
                    Log.e(LOG_TAG, "Set Characteristic Notification : FAILURE");
                }

                BluetoothGattDescriptor mConfigDescriptor = mDataCharacteristic.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR);
                if(mConfigDescriptor != null) {
                    Log.d(LOG_TAG, "Writing to descriptor...");
                    mConfigDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(mConfigDescriptor);
                } else {
                    Log.w(LOG_TAG, "Client Configuration Descriptor not found!");
                }
            } else {
                Log.w(LOG_TAG, "Data characteristic not found!");
                return;
            }
        } else{
            Log.w(LOG_TAG, "ADC Service not found!");
            return;
        }

    }

    private void setGyroCharacteristicNotification() {

        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID_ADC_SERVICE);

        if (mCustomService != null) {
            BluetoothGattCharacteristic mDataCharacteristic = mCustomService.getCharacteristic(UUID_GYRO);
            if (mDataCharacteristic != null) {

                if(mBluetoothGatt.setCharacteristicNotification(mDataCharacteristic, true)) {
                    Log.d(LOG_TAG, "Set Characteristic Notification : SUCCESS");
                } else {
                    Log.e(LOG_TAG, "Set Characteristic Notification : FAILURE");
                }

                BluetoothGattDescriptor mConfigDescriptor = mDataCharacteristic.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR);
                if(mConfigDescriptor != null) {
                    Log.d(LOG_TAG, "Writing to descriptor...");
                    mConfigDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(mConfigDescriptor);
                } else {
                    Log.w(LOG_TAG, "Client Configuration Descriptor not found!");
                }
            } else {
                Log.w(LOG_TAG, "Data characteristic not found!");
                return;
            }
        } else{
            Log.w(LOG_TAG, "ADC Service not found!");
            return;
        }

    }

    private void setMagCharacteristicNotification() {

        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID_ADC_SERVICE);

        if (mCustomService != null) {
            BluetoothGattCharacteristic mDataCharacteristic = mCustomService.getCharacteristic(UUID_MAGNETIC);
            if (mDataCharacteristic != null) {

                if(mBluetoothGatt.setCharacteristicNotification(mDataCharacteristic, true)) {
                    Log.d(LOG_TAG, "Set Characteristic Notification : SUCCESS");
                } else {
                    Log.e(LOG_TAG, "Set Characteristic Notification : FAILURE");
                }

                BluetoothGattDescriptor mConfigDescriptor = mDataCharacteristic.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR);
                if(mConfigDescriptor != null) {
                    Log.d(LOG_TAG, "Writing to descriptor...");
                    mConfigDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(mConfigDescriptor);
                } else {
                    Log.w(LOG_TAG, "Client Configuration Descriptor not found!");
                }
            } else {
                Log.w(LOG_TAG, "Data characteristic not found!");
                return;
            }
        } else{
            Log.w(LOG_TAG, "ADC Service not found!");
            return;
        }
    }

    // broadcast intents
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent();

        if (UUID_ACC.equals(characteristic.getUuid())) {
            Log.d(LOG_TAG, "accdata changed");
            byte accRaws[] = characteristic.getValue();
            Calc.ax = Utils.ByteToFloat(accRaws, 0);
            Calc.ay = Utils.ByteToFloat(accRaws, 4);
            Calc.az = -Utils.ByteToFloat(accRaws, 8);
        }
        else if (UUID_GYRO.equals(characteristic.getUuid())) {
            Log.d(LOG_TAG, "gyrodata changed");
            byte gyroRaws[] = characteristic.getValue();
            Calc.gx = Utils.ByteToFloat(gyroRaws, 0)*((float)Math.PI)/180.0f;
            Calc.gy = Utils.ByteToFloat(gyroRaws, 4)*((float)Math.PI)/180.0f;
            Calc.gz = -Utils.ByteToFloat(gyroRaws, 8)*((float)Math.PI)/180.0f;
        }
        else if (UUID_MAGNETIC.equals(characteristic.getUuid())) {
            Log.d(LOG_TAG, "magneticdata changed");
            byte magRaws[] = characteristic.getValue();
            Calc.mx = -Utils.ByteToFloat(magRaws, 0);
            Calc.my = Utils.ByteToFloat(magRaws, 4);
            Calc.mz = -Utils.ByteToFloat(magRaws, 8);
            intent.setAction(MAGNETIC_DATA);
            sendBroadcast(intent);
        }
    }

    public class LocalBinder extends Binder {
        BLEService getService() {
            return BLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
//                Log.e(LOG_TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
//            Log.e(LOG_TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
//            Log.w(LOG_TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
//            Log.d(LOG_TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
//            Log.w(LOG_TAG, "Device not found.  Unable to connect.");
            return false;
        }

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
//        Log.d(LOG_TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        broadcastUpdate(ACTION_GATT_DISCONNECTED);
    }

    public int getConnectionState() {
        return mConnectionState;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }


}