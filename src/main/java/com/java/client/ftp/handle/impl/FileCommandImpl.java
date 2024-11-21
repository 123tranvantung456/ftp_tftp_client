package com.java.client.ftp.handle.impl;

import com.java.client.ftp.handle.FileCommand;
import org.springframework.stereotype.Component;

@Component
public class FileCommandImpl implements FileCommand {
    @Override
    public void put(String filename) {

    }

    @Override
    public void multiPut(String[] filenames) {

    }

    @Override
    public void get(String filename) {

    }

    @Override
    public void multiGet(String[] filenames) {

    }

    @Override
    public void delete(String remoteFilePath) {

    }

    @Override
    public void multiDelete(String[] remoteFilePaths) {

    }

    @Override
    public void send(String localFilePath, String remoteFilePath) {

    }

    @Override
    public void receive(String remoteFilePath, String localFilePath) {

    }

    @Override
    public void append(String localFilePath, String remoteFilePath) {

    }
}
