package com.chatterbox.lan.controllers;


import com.chatterbox.lan.database.*;
import com.chatterbox.lan.models.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;

public class ChatPageController {
    private MessageRepo messageRepo;
    private UserRepo userRepo;
    private User currentUser;
    private String currentConversationId;
    private String selectedUsername;

    @FXML
    private ListView<Message> messageList;


    @FXML
    private ListView<String> userList;

    @FXML
    private Label inboxLabel;

    @FXML
    private TextField inputMessage;

    @FXML
    private Button sendButton;

    @FXML
    public void initialize() {
//        userList.getItems().addAll("jon","doe");
//
//        // Create users
//
//        // Create users
//        User me = new User("jon", "/avatars/avatar1.png");
//        me.setMe(true); // Mark this user as "me"
//
//        User other = new User("doe", "/avatars/avatar2.png");
//        other.setMe(false); // This is another user
//        inboxLabel.setText(other.getUsername());
//
//        // Add messages
//        messageList.getItems().addAll(
//                new Message(me, "Hello world!"),
//                new Message(me, "This is my message"),
//                new Message(other, "Hello from the other side!"),
//                new Message(other, "This message is from someone else")
//        );

        db.connect();
        messageRepo = new MessageRepo();
        userRepo = new UserRepo();

        // Set up current user (the logged-in user)
        currentUser = new User("jon", "/avatars/avatar1.png");
        currentUser.setMe(true);
        userRepo.saveUser(currentUser);

        // Create some other users for demo (you can remove this later)
        User user2 = new User("doe", "/avatars/avatar2.png");
        userRepo.saveUser(user2);

        User user3 = new User("alice", "/avatars/avatar3.png");
        userRepo.saveUser(user3);

        // Load all users into the user list
        loadUsers();

        // Set up message cell factory
        messageList.setCellFactory(param -> new MessageCell());

        // Listen for user selection changes
        userList.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                loadConversation(newUser);
            }
        });


        if (!userList.getItems().isEmpty()) {
            userList.getSelectionModel().select(0);
        }
    }


    private void loadUsers() {
        List<User> users = userRepo.getAllUsers();
        userList.getItems().clear();

        for (User user : users) {
            // Don't show yourself in the user list
            if (!user.getUsername().equals(currentUser.getUsername())) {
                userList.getItems().add(user.getUsername());
            }
        }
    }


    private void loadConversation(String otherUsername) {
        selectedUsername = otherUsername;
        currentConversationId = buildConversationId(currentUser.getUsername(), otherUsername);

        // Get messages for this conversation
        List<Message> messages = messageRepo.getMessages(currentConversationId);

        // Mark messages as "me" or "other"
        for (Message msg : messages) {
            boolean isMe = msg.getUser().getUsername().equals(currentUser.getUsername());
            msg.getUser().setMe(isMe);
        }

        // Update UI
        inboxLabel.setText(otherUsername);
        messageList.getItems().setAll(messages);
    }

    private String buildConversationId(String userA, String userB) {
        if (userA.compareToIgnoreCase(userB) <= 0) {
            return userA + "|" + userB;
        }
        return userB + "|" + userA;
    }
    @FXML
    public void onSendMessage() {
        if (inputMessage == null || inputMessage.getText().trim().isEmpty()) {
            return;
        }

        if (currentConversationId == null || selectedUsername == null) {
            System.err.println("No conversation selected!");
            return;
        }

        String text = inputMessage.getText().trim();

        // Create new message
        Message newMessage = new Message(currentUser, text, currentConversationId);

        // Save to database
        messageRepo.saveMessage(newMessage);

        // Mark as "me" for display
        newMessage.getUser().setMe(true);

        // Add to UI
        messageList.getItems().add(newMessage);

        // Clear input field
        inputMessage.clear();
    }

    /**
     * Call this when closing the app to properly close MongoDB connection
     */
    public void cleanup() {
        db.close();
    }
}



