package com.example.gatthidservertest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import static com.example.gatthidservertest.UUID.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class GattHidServer extends Service {
    private static final String TAG = "GattHidServer";

    //region GattHidServerBindler
    private GattHidServerBindler mGattHidServerBindler = new GattHidServerBindler();

    class GattHidServerBindler extends Binder {
        GattHidServer getService() {
            return GattHidServer.this;
        }
    }
    //endregion

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mGattHidServerBindler;
    }

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mBluetoothGattServer;

    private BluetoothGattService Service_HumanInterfaceDevice;
    private BluetoothGattService Service_DeviceInformation;
    private BluetoothGattService Service_BatteryService;

    private BluetoothDevice mHostDevice;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    //region Public Methods
    private int waitForAddService;

    @SuppressLint("MissingPermission")
    public void initBleAdvertiser() {
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        AdvertiseSettings mAdvertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .setTimeout(0)
                .build();
        AdvertiseData mAdvertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(UUID.UUID_SERVICE_DEVICE_INFORMATION))
                .addServiceUuid(new ParcelUuid(UUID.UUID_SERVICE_BATTERY_SERVICE))
                .addServiceUuid(new ParcelUuid(UUID.UUID_SERVICE_HUMAN_INTERFACE_DEVICE))
                .build();
        AdvertiseData mScanResultData = new AdvertiseData.Builder()
                .addServiceUuid(new ParcelUuid(UUID.UUID_SERVICE_DEVICE_INFORMATION))
                .addServiceUuid(new ParcelUuid(UUID.UUID_SERVICE_BATTERY_SERVICE))
                .addServiceUuid(new ParcelUuid(UUID.UUID_SERVICE_HUMAN_INTERFACE_DEVICE))
                .build();
        mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, mScanResultData, mAdvertiseCallback);
    }

    @SuppressLint("MissingPermission")
    public void initGattHidServer() {
        mBluetoothGattServer = mBluetoothManager.openGattServer(this, mBluetoothGattServerCallback);
        mBluetoothGattServer.clearServices();

        initService_DeviceInformation();
        waitForAddService = -1;
        mBluetoothGattServer.addService(Service_DeviceInformation);
        while (waitForAddService == -1) ;

        initService_BatteryService();
        waitForAddService = -1;
        mBluetoothGattServer.addService(Service_BatteryService);
        while (waitForAddService == -1) ;

        initService_HumanInterfaceDevice();
        waitForAddService = -1;
        mBluetoothGattServer.addService(Service_HumanInterfaceDevice);
        while (waitForAddService == -1) ;

        List<BluetoothGattService> services = mBluetoothGattServer.getServices();
        if (services.size() == 3) {
            Log.i(TAG, "All services had been added.");
        } else {
            Log.e(TAG, "Some services were missing.");
            for (BluetoothGattService service : services) {
                Log.d(TAG, "Exist service UUID = " + service.getUuid().toString());
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void sendInputEvent(byte keycode) {
        if (mHostDevice != null) {
            InputReport.setValue(new byte[]{0x00, 0x00, keycode, 0x00, 0x00, 0x00, 0x00, 0x00});
            while (!mBluetoothGattServer.notifyCharacteristicChanged(mHostDevice, InputReport, false))
                ;
            InputReport.setValue(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
            while (!mBluetoothGattServer.notifyCharacteristicChanged(mHostDevice, InputReport, false))
                ;
        }
    }

    private byte batteryLevel = 0x64;

    @SuppressLint("MissingPermission")
    public void sendBatteryLevel() {
        batteryLevel--;
        if (mHostDevice != null) {
            BatteryLevel.setValue(new byte[]{batteryLevel});
            while (!mBluetoothGattServer.notifyCharacteristicChanged(mHostDevice, BatteryLevel, false))
                ;
        }
    }

    @SuppressLint("MissingPermission")
    public void stopGattHidServer() {
        if (mBluetoothGattServer != null) {
            mBluetoothGattServer.close();
            mBluetoothGattServer = null;
        }
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mBluetoothLeAdvertiser = null;
        }
    }
    //endregion

    //region Init Gatt Service
    private BluetoothGattCharacteristic BatteryLevel;
    private BluetoothGattCharacteristic InputReport;
    private BluetoothGattCharacteristic OutputReport;

    private void initService_DeviceInformation() {
        Service_DeviceInformation = new BluetoothGattService(UUID.UUID_SERVICE_DEVICE_INFORMATION, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        {   // Manufacturer Name
            final BluetoothGattCharacteristic manufacturerName = new BluetoothGattCharacteristic(
                    UUID.UUID_CHAR_MANUFACTURER_NAME_STRING,
                    BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);
            manufacturerName.setValue("Faulty Chow".getBytes(StandardCharsets.UTF_8));
            while (!Service_DeviceInformation.addCharacteristic(manufacturerName)) ;
        }
    }

    private void initService_BatteryService() {
        Service_BatteryService = new BluetoothGattService(UUID.UUID_SERVICE_BATTERY_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BatteryLevel = new BluetoothGattCharacteristic(
                UUID.UUID_CHAR_BATTERY_LEVEL,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);
        BatteryLevel.setValue(new byte[]{0x64});
        final BluetoothGattDescriptor clientCharacteristicConfiguration = new BluetoothGattDescriptor(
                UUID.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
        clientCharacteristicConfiguration.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        BatteryLevel.addDescriptor(clientCharacteristicConfiguration);
        Service_BatteryService.addCharacteristic(BatteryLevel);
    }

    private void initService_HumanInterfaceDevice() {
        Service_HumanInterfaceDevice = new BluetoothGattService(UUID.UUID_SERVICE_HUMAN_INTERFACE_DEVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        {   // HID Information
            BluetoothGattCharacteristic information = new BluetoothGattCharacteristic(
                    UUID.UUID_CHAR_HID_INFORMATION,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);
            information.setValue(new byte[]{0x11, 0x01, 0x00, 0x03});
            while (!Service_HumanInterfaceDevice.addCharacteristic(information)) ;
        }

        {   // Report Map
            BluetoothGattCharacteristic reportMap = new BluetoothGattCharacteristic(
                    UUID.UUID_CHAR_REPORT_MAP,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);
            reportMap.setValue(new byte[]{0x00});
            while (!Service_HumanInterfaceDevice.addCharacteristic(reportMap)) ;
        }

        {   // HID Control Point
            BluetoothGattCharacteristic controlPoint = new BluetoothGattCharacteristic(
                    UUID.UUID_CHAR_HID_CONTROL_POINT,
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED);
            controlPoint.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            while (!Service_HumanInterfaceDevice.addCharacteristic(controlPoint)) ;
        }

        {   // Input Report
            InputReport = new BluetoothGattCharacteristic(
                    UUID.UUID_CHAR_REPORT,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED | BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED);

            BluetoothGattDescriptor clientCharacteristicConfiguration = new BluetoothGattDescriptor(
                    UUID.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION,
                    BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED | BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED);
            clientCharacteristicConfiguration.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            InputReport.addDescriptor(clientCharacteristicConfiguration);

            BluetoothGattDescriptor inputReference = new BluetoothGattDescriptor(
                    UUID.UUID_DESCRIPTOR_REPORT_REFERENCE,
                    BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED);
            inputReference.setValue(new byte[]{0x01, 0x01});
            InputReport.addDescriptor(inputReference);

            while (!Service_HumanInterfaceDevice.addCharacteristic(InputReport)) ;
        }

        {   // Output Report
            OutputReport = new BluetoothGattCharacteristic(
                    UUID.UUID_CHAR_REPORT,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED | BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED);
            OutputReport.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

            BluetoothGattDescriptor outputReference = new BluetoothGattDescriptor(
                    UUID.UUID_DESCRIPTOR_REPORT_REFERENCE,
                    BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED);
            outputReference.setValue(new byte[]{0x01, 0x02});
            OutputReport.addDescriptor(outputReference);

            while (!Service_HumanInterfaceDevice.addCharacteristic(OutputReport)) ;
        }
    }
    //endregion

    //region Callback
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        private final static String tag = "AdvertiseCallback";

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.i(tag, "Advertiser init Success.");
            initGattHidServer();
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(tag, "Advertiser init Failure.");
        }
    };

    @SuppressLint("MissingPermission")
    private BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {
        private final static String tag = "GattServerCallback";

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothAdapter.STATE_CONNECTED) {
                mHostDevice = device;
                Log.i(tag, "Connect to Host: " + mHostDevice.getName());
            } else if (mHostDevice != null) {
                Log.e(tag, "Disconnect to Host: " + mHostDevice.getName());
                mHostDevice = null;
            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(tag, "Service added: " + service.getUuid());
            } else {
                Log.e(tag, "Service: " + service.getUuid().toString() + " Added Failure.");
            }
            waitForAddService = status;
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.i(tag, "Read Request from " + device.getName() + ": " + characteristic.getUuid().toString());
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, characteristic.getValue());
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.i(tag, "Write Request from " + device.getName() + ": " + characteristic.getUuid().toString());
            if (responseNeeded) {
                Log.i(tag, "Sending response to device: " + device.getName());
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, new byte[]{});
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            Log.i(tag, "Read Request from " + device.getName() + ": " + descriptor.getUuid().toString());
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, descriptor.getValue());
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            Log.i(tag, "Write Request from " + device.getName() + ": " + descriptor.getUuid().toString());
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(tag, "Notification sent to " + device.getName());
            } else {
                Log.e(tag, "Notification failed to " + device.getName());
            }
        }
    };
    //endregion
}
