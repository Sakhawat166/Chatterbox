package com.chatterbox.lan.network;

import com.chatterbox.lan.models.*;
import com.chatterbox.lan.utils.env;
import java.io.*;


public class Client {
    private SocketWrapper socketWrapper;
    private String username;

    private volatile MessageListener messageListener;

    public interface MessageListener {
        void onMessageReceived(Event event);
    }

    public Client() {
        try {
            int PORT = env.getPORT();
            this.socketWrapper = new SocketWrapper("localhost", PORT);
            // Start listening thread
            Thread readThread = new Thread(
                    new ClientThread(socketWrapper, this),
                    "client-read-thread"
            );
            readThread.setDaemon(true);
            readThread.start();
            System.out.println("[CLIENT] Connected to server on port " + PORT);
        } catch (IOException e) {
            System.err.println("[CLIENT] Failed to connect: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void login(String username) {
        this.username = username;
        Event req = new Event("LOGIN");
        req.setUsername(username);
        sendRequest(req);
    }


    public void sendMessage(String conversationId, String text) {
        Event req = new Event("SEND_MESSAGE");
        req.setConversationId(conversationId);
        req.setText(text);
        sendRequest(req);
    }

    public void getMessages(String conversationId) {
        Event req = new Event("GET_MESSAGES");
        req.setConversationId(conversationId);
        sendRequest(req);
    }

    public void getUsers() {
        Event req = new Event("GET_USERS");
        req.setUsername(username);
        sendRequest(req);
    }

    private void sendRequest(Event event) {
        try {
            if (socketWrapper != null) {
                socketWrapper.write(event);
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Error sending request: " + e.getMessage());
        }
    }

    // Called by ReadThreadClient
    public void handleIncomingEvent(Event event) {
        MessageListener listener = messageListener;
        if (listener != null) {
            listener.onMessageReceived(event);
        }
    }



    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public void disconnect() {
        try {
            if (socketWrapper != null) {
                socketWrapper.closeConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }
}