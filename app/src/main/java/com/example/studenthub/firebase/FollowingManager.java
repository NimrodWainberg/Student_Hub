package com.example.studenthub.firebase;

import com.example.studenthub.Model.User;
import com.example.studenthub.firebase.interfaces.FirebaseCallBack;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FollowingManager {

    public static DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference().child("Follow");
    public static DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        public static void follow(String uid1, String uid2) {
            followingRef.child(uid1)
                    .child("following").child(uid2).setValue(true);
            followingRef.child(uid2)
                    .child("followers").child(uid1).setValue(true);
        }

    public static void unfollow(String uid1, String uid2) {
        followingRef.child(uid1)
                .child("following").child(uid2).removeValue();
        followingRef.child(uid2)
                .child("followers").child(uid1).removeValue();
    }

    public static void getFollowing(FirebaseCallBack<List<User>> callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        followingRef.child(uid)
                .child("followers")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    HashMap<String, Boolean> hash = new HashMap<>();
                    for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                        hash.put(childSnapShot.getKey(), true);
                    }
                    usersRef.get().addOnSuccessListener(usersDataSnapshot -> {
                        List<User> following = new ArrayList<>();
                        for (DataSnapshot userChildSnap : usersDataSnapshot.getChildren()) {
                            if (hash.containsKey(userChildSnap.getKey()) &&
                                    hash.get(userChildSnap.getKey())) {
                                User u = userChildSnap.getValue(User.class);
                                following.add(u);
                            }
                        }
                        callback.onComplete(following);
                    }).addOnFailureListener(callback::onFailure);

                })
                .addOnFailureListener(callback::onFailure);
    }
}
