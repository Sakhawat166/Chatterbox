package com.chatterbox.lan.controllers;

import com.chatterbox.lan.models.Message;
import javafx.collections.*;

public class ChatDataManager {
    private ObservableList<Message> messages = FXCollections.observableArrayList();

     public void addMessage(Message message) {
        messages.add(message);
    }

     public ObservableList<Message> getMessages() {
        return messages;
    }
}
