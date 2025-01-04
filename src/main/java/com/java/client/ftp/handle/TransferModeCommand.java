package com.java.client.ftp.handle;

public interface TransferModeCommand {
    boolean activeMode(String commandToServer);
    boolean passiveMode(String commandToServer);
}
