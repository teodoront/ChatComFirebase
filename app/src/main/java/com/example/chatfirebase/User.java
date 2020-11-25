package com.example.chatfirebase;

public class User {
    private final String uuid;
    private final String userName;
    private final String profileFinalUrl;

    public User(String uuid, String userName, String profileFinalUrl) { //Construtor
        this.uuid = uuid;
        this.userName = userName;
        this.profileFinalUrl = profileFinalUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public String getUserName() {
        return userName;
    }

    public String getProfileFinalUrl() {
        return profileFinalUrl;
    }
}
