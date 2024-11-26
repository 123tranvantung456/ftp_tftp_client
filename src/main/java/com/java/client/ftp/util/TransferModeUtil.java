package com.java.client.ftp.util;

import com.java.client.ftp.enums.TransferMode;
import com.java.client.ftp.handle.TransferModeCommand;
import com.java.client.ftp.system.ClientConfig;

public class TransferModeUtil {
    public static void handleTransferMode(ClientConfig clientConfig, TransferModeCommand transferModeCommand, String commandToServer) {
        if (clientConfig.getTransferModeDefault() == TransferMode.ACTIVE) {
            transferModeCommand.activeMode(commandToServer);
        } else if (clientConfig.getTransferModeDefault() == TransferMode.PASSIVE) {
            transferModeCommand.passiveMode(commandToServer);
        }
    }
}
