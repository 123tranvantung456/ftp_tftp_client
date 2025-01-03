package com.java.client.ftp.handle;

public interface ConnectionCommand {
    void openConnection(String serverAddress, Integer serverPort, String username, String password);
    void closeConnection();
    void disconnect();
    void bye();
    void quit();
}