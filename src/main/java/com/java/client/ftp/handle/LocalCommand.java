package com.java.client.ftp.handle;

public interface LocalCommand {
    void debug();
    void active();
    void passive();
    void changeLocalDirectory(String localDirectory);
    void showStatus();
}