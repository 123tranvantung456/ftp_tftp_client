package com.java.client.ftp.handle;

public interface DataTransferCommand {
    void setAsciiMode();
    void setBinaryMode();
    void activeMode();
    void passiveMode();
}