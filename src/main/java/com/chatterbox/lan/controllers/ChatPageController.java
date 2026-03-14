package com.chatterbox.lan.controllers;


import com.chatterbox.lan.models.*;
import com.chatterbox.lan.network.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;


public class ChatPageController {
    private Client client;
    private User currentUser;
    private String currentConversationId;

    private final ObservableList<String> friends = FXCollections.observableArrayList();



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

    @FXML private VBox createConversationOverlay;
    @FXML private TextField conversationNameField;
    @FXML private ListView<String> availableUsersList;


    @FXML private VBox addFriendOverlay;
    @FXML private TextField friendUserNameField;

    @FXML
    public void initialize() {
        messageList.setCellFactory(param -> new MessageCell());
        conversationList.setCellFactory(list -> new ListCell<>() {

            private final Label nameLabel = new Label();
            private final HBox container = new HBox(nameLabel);

            {
                container.setSpacing(10);
                nameLabel.setStyle("-fx-font-weight: bold;");
                container.setStyle("-fx-padding: 8 12;");
            }

            @Override
            protected void updateItem(Conversation convo, boolean empty) {
                super.updateItem(convo, empty);

                if (empty || convo == null) {
                    setGraphic(null);
                } else {
                    String name = convo.getName();
                    if(name == null) {
                        name = convo.getMembers().get(0).equals(currentUser.getUsername()) ? convo.getMembers().get(1) : convo.getMembers().get(0);
                    }
                    nameLabel.setText(name);
                    setGraphic(container);
                }
            }
        });
        availableUsersList.getSelectionModel().setSelectionMode(
               SelectionMode.MULTIPLE
        );
        availableUsersList.setItems(friends);
        // Hide overlays initially
        if (createConversationOverlay != null) createConversationOverlay.setVisible(false);
        if (addFriendOverlay != null) addFriendOverlay.setVisible(false);

    }

    // Called by LoginController after successful login
    public void setCurrentUser(User user, Client clientInstance) {
        this.currentUser = user;
        this.client = clientInstance;

        client.setListener(event -> Platform.runLater(() -> {
            switch (event.getType()) {
                case "NEW_MESSAGE" -> handleIncomingMessage(event);
                case "MESSAGES_RESPONSE" -> handleMessagesResponse(event);
                case "CONVERSATIONS_UPDATED" -> handleConversationsUpdate(event);
                case "NEW_CONVERSATION" -> handleNewConversation(event);
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
        client.getConversations();
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



    // for loading conversations in the left pane when user logs in
    private void handleConversationsUpdate(Event event) {
        @SuppressWarnings("unchecked")
        List<Conversation> conversations = (List<Conversation>) event.getData("conversations");

        if (conversations == null) {
            System.err.println("No conversations in response");
            return;
        }
        for (Conversation conv : conversations) {
            for (String member : conv.getMembers()) {
                if (!member.equals(currentUser.getUsername()) && !friends.contains(member)) {
                    friends.add(member);
                }
            }
        }
        conversationList.getItems().clear();
        conversationList.getItems().addAll(conversations);
        System.out.println("[CONTROLLER] Updated conversations: " + conversations.size() + " conversations");
    }

    private void handleNewConversation(Event event) {
        Conversation newConversation = (Conversation) event.getData("conversation");
        if (newConversation == null) {
            System.err.println("No conversation data in NEW_CONVERSATION event");
            return;
        }
        conversationList.getItems().add(newConversation);
        for (String member : newConversation.getMembers()) {
            if (!member.equals(currentUser.getUsername()) && !friends.contains(member)) {
                friends.add(member);
            }
        }
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
    @FXML
    public void onOpenCreateConversation() {
        if (createConversationOverlay != null) {
            createConversationOverlay.setVisible(true);
            createConversationOverlay.setManaged(true);
        }
        conversationNameField.clear();
        availableUsersList.getSelectionModel().clearSelection();
    }

    @FXML
    public void onCloseCreateConversation() {
        if (createConversationOverlay != null) {
            createConversationOverlay.setVisible(false);
            createConversationOverlay.setManaged(false);
        }
        conversationNameField.clear();
        availableUsersList.getSelectionModel().clearSelection();
    }
    @FXML
    public void onSubmitCreateConversation() {
        List<String> selectedUsers =
                availableUsersList.getSelectionModel().getSelectedItems();

        if (selectedUsers.isEmpty()) {
            System.err.println("Select at least one user.");
            return;
        }

        List<String> members = new ArrayList<>();

        // always include current user
        members.add(currentUser.getUsername());

        for (String s : selectedUsers) {
            members.add(s);
        }

        String name = conversationNameField.getText().trim();

        client.createConversation(name, members);

        onCloseCreateConversation();
    }
    @FXML
    public void onOpenAddFriend() {
        if (addFriendOverlay != null) {
            addFriendOverlay.setVisible(true);
            addFriendOverlay.setManaged(true);
        }
        friendUserNameField.clear();
    }

    @FXML
    public void onCloseAddFriend() {
        if (addFriendOverlay != null) {
            addFriendOverlay.setVisible(false);
            addFriendOverlay.setManaged(false);
        }
        friendUserNameField.clear();
    }
    @FXML
    public void onSubmitAddFriend() {
        String friendUsername = friendUserNameField.getText().trim();

        if (friendUsername.isEmpty()) return;

        List<String> members = new ArrayList<>();
        members.add(friendUsername);
        if (currentUser != null) members.add(currentUser.getUsername());
        client.createConversation(null, members);
        System.out.println("[ADD_FRIEND] Added friend: " + friendUsername);
        onCloseAddFriend();
    }


    public void cleanup() {
        client.disconnect();
    }
}