package com.chatterbox.lan.network;

import com.chatterbox.lan.database.*;
import com.chatterbox.lan.models.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventHandler {

    private final ConcurrentHashMap<String, SocketWrapper> connectedClients;
    private final MessageRepo messageRepo;
    private final UserRepo userRepo;
    private final ConversationRepo conversationRepo;

    public EventHandler(
            ConcurrentHashMap<String, SocketWrapper> connectedClients,
            MessageRepo messageRepo,
            UserRepo userRepo,
            ConversationRepo conversationRepo
    ) {
        this.connectedClients = connectedClients;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
        this.conversationRepo = conversationRepo;
    }

    public synchronized void handleEvent(String username, Event event) {
        try {
            switch (event.getType()) {
                case "LOGIN" -> handleLogin(username);
                case "SEND_MESSAGE" -> handleSendMessage(username, event.getConversationId(), event.getText());
                case "GET_MESSAGES" -> handleGetMessages(username, event.getConversationId());
                case "GET_USERS" -> handleGetUsers(username);
                default -> System.err.println("[UNKNOWN Event] " + event.getType());
            }
        } catch (Exception e) {
            System.err.println("[HANDLER] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLogin(String username) {
        System.out.println("[LOGIN] " + username);
        // Optional: could broadcast "user online" event here
    }

    private void handleSendMessage(String username, String conversationId, String text) {
        User sender = userRepo.getUserByUsername(username);
        if (sender == null) {
            sender = new User(username, "/avatars/default.png");
        }
        Message message = new Message(sender, text, conversationId);

        messageRepo.saveMessage(message);
        System.out.println("[MSG] [" + conversationId + "] " + username + ": " + text);

        broadcastMessage(message);
    }

    private void handleGetMessages(String username, String conversationId) throws IOException {
        List<Message> messages = messageRepo.getMessages(conversationId);

        Event resp = new Event("MESSAGES_RESPONSE");
        resp.setConversationId(conversationId);
        resp.setData("messages", messages);

        SocketWrapper client = connectedClients.get(username);
        if (client != null) {
            client.write(resp);
        }
    }

    private void handleGetUsers(String username) throws IOException {
        // Get all conversations of the user
        List<Conversation> conversations = conversationRepo.getUserConversations(username);

        Event event = new Event("USERS_UPDATED");
        event.setData("conversations", conversations);

        SocketWrapper client = connectedClients.get(username);
        if (client != null) {
            client.write(event);
        }
    }

    private void broadcastMessage(Message message) {
        String conversationId = message.getConversationId();

        Event broadcastEvent = new Event("NEW_MESSAGE");
        broadcastEvent.setConversationId(conversationId);
        broadcastEvent.setUsername(message.getSender().getUsername());
        broadcastEvent.setText(message.getText());

        Conversation conversation = conversationRepo.getConversation(conversationId);
        if (conversation == null) {
            System.err.println("[BROADCAST] Conversation not found: " + conversationId);
            return;
        }

        List<String> members = conversation.getMembers();
        System.out.println("[BROADCAST] Sending to conversation members: " + members);

        for (String memberUsername : members) {
            SocketWrapper clientSocket = connectedClients.get(memberUsername);
            if (clientSocket != null) {
                try {
                    clientSocket.write(broadcastEvent);
                    System.out.println("[BROADCAST] Sent to " + memberUsername);
                } catch (IOException e) {
                    System.err.println("[BROADCAST] Failed to send to " + memberUsername + ": " + e.getMessage());
                }
            }
        }
    }
}