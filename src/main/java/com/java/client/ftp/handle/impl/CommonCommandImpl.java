package com.java.client.ftp.handle.impl;
import com.java.client.ftp.handle.CommonCommand;
import org.springframework.stereotype.Component;

@Component
public class CommonCommandImpl implements CommonCommand {
    @Override
    public void listDetail(String remoteDirectory) {

    }

    @Override
    public void listDetailAndStore(String remoteDirectory, String outputFile) {

    }

    @Override
    public void list(String remoteDirectory) {

    }

    @Override
    public void listAndStore(String remoteDirectory, String outputFile) {

    }

    @Override
    public void rename(String oldName, String newName) {

    }
}
