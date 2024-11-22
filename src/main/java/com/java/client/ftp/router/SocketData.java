package com.java.client.ftp.router;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Component
@Setter
@Getter
public class SocketData {
    private Socket socket;
    private ServerSocket serverSocket;

    public void checkConnection() {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            System.out.println("Socket is connected and open.");
        } else {
            System.out.println("Socket is not connected or closed.");
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            System.out.println("Server socket is open.");
        } else {
            System.out.println("Server socket is closed.");
        }
    }

    public void closeSockets() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Data socket closed.");
            }

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server socket closed.");
            }

        } catch (IOException e) {
            System.err.println("Error closing data socket.");
        }
    }
}