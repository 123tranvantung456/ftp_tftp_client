package com.javaweb.client.system.handle;

public interface CommonCommand {
    void listDirectory(String remoteDirectory);
    void multiListDirectory(String remoteDirectory, String outputFile);
    void list(String remoteDirectory);
    void multiList(String remoteDirectory, String outputFile);
    void rename(String oldName, String newName);
}
