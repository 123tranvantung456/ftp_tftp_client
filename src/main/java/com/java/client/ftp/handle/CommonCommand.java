package com.java.client.ftp.handle;

public interface CommonCommand {
    void listDetail(String remoteDirectory);
    void listDetailAndStore(String remoteDirectory, String outputFile);
    void list(String remoteDirectory);
    void listAndStore(String remoteDirectory, String outputFile);
    void rename(String oldName, String newName);
}
