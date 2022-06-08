package com.example.gatthidservertest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private GattHidServer mGattHidServer;
    private KeyboardTest mKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, GattHidServer.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

//        Key_A.setOnClickListener(v -> {
//            if (mGattHidServer != null) {
//                Log.i("MainActivity", "Key_A is clicked");
//                mGattHidServer.sendInputEvent((byte) 65);
////                mGattHidServer.sendBatteryLevel();
//            } else {
//                Log.e("MainActivity", "mGattHidServer == null.");
//            }
//        });
        mKeyboard = new KeyboardTest(this, R.xml.keyboard_main);
        mKeyboard.registerKeyboardView(findViewById(R.id.keyboard_main));

//        KeyboardTest keyboardTest = new KeyboardTest(this, R.xml.keyboard_remain);
//        keyboardTest.registerKeyboardView(findViewById(R.id.keyboard_remain));
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mGattHidServer = ((GattHidServer.GattHidServerBindler) iBinder).getService();
            //mKeyboard.registerGattHidServer(mGattHidServer);
            Log.i("Service", "Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("Service", "Disconnected");
        }
    };
}