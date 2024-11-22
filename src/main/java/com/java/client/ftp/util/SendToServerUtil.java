package com.java.client.ftp.util;

import com.java.client.ftp.enums.CommandToServer;

public class SendToServerUtil {
    public static String message(CommandToServer commandToServer, String arg){
        return commandToServer + " " + arg;
    }
    public static String message(CommandToServer commandToServer){
        return commandToServer.name();
    }
}
