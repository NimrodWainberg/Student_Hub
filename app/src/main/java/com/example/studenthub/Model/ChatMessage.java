package com.example.studenthub.Model;

import android.view.ContextMenu;

import java.util.concurrent.CompletionException;

public class ChatMessage  implements Comparable<ChatMessage> {
    private String id;
    private String authorId;
    private String recipientId;
    private String message;
    private long date;

    public ChatMessage(String id, String authorId, String recipientId, String message) {
        this.authorId = authorId;
        this.recipientId = recipientId;
        this.message = message;
        this.date = System.currentTimeMillis();
        this.id = id;
    }

    public ChatMessage() {}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getAuthorId() {
        return authorId;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "authorId='" + authorId + '\'' +
                ", recipientId='" + recipientId + '\'' +
                ", message='" + message + '\'' +
                ", date=" + date +
                '}';
    }

    @Override
    public int compareTo(ChatMessage chatMessage) {
        if(chatMessage.date > date)
            return -1;
        else if(chatMessage.date < date)
            return 1;
        return 0;
    }
}
