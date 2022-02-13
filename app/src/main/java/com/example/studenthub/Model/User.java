package com.example.studenthub.Model;
import com.example.studenthub.Fragment.HubDatabase;

import java.util.Map;

public class User {
    private String email;
    private String fullName;
    private String imageUrl;
    private String bio;
    private String id;


    public User(String email, String fullName, String imageUrl, String bio) {
        this.email = email;
        this.fullName = fullName;
        this.imageUrl = imageUrl;
        this.bio = bio;
    }

    public User() {
    }

    public User(Map<String,Object> data) {
        String imageUrl = (String) data.get("imageUrl");
        String bio = (String) data.get("bio");
        String fullName = (String) data.get("fullName");
        String email = (String) data.get("email");
        String id = (String) data.get("id");
        this.imageUrl = imageUrl;
        this.bio = bio;
        this.fullName = fullName;
        this.email = email;
        this.id = id;
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



    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", bio='" + bio + '\'' +
                '}';
    }
}