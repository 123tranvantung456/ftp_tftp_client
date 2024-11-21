package com.javaweb.client.system.handle.impl;

import com.javaweb.client.system.handle.FileCommand;
import org.springframework.stereotype.Component;

@Component
public class FileCommandImpl implements FileCommand {
    @Override
    public void put(String localFilePath, String remoteFilePath) {

    }

    @Override
    public void multiPut(String[] localFilePaths, String remoteDirectory) {

    }

    @Override
    public void get(String remoteFilePath, String localFilePath) {

    }

    @Override
    public void multiGet(String[] remoteFilePaths, String localDirectory) {

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
