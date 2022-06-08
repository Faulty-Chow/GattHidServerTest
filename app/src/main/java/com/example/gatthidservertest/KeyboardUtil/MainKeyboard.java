package com.example.gatthidservertest.KeyboardUtil;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.example.gatthidservertest.KeyboardView.Keyboard;
import com.example.gatthidservertest.KeyboardView.KeyboardView;
import com.example.gatthidservertest.R;

public class MainKeyboard {
    private static final int KeyCode_Remain = -1;

    private Activity mActivity;
    private KeyboardView mainKeyboardView;
    private Keyboard mainKeyboard;
    private KeyboardView remainKeyboardView;
    private Keyboard remainKeyboard;

    public MainKeyboard(Activity activity) {
        mActivity = activity;
        mainKeyboardView = (KeyboardView) mActivity.findViewById(R.id.keyboard_main);
        mainKeyboard = new Keyboard(mActivity, R.xml.keyboard_main);
        mainKeyboardView.setKeyboard(mainKeyboard);
        mainKeyboardView.setEnabled(true);
        mainKeyboardView.setPreviewEnabled(false);
        mainKeyboardView.setOnKeyboardActionListener(mainListener);

        remainKeyboardView = (KeyboardView) mActivity.findViewById(R.id.keyboard_remain);
        remainKeyboard = new Keyboard(mActivity, R.xml.keyboard_remain);
        remainKeyboardView.setKeyboard(remainKeyboard);
        remainKeyboardView.setEnabled(true);
        remainKeyboardView.setPreviewEnabled(false);
        remainKeyboardView.setOnKeyboardActionListener(remainListener);
    }

    private KeyboardView.OnKeyboardActionListener mainListener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void onPress(int primaryCode) {
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            if (primaryCode == KeyCode_Remain) {
                if (remainKeyboardView.getVisibility() == TextView.GONE) {
                    showRemainKeyboard();
                } else {
                    hideRemainKeyboard();
                }
            }

        }

        @Override
        public void onText(CharSequence text) {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void swipeUp() {
        }
    };

    private KeyboardView.OnKeyboardActionListener remainListener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void onPress(int primaryCode) {

        }

        @Override
        public void onRelease(int primaryCode) {

        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {

        }

        @Override
        public void onText(CharSequence text) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    };

    public void showMainKeyboard() {
        mainKeyboardView.setVisibility(TextView.VISIBLE);
        mainKeyboardView.setEnabled(true);
    }

    public void hideMainKeyboard() {
        mainKeyboardView.setVisibility(TextView.GONE);
        mainKeyboardView.setEnabled(false);
        hideRemainKeyboard();
    }

    private void showRemainKeyboard() {
        remainKeyboardView.setVisibility(TextView.VISIBLE);
        remainKeyboardView.setEnabled(true);
    }

    public void hideRemainKeyboard() {
        remainKeyboardView.setVisibility(TextView.GONE);
        remainKeyboardView.setEnabled(false);
    }
}
