package com.chatterbox.lan.network;

import java.io.*;
import java.net.Socket;

public class ServerThread implements Runnable {
    private Socket clientSocket;
    public ServerThread(Socket clientSocket){
        this.clientSocket = clientSocket;
    }
     @Override
    public void run() {
         try (
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
         ) {

             String message;
             while ((message = in.readLine()) != null) {
                 System.out.println("Received message: " + message);
                 // Broadcast the message to all clients (for simplicity, we just echo it back to the sender)
                 out.println("Echo: " + message);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
}
