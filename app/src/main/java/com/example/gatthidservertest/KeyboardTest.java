package com.example.gatthidservertest;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.example.gatthidservertest.KeyboardUtil.MyKeyboardView;
import com.example.gatthidservertest.KeyboardView.Keyboard;
import com.example.gatthidservertest.KeyboardView.KeyboardView;

import java.util.HashMap;
import java.util.Map;

public class KeyboardTest extends Keyboard {
    private Context mContext;

    public KeyboardTest(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
        mContext = context;
    }

    private MyKeyboardView mKeyboardView;

    public void registerKeyboardView(MyKeyboardView view) {
        mKeyboardView = view;
        mKeyboardView.setKeyboard(this);
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setOnKeyboardActionListener(mKeyActionListener);
    }

    private KeyboardView.OnKeyboardActionListener mKeyActionListener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void onPress(int primaryCode) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onRelease(int primaryCode) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            // TODO Auto-generated method stub
            //mGattHidServer.sendInputEvent((byte)primaryCode);
        }

        @Override
        public void onText(CharSequence text) {
            // TODO Auto-generated method stub

        }

        @Override
        public void swipeLeft() {
            // TODO Auto-generated method stub

        }

        @Override
        public void swipeRight() {
            // TODO Auto-generated method stub

        }

        @Override
        public void swipeDown() {
            // TODO Auto-generated method stub

        }

        @Override
        public void swipeUp() {
            // TODO Auto-generated method stub

        }
    };

    public void show() {
        mKeyboardView.setVisibility(KeyboardView.VISIBLE);
        mKeyboardView.setEnabled(true);
    }

    public void hide() {
        mKeyboardView.setVisibility(KeyboardView.GONE);
        mKeyboardView.setEnabled(false);
    }

    private Button KeyboardButton;
    private boolean isVisibility = true;

    public void registerKeyboardButton(Button button) {
        KeyboardButton = button;
        KeyboardButton.setOnClickListener(view -> {
            if (isVisibility) {
                hide();
            } else {
                show();
            }
            isVisibility = !isVisibility;
        });
    }

    private Switch ServerSwitch;

    public void registerServerButton(Switch _switch) {
        ServerSwitch = _switch;
        ServerSwitch.setEnabled(false);
        ServerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Log.i("MainActivity", "Init GattHidServer.");
                mGattHidServer.initBleAdvertiser();
            } else if (mGattHidServer != null) {
                Log.i("MainActivity", "Stop GattHidServer.");
                mGattHidServer.stopGattHidServer();
            }
        });
    }

    private GattHidServer mGattHidServer;

    public void registerGattHidServer(GattHidServer gattHidServer) {
        mGattHidServer = gattHidServer;
        if (mGattHidServer != null) {
            ServerSwitch.setEnabled(true);
        }
    }
}
