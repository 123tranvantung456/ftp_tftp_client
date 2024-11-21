package com.java.client.ftp.system;

import org.springframework.stereotype.Component;
import java.io.*;
import java.net.*;

@Component
public class FTPClient {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public void connect(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        System.out.println("Connected to server: " + serverAddress + " on port " + port);
    }

    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("Connection closed.");
        }
    }

    public void sendToServer(String command) throws IOException {
        writer.write(command);
        writer.newLine();
        writer.flush();
        System.out.println("Sent to server: " + command);
    }

    public String receiveFromServer() throws IOException {
        String response = reader.readLine();
        System.out.println("Received from server: " + response);
        return response;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }
}
