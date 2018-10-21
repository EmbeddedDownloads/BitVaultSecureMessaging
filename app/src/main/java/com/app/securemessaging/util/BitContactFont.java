package com.app.securemessaging.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by linchpin on 16/11/17.
 */

public class BitContactFont extends android.support.v7.widget.AppCompatTextView {

    public BitContactFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BitContactFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BitContactFont(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/bitcontact.ttf");
        setTypeface(tf);
    }

}