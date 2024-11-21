package com.javaweb.client.system.enums;

public enum CommandToServer {
    USER,
    PASS,
    QUIT,
    PORT,
    EPRT,
    PASV,
    EPSV,
    TYPE,
    STOR,
    RETR,
    APPE,
    DELE,
    LIST,
    NLST,
    RNFR,
    RNTO,
    CWD,
    XPWD,
    XMKD,
    XRMD;

    public static CommandToServer fromString(String command) {
        try {
            return CommandToServer.valueOf(command.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
