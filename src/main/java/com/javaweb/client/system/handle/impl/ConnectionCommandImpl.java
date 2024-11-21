package com.javaweb.client.system.handle.impl;

import com.javaweb.client.system.handle.ConnectionCommand;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;

@Component
public class ConnectionCommandImpl implements ConnectionCommand {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    @Override
    public void openConnection(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Đọc phản hồi từ server khi kết nối thành công
            String response = reader.readLine();
            System.out.println("Server response: " + response);

            System.out.println("Connected to " + serverAddress + " on port " + port);
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    @Override
    public void closeConnection() {
        try {
            if (writer != null) {
                writer.write("QUIT\r\n");
                writer.flush();
            }
            disconnect();
        } catch (IOException e) {
            System.out.println("Error during closing connection: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Disconnected from the server.");
        } catch (IOException e) {
            System.out.println("Error during disconnect: " + e.getMessage());
        }
    }

    @Override
    public void bye() {
        System.out.println("Closing connection gracefully...");
        closeConnection();
    }

    @Override
    public void quit() {
        System.out.println("Quitting the session...");
        closeConnection();
    }
}
