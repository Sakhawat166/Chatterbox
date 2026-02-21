package com.chatterbox.lan.network;

import com.chatterbox.lan.utils.env;
import java.io.*;
import java.net.Socket;

public class Client {
    public Client() {
        String PORT = env.getPORT();
        try (
                Socket clientSocket = new Socket("localhost", PORT);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        ) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
