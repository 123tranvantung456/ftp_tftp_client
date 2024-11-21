package com.java.client.ftp.handle;

public interface DirectoryCommand {
    void makeDirectory(String remoteDirectory);
    void removeDirectory(String remoteDirectory);
    void changeDirectory(String remoteDirectory);
    void printWorkingDirectory();
}
