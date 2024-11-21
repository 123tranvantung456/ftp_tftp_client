package com.javaweb.client.system.handle;

public interface DirectoryCommand {
    void makeDirectory(String remoteDirectory);
    void removeDirectory(String remoteDirectory);
    void changeDirectory(String remoteDirectory);
    void printWorkingDirectory();
}
