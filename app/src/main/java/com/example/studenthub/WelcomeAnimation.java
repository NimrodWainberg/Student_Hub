package com.example.studenthub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class WelcomeAnimation extends AppCompatActivity {
    TextView appName;
    LottieAnimationView lottie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_animation);

        appName = findViewById(R.id.app_name);
        lottie = findViewById(R.id.lottie);

        // animation
        appName.animate().translationY(-1600).setDuration(2700).setStartDelay(0);
        lottie.animate().translationX(2000).setDuration(2000).setStartDelay(2900);

        // Run the animation async using runnable (run without any param on a different thread)
        // 3 sec
        new Handler().postDelayed(() -> {
            // After the Animation move to sign in activity activity
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);

            // kill the intent
            finish();
        }, 5000);
    }
}