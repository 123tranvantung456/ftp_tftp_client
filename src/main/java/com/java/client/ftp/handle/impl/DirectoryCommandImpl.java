package com.java.client.ftp.handle.impl;

import com.java.client.ftp.enums.CommandToServer;
import com.java.client.ftp.handle.DirectoryCommand;
import com.java.client.ftp.system.FTPClient;
import com.java.client.ftp.util.SendToServerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectoryCommandImpl implements DirectoryCommand {
    private final FTPClient ftpClient;

    @Override
    public boolean makeDirectory(String remoteDirectory) {
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.XMKD, remoteDirectory));
        return ftpClient.receiveCommand().startsWith("257");
    }

    @Override
    public boolean removeDirectory(String remoteDirectory) {
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.XRMD, remoteDirectory));
        return ftpClient.receiveCommand().startsWith("257");
    }

    @Override
    public void changeDirectory(String remoteDirectory) {
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.CWD, remoteDirectory));
        ftpClient.receiveCommand();
    }

    @Override
    public void printWorkingDirectory() {
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.XPWD));
        ftpClient.receiveCommand();
    }
}
