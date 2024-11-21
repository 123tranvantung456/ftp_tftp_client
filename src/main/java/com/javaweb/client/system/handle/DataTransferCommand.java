package com.javaweb.client.system.handle;

public interface DataTransferCommand {
    void setAsciiMode();
    void setBinaryMode();
    void sendQuoteCommand(String command);
    void setActiveMode();
    void setPassiveMode();
}
