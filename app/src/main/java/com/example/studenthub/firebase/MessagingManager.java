package com.example.studenthub.firebase;


import android.util.Pair;

import androidx.annotation.NonNull;

import com.example.studenthub.Model.ChatMessage;
import com.example.studenthub.Model.ChatRoom;
import com.example.studenthub.Model.User;
import com.example.studenthub.firebase.interfaces.FirebaseCallBack;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MessagingManager {
    private static MessagingManager instance;
    public DatabaseReference chatRoomsRef = FirebaseDatabase.getInstance().getReference().child("ChatRooms");
    public DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    public HashMap<String, User> cachedUsers = new HashMap<>();
    private ValueEventListener userCachingEventListener;
    private ValueEventListener chatRoomMessagesValueEventListener;
    private ValueEventListener chatRoomsValueEventListener;

    private MessagingManager() {}

    public static MessagingManager getInstance() {
        if (instance == null) {
            instance = new MessagingManager();
        }
        return instance;
    }

    public void createNewChatRoom(String recipientId, FirebaseCallBack<String> callback) {
        String uid = FirebaseAuth.getInstance().getUid();

        chatRoomsRef.get()
                .addOnSuccessListener(dataSnapshot -> {
                    for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                        if ((((String) childSnap.child("ownerId").getValue()).equals(uid)
                                && ((String) childSnap.child("secondUserId").getValue()).equals(recipientId))
                                || (((String) childSnap.child("secondUserId").getValue()).equals(uid)
                                && ((String) childSnap.child("ownerId").getValue()).equals(recipientId))) {
                            callback.onFailure(new Exception("Chat room with this user already exists"));
                            return;
                        }
                    }
                    DatabaseReference newRoom = chatRoomsRef.push();
                    ChatRoom room = new ChatRoom(newRoom.getKey(), uid, recipientId);
                    newRoom.setValue(room)
                            .addOnSuccessListener(unused ->
                                    callback.onComplete("Successfully added chat room "))
                            .addOnFailureListener(callback::onFailure);
                }).addOnFailureListener(callback::onFailure);

    }

    public void addUserCachingEventListener(FirebaseCallBack<String> callBack) {
        userCachingEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnap : snapshot.getChildren()) {
                    User u = childSnap.getValue(User.class);
                    if (u == null) continue;
                    cachedUsers.put(u.getId(), u);
                }
                callBack.onComplete("Finished caching users");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callBack.onFailure(error.toException());
            }
        };
        usersRef.addValueEventListener(userCachingEventListener);
    }

    public void removeUserCachingEventListener() {
        if (userCachingEventListener != null)
            usersRef.removeEventListener(userCachingEventListener);
    }

    public void sendNewMessage(String roomId, String recipientId, String messageContent,
                               FirebaseCallBack<String> callBack) {
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference newMessage = chatRoomsRef.child(roomId).child("chatMessages")
                .push();
        ChatMessage message = new ChatMessage(newMessage.getKey(), uid, recipientId, messageContent);
        newMessage.setValue(message)
                .addOnSuccessListener(unused -> callBack
                        .onComplete("Successfully sent message" + message))
                .addOnFailureListener(callBack::onFailure);
    }

    public void addChatRoomMessagesValueEventListener(String roomId,
                                                      FirebaseCallBack<Pair<List<ChatMessage>, ChatRoom>> callback) {

        Query query = chatRoomsRef.child(roomId);
        chatRoomMessagesValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ChatRoom room = snapshot.getValue(ChatRoom.class);
                if (room == null) return;
                List<ChatMessage> chatMessages = new ArrayList<>(room.getChatMessages().values());
                Collections.sort(chatMessages);
                room.setChatMessages(null); // clean a bit
                callback.onComplete(new Pair<>(chatMessages, room));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        };
        query.addValueEventListener(chatRoomMessagesValueEventListener);
    }

    public void removeChatRoomMessagesValueEventListener(String roomId) {
        if (chatRoomMessagesValueEventListener != null)
            chatRoomsRef.child(roomId).removeEventListener(chatRoomMessagesValueEventListener);
    }

    public User getUserById(String id) {
        System.out.println(id);
        System.out.println(cachedUsers.keySet());
        return cachedUsers.get(id);
    }

    public void addChatRoomsValueEventListener(FirebaseCallBack<List<ChatRoom>> callBack) {
        String uid = FirebaseAuth.getInstance().getUid();
        chatRoomsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ChatRoom> chatRooms = new ArrayList<>();
                String owner, secondUser;

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    owner = ((String) childSnapshot.child("ownerId").getValue());
                    secondUser = ((String) childSnapshot.child("secondUserId").getValue());
                    if (owner == null || secondUser == null)
                        continue;
                    if (owner.equals(uid) || secondUser.equals(uid)) {
                        chatRooms.add(childSnapshot.getValue(ChatRoom.class));
                    }
                }
                callBack.onComplete(chatRooms);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callBack.onFailure(error.toException());
            }
        };
        chatRoomsRef.addValueEventListener(chatRoomsValueEventListener);

    }

    public void removeChatRoomsValueEventListener() {
        if (chatRoomMessagesValueEventListener != null)
            chatRoomsRef.removeEventListener(chatRoomsValueEventListener);
    }
}
