package com.java.client.ftp.handle;

public interface AuthCommand {
    void login(String username, String password);
}