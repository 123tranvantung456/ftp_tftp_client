package com.java.client.ftp.handle;

public interface LocalCommand {
    void debug();
    void changeLocalDirectory(String localDirectory);
    void sendLiteralCommand(String command);
    void togglePromptMode(boolean enable);
    void toggleGlobMode(boolean enable);
    void toggleTraceMode(boolean enable);
    void toggleVerboseMode(boolean enable);
    void showStatus();
    void toggleHashMode(boolean enable);
    void displayHelp();
}
