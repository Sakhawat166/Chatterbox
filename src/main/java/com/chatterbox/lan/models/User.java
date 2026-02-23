package com.chatterbox.lan.models;

public class User {
    private String id;
    private String username;
    private String avatarPath;
    public boolean isMe;
    public User(String username, String avatarPath){
        this.username = username;
        this.avatarPath = avatarPath;
    }
    public User(String id, String username, String avatarPath) {
        this.id = id;
        this.username = username;
        this.avatarPath = avatarPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }
}
