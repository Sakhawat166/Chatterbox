package com.chatterbox.lan.controllers;

import com.chatterbox.lan.models.Message;
import com.chatterbox.lan.models.User;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class ChatPageController {
    @FXML
    private Label welcomeText;

    @FXML
    private ListView<Message> messageList;

    private ObservableList<Message> observableMessage;

    @FXML
    private ListView<String> userList;

    @FXML
    private Label inboxLabel;

    @FXML
    public void initialize(){
        userList.getItems().addAll("jon","doe");

        // Create users

        // Create users
        User me = new User("jon", "/avatars/avatar1.png");
        me.setMe(true); // Mark this user as "me"

        User other = new User("doe", "/avatars/avatar2.png");
        other.setMe(false); // This is another user
        inboxLabel.setText(other.getUsername());

        // Add messages
        messageList.getItems().addAll(
                new Message(me, "Hello world!"),
                new Message(me, "This is my message"),
                new Message(other, "Hello from the other side!"),
                new Message(other, "This message is from someone else")
        );

        messageList.setCellFactory(param -> new MessageCell());




    }
}
