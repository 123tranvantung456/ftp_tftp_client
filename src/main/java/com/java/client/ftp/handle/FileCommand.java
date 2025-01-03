package com.java.client.ftp.handle;

public interface FileCommand {
    void put(String filename);
    void multiPut(String[] filenames);
    void get(String filename);
    void multiGet(String[] filenames);
    boolean delete(String filename);
    void multiDelete(String[] filenames);
    void send(String localFilePath, String remoteFilePath);
    void receive(String localFilePath, String remoteFilePath);
    void append(String localFilePath, String remoteFilePath);
}