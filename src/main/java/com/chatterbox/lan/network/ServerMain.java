package com.chatterbox.lan.network;

import com.chatterbox.lan.utils.env;

import java.net.*;

public class ServerMain {
    public ServerMain() {
        try(ServerSocket serverSocket = new ServerSocket(env.getPORT())){
            System.out.println("Server is running on port " + env.getPORT());
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                ServerThread serverThread = new ServerThread(clientSocket);
                Thread thread = new Thread(serverThread);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
