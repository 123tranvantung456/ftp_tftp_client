package com.javaweb.client.system.handle;

public interface ConnectionCommand {
    void openConnection(String serverAddress, int port);
    void closeConnection();
    void disconnect();
    void bye();
    void quit();
}
