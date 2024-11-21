package com.javaweb.client.system.handle;

public interface FileCommand {
    void put(String localFilePath, String remoteFilePath);
    void multiPut(String[] localFilePaths, String remoteDirectory);
    void get(String remoteFilePath, String localFilePath);
    void multiGet(String[] remoteFilePaths, String localDirectory);
    void delete(String remoteFilePath);
    void multiDelete(String[] remoteFilePaths);
    void send(String localFilePath, String remoteFilePath);
    void receive(String remoteFilePath, String localFilePath);
    void append(String localFilePath, String remoteFilePath);
}