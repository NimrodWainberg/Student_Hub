package com.example.studenthub.Model;

import java.util.HashMap;

public class ChatRoom {

    public static String DUMMY = "DUMMY";
    String id, ownerId, secondUserId;
    HashMap<String,ChatMessage> chatMessages;

    public ChatRoom(String id, String ownerId, String secondUserId,
                    HashMap<String, ChatMessage> chatMessages) {
        this.ownerId = ownerId;
        this.secondUserId = secondUserId;
        this.chatMessages = chatMessages;
        this.id = id;
    }

    public ChatRoom() {}
    public ChatRoom(String id,String ownerId,String secondUserId) {
        this.ownerId = ownerId;
        this.secondUserId = secondUserId;
        this.chatMessages = new HashMap<>();
        chatMessages.put(DUMMY,new ChatMessage(DUMMY,ownerId,secondUserId,DUMMY));
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getSecondUserId() {
        return secondUserId;
    }

    public void setSecondUserId(String secondUserId) {
        this.secondUserId = secondUserId;
    }

    public HashMap<String,ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(HashMap<String,ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "ownerId='" + ownerId + '\'' +
                ", secondUserId='" + secondUserId + '\'' +
                ", chatMessages=" + chatMessages +
                '}';
    }
}
