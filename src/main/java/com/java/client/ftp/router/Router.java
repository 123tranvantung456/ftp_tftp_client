package com.java.client.ftp.router;

import com.java.client.ftp.enums.CommandOfClient;

public interface Router {
    void routeCommand(String commandString);
}
