package com.example.gatthidservertest.KeyboardUtil;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.gatthidservertest.KeyboardView.Keyboard;
import com.example.gatthidservertest.KeyboardView.KeyboardView;
import com.example.gatthidservertest.R;

public class SoftKeyboard implements KeyboardView.OnKeyboardActionListener {
    protected Context mContext;
    protected Keyboard mKeyboard;
    protected KeyboardView mKeyboardView;
    protected View mControlView;

    public SoftKeyboard(Context context) {
        mContext = context;
    }

    public boolean registerKeyboard(int xmlLayoutResId) {
        mKeyboard = new Keyboard(mContext, xmlLayoutResId);
        return mKeyboard != null;
    }

    public boolean registerKeyboardView(KeyboardView view) {
        mKeyboardView = view;
        if (mKeyboard == null)
            return false;
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setPreviewEnabled(false);
        return true;
    }

    public boolean registerController(View view) {
        mControlView = view;
        mControlView.setOnClickListener(v -> {
            if (mKeyboardView.getVisibility() == KeyboardView.VISIBLE) {
                hide();
                mControlView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            } else {
                show();
                mControlView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.selected));
            }
        });

        return mControlView != null;
    }

    public void show() {
        mKeyboardView.setVisibility(KeyboardView.VISIBLE);
        mKeyboardView.setEnabled(true);
    }

    public void hide() {
        mKeyboardView.setVisibility(KeyboardView.GONE);
        mKeyboardView.setEnabled(false);
    }

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
}
