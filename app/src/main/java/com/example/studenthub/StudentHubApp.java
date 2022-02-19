package com.example.studenthub;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.studenthub.firebase.MessagingManager;
import com.example.studenthub.firebase.MessagingService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class StudentHubApp extends Application {

    public static String token = "";
    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, MessagingService.class));
        Log.d("StudentHubApp","Starting messaging service");
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                token = s;
                System.out.println(token);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("There was a problem receiving token");
            }
        });


    }
}
