package com.example.gatthidservertest.KeyboardUtil;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.example.gatthidservertest.KeyboardView.Keyboard;
import com.example.gatthidservertest.KeyboardView.KeyboardView;
import com.example.gatthidservertest.R;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyKeyboardView extends KeyboardView {
    public static int KeyCode_Tab = 9;
    public static int KeyCode_Enter = 10;
    public static int KeyCode_Shift = 16;
    public static int KeyCode_Ctrl = 17;
    public static int KeyCode_Alt = 18;
    public static int KeyCode_CapsLock = 20;
    public static int KeyCode_Esc = 27;
    public static int KeyCode_Space = 32;
    public static int KeyCode_Win = 91;

    public static Set<Integer> SpecialKeyCodes;

    {
        SpecialKeyCodes = new HashSet<Integer>();
        SpecialKeyCodes.add(KeyCode_Esc);
        SpecialKeyCodes.add(KeyCode_Tab);
        SpecialKeyCodes.add(KeyCode_Enter);
        SpecialKeyCodes.add(KeyCode_Shift);
        SpecialKeyCodes.add(KeyCode_Ctrl);
        SpecialKeyCodes.add(KeyCode_Alt);
        SpecialKeyCodes.add(KeyCode_CapsLock);
        SpecialKeyCodes.add(KeyCode_Space);
        SpecialKeyCodes.add(KeyCode_Win);
    }

    public MyKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.keyboardViewStyle);
    }

    public MyKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MyKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private int kbdPaddingLeft;
    private int kbdPaddingRight;
    private int kbdPaddingTop;
    private int kbdPaddingBottom;

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        kbdPaddingLeft = getPaddingLeft();
        kbdPaddingRight = getPaddingRight();
        kbdPaddingTop = getPaddingTop();
        kbdPaddingBottom = getPaddingBottom();
        initSpecialKeys(canvas);
    }

    private void initSpecialKeys(Canvas canvas) {
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            if (SpecialKeyCodes.contains(key.codes[0])) {
                drawBackground(canvas, key, R.drawable.ic_launcher_background);
                drawText(canvas, key);
            }
        }
    }

    public void drawBackground(Canvas canvas, Keyboard.Key key, @DrawableRes int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(mContext, drawableId);
        int[] drawableState = key.getCurrentDrawableState();
        drawable.setState(drawableState);
        final Rect bounds = drawable.getBounds();
        if (key.width != bounds.right ||
                key.height != bounds.bottom) {
            drawable.setBounds(0, 0, key.width, key.height);
        }
        canvas.translate(key.x + kbdPaddingLeft, key.y + kbdPaddingTop);
        drawable.draw(canvas);
        canvas.translate(-key.x - kbdPaddingLeft, -key.y - kbdPaddingTop);
    }

    public void drawText(Canvas canvas, Keyboard.Key key) {
        if (key.label != null) {
            drawText(canvas, key, key.label.toString());
        }
    }

    public void drawText(Canvas canvas, Keyboard.Key key, String text) {
        canvas.translate(key.x + kbdPaddingLeft, key.y + kbdPaddingTop);
        final Paint paint = mPaint;
        final Rect padding = mPadding;
        if (text.length() > 1 && key.codes.length < 2) {
            paint.setTextSize(mLabelTextSize);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            paint.setTextSize(mKeyTextSize);
            paint.setTypeface(Typeface.DEFAULT);
        }
        // Draw a drop shadow for the text
        paint.setShadowLayer(mShadowRadius, 0, 0, mShadowColor);
        // Draw the text
        canvas.drawText(text,
                (key.width - padding.left - padding.right) / 2
                        + padding.left,
                (key.height - padding.top - padding.bottom) / 2
                        + (paint.getTextSize() - paint.descent()) / 2 + padding.top,
                paint);
        // Turn off drop shadow
        paint.setShadowLayer(0, 0, 0, 0);
        canvas.translate(-key.x - kbdPaddingLeft, -key.y - kbdPaddingTop);
    }
}
