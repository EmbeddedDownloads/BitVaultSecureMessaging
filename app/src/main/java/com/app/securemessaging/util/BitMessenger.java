package com.app.securemessaging.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Font file used for displaying the icons as a text.
 */

public class BitMessenger extends android.support.v7.widget.AppCompatTextView {

    public BitMessenger(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BitMessenger(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BitMessenger(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/bitmessenger.ttf");
        setTypeface(tf);
    }

}