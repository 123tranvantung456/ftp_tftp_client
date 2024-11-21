package com.java.client.ftp.handle;

public interface ConnectionCommand {
    void openConnection(String serverAddress, Integer serverPort);
    void closeConnection();
    void disconnect();
    void bye();
    void quit();
}