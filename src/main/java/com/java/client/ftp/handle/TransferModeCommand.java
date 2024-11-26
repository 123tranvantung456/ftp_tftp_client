package com.java.client.ftp.handle;

public interface TransferModeCommand {
    void activeMode(String commandToServer);
    void passiveMode(String commandToServer);
}
