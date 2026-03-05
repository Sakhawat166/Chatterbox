package com.chatterbox.lan.controllers;


import com.chatterbox.lan.models.*;
import com.chatterbox.lan.network.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;


public class ChatPageController {
    private Client client;
    private User currentUser;
    private String currentConversationId;


    @FXML
    private ListView<Message> messageList;

    @FXML
    private ListView<Conversation> conversationList;

    @FXML
    private Label inboxLabel;

    @FXML
    private TextField inputMessage;

    @FXML
    private Button sendButton;

    @FXML
    public void initialize() {
        messageList.setCellFactory(param -> new MessageCell());

    }

    // Called by LoginController after successful login
    public void setCurrentUser(User user, Client clientInstance) {
        this.currentUser = user;
        this.client = clientInstance;

        client.setMessageListener(event -> Platform.runLater(() -> {
            switch (event.getType()) {
                case "NEW_MESSAGE" -> handleIncomingMessage(event);
                case "MESSAGES_RESPONSE" -> handleMessagesResponse(event);
                case "USERS_UPDATED" -> handleConversationsUpdate(event);
                default -> System.err.println("[CLIENT] Unknown event type: " + event.getType());
            }
        }));

        conversationList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        onConversationSelected(newVal);

                    }
                });

        // Fetch conversations immediately after login
        client.getUsers();


    }

    private void appendMessages(List<Message> messages) {
        messageList.getItems().addAll(messages);
        if (!messages.isEmpty()) {
            messageList.scrollTo(messages.size() - 1);
        }
    }

    private void onConversationSelected(Conversation conversation) {
        if (conversation == null) {
            return;
        }
        currentConversationId = conversation.getConversationId();
        inboxLabel.setText(conversation.getName());
        client.getMessages(currentConversationId);
    }



    // for loading conversations in the left pane when user logs in or when new conversation is created
    private void handleConversationsUpdate(Event event) {
        @SuppressWarnings("unchecked")
        List<Conversation> conversations = (List<Conversation>) event.getData("conversations");

        if (conversations == null) {
            System.err.println("No conversations in response");
            return;
        }

        conversationList.getItems().clear();
        conversationList.getItems().addAll(conversations);
        System.out.println("[CONTROLLER] Updated conversations: " + conversations.size() + " conversations");
    }


    // for loading new incoming message in real-time
    private void handleIncomingMessage(Event event) {
        if (currentConversationId == null || !currentConversationId.equals(event.getConversationId())) return;

        User sender = new User(event.getUsername(), "/avatars/default.png");
        sender.setMe(sender.getUsername().equals(currentUser.getUsername()));

        Message incoming = new Message(sender, event.getText(), event.getConversationId());
        appendMessages(List.of(incoming));
    }

    // for loading all message of a conversation when selected
    private void handleMessagesResponse(Event event) {

        List<Message> messages = (List<Message>) event.getData("messages");
        if (messages == null) {
            System.err.println("No messages in response");
            return;
        }

        // Set isMe flag for each message's sender
        for (Message message : messages) {
            if (message.getSender() != null) {
                boolean isCurrentUser = message.getSender().getUsername().equals(currentUser.getUsername());
                message.getSender().setMe(isCurrentUser);
            }
        }


        messageList.getItems().clear();
        appendMessages(messages);
    }


    @FXML
    public void onSendMessage() {
        if (inputMessage == null || inputMessage.getText().trim().isEmpty()) {
            return;
        }
        if (currentConversationId == null) {
            System.err.println("No conversation selected!");
            return;
        }

        String text = inputMessage.getText().trim();
//
//        // IMMEDIATELY add message to UI with current user as sender
//        User sender = new User(currentUser.getUsername(), currentUser.getAvatarPath());
//        sender.setMe(true);
//        Message outgoingMessage = new Message(sender, text, currentConversationId);
//        messageList.getItems().add(outgoingMessage);

        // Then send to server
        client.sendMessage(currentConversationId, text);
        inputMessage.clear();
    }

    public void onCreateConversation() {
        // create priavte chat or group chat
    }

    public void cleanup() {
        client.disconnect();
    }
}