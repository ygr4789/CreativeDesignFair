package com.example.testapp;

import android.util.Log;

public class Constants {
    // Enter your custom service UUID below between the quotes
    public static final String ADC_SERVICE = "9a48ecba-2e92-082f-c079-9e75aae428b1";

    // Enter your data characteristic UUID below between the quotes
    public static final String ACC_CHARACTERISTIC = "2db29ee2-d964-43fe-b33f-fbfe83941613";
    public static final String GYRO_CHARACTERISTIC = "afaa0cbf-dced-40c9-b009-00c19f1de6e3";
    public static final String MAGNETIC_CHARACTERISTIC = "83a30667-6f39-4720-9f4b-33ad1154c7f8";
    public static final String INTERVAL_CHARACTERISTIC = "1A3AC130-31EE-758A-BC50-54A61958EF81";

    // This is a standard descriptor UUID, change it if required
    public static final String CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    // Enter your custom device name below between the quotes
    public static final String DEVICE_NAME = "Arduino Nano 33 BLE";

}
