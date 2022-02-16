package com.example.studenthub.Model;
import com.example.studenthub.Fragment.HubDatabase;

import java.util.Map;

public class User {
    private String username;
    private String email;
    private String fullName;
    private String imageUrl;
    private String bio;
    private String id;


    public User(String username, String email, String fullName, String imageUrl, String bio) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.imageUrl = imageUrl;
        this.bio = bio;
    }

    public User() {
        // Default empty constructor
    }

    public User(Map<String,Object> data) {
        this.username = (String) data.get("username");
        this.imageUrl = (String) data.get("imageUrl");
        this.bio = (String) data.get("bio");
        this.fullName = (String) data.get("fullName");
        this.email = (String) data.get("email");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(String id) {
        this.id = id;
    }
}