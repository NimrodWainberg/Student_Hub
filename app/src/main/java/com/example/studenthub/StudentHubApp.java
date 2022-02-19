package com.example.studenthub;

import android.app.Application;
import android.content.Intent;

import com.example.studenthub.firebase.MessagingService;
import com.google.firebase.messaging.FirebaseMessaging;

public class StudentHubApp extends Application {
    public static String token = "";
    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, MessagingService.class));
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> token = s);
    }
}
