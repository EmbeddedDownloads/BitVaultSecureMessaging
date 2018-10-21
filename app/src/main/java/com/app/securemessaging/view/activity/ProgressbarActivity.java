package com.app.securemessaging.view.activity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import com.app.securemessaging.R;

/**
 * Set progress bar for downloading of image
 */

public class ProgressbarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progressbar);
        ProgressBar progress = (ProgressBar) findViewById(R.id.circle_progress_bar);
        double n = 10.0;
        Log.d("MainActivity", "double" + n);
        Log.d("MainActivity", "casting" + (int) (n * 100));
        ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress", 0, (int) (n * 100));
        animation.setDuration(5000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();

    }
}