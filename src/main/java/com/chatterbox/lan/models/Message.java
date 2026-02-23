package com.chatterbox.lan.models;

import java.time.LocalDateTime;

public class Message {
    private User user;
    private String text;
    private String id;
    private LocalDateTime timestamp;
    private String conversationId;


    public Message(User user, String text,String conversationId ) {
        this.user = user;
        this.text = text;
        this.conversationId = conversationId;
        this.timestamp = LocalDateTime.now();
    }
    public Message(String id, User user, String text,String conversationId ,LocalDateTime timestamp) {
        this.id = id;
        this.user = user;
        this.text = text;
        this.timestamp = timestamp;
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
