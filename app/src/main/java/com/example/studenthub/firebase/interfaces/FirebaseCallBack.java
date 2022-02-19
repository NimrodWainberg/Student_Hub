package com.example.studenthub.firebase.interfaces;

public interface FirebaseCallBack<T> {

    void onComplete(T object);
    void onFailure(Exception e);
}
