package com.java.client.ftp.handle.impl;

import com.java.client.ftp.system.FTPClient;
import com.java.client.ftp.enums.CommandToServer;
import com.java.client.ftp.handle.ConnectionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConnectionCommandImpl implements ConnectionCommand {

    private final FTPClient ftpClient;

    @Override
    public void openConnection(String serverAddress, Integer serverPort) {
        try {
            ftpClient.connect(serverAddress, serverPort);
        } catch (Exception e) {
            System.out.println("Error while connecting: " + e.getMessage());
        }
    }

    @Override
    public void closeConnection() {
        try {
            ftpClient.close();
        } catch (Exception e) {
            System.out.println("Error while closing connection: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        closeConnection();
    }

    @Override
    public void bye() {
        try {
            ftpClient.sendToServer(CommandToServer.QUIT.name());
            closeConnection();
        } catch (Exception e) {
            System.out.println("Error while sending 'QUIT' command: " + e.getMessage());
        }
    }

    @Override
    public void quit() {
        bye();
    }
}
