package com.mandakini.memonest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.mandakini.memonest.R;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressSplash;
    private Handler handler = new Handler();
    private int progressValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressSplash = findViewById(R.id.progressSplash);

        handler.postDelayed(progressRunnable, 30);

        handler.postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 3000);
    }

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (progressValue <= 100) {
                progressSplash.setProgress(progressValue);
                progressValue++;
                handler.postDelayed(this, 25);
            }
        }
    };
}