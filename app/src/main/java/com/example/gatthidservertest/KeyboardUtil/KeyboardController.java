package com.example.gatthidservertest.KeyboardUtil;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gatthidservertest.GattHidServer;
import com.example.gatthidservertest.KeyboardView.KeyboardView;
import com.example.gatthidservertest.R;

import java.util.ArrayList;
import java.util.List;

public class KeyboardController {
    private Context mContext;
    private RelativeLayout myLayout;
    private GattHidServer mGattHidServer;
    private List<SoftKeyboard> mSoftKeyboards;

    public KeyboardController(Context context, GattHidServer server) {
        mContext = context;
        mGattHidServer = server;
        myLayout = new RelativeLayout(mContext);
        mSoftKeyboards = new ArrayList<>();
    }

    public void init_main_keyboard(KeyboardView keyboardView,TextView controller) {
        SoftKeyboard mainKeyboard = new SoftKeyboard(mContext);
        mainKeyboard.registerKeyboard(R.xml.keyboard_main);
        mainKeyboard.registerKeyboardView(keyboardView);
        mainKeyboard.registerController(controller);
    }

    private void init_num_keyboard(KeyboardView keyboardView,TextView controller) {
        SoftKeyboard numKeyboard = new SoftKeyboard(mContext);
        numKeyboard.registerKeyboard(R.xml.keyboard_num);
        numKeyboard.registerKeyboardView(keyboardView);
        numKeyboard.registerController(controller);
    }

}
