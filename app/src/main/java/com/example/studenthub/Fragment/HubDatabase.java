package com.example.studenthub.Fragment;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import com.example.studenthub.Model.User;


interface UserCallback {
    void consume(User user, FirebaseFirestoreException err);
}

public class HubDatabase {

    public static void getUser(UserCallback callback) {
        assert FirebaseAuth.getInstance().getUid() != null;
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .addSnapshotListener((value, error) -> {
                    if(value!=null && value.getData()!=null) {
                        User user = new User(value.getData());
                        callback.consume(user,error);
                    }
                });
    }
    private HubDatabase() {}
}