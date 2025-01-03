package com.java.client.ftp.handle;

public interface DirectoryCommand {
    boolean makeDirectory(String remoteDirectory);
    boolean removeDirectory(String remoteDirectory);
    void changeDirectory(String remoteDirectory);
    void printWorkingDirectory();
}
