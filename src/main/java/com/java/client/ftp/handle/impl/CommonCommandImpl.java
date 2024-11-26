package com.java.client.ftp.handle.impl;

import com.java.client.ftp.enums.CommandToServer;
import com.java.client.ftp.enums.TransferType;
import com.java.client.ftp.handle.CommonCommand;
import com.java.client.ftp.handle.DataTransferCommand;
import com.java.client.ftp.handle.TransferModeCommand;
import com.java.client.ftp.router.SocketData;
import com.java.client.ftp.system.ClientConfig;
import com.java.client.ftp.system.FTPClient;
import com.java.client.ftp.util.PrintUtil;
import com.java.client.ftp.util.SendToServerUtil;
import com.java.client.ftp.util.TransferModeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.*;

@Component
@RequiredArgsConstructor
public class CommonCommandImpl implements CommonCommand {

    private final FTPClient ftpClient;
    private final ClientConfig clientConfig;
    private final SocketData socketData;
    private final DataTransferCommand transferCommand;
    private final TransferModeCommand transferModeCommand;

    @Override
    public void listDetail(String remoteDirectory) {

    }

    @Override
    public void listDetailAndStore(String remoteDirectory, String outputFile) {

    }

    @Override
    public List<String> listName(String remoteDirectory) {
        List<String> result = new ArrayList<>();
        if (clientConfig.getTransferType() == TransferType.ASCII) {
            result = listNameHandle(remoteDirectory);
            ftpClient.receiveCommand();
        } else if (clientConfig.getTransferType() == TransferType.BINARY) {
            transferCommand.setAsciiMode();
            result = listNameHandle(remoteDirectory);
            ftpClient.receiveCommand();
            transferCommand.setBinaryMode();
        }
        return result;
    }

    @Override
    public void listNameAndStore(String remoteDirectory, String outputFile) {

    }

    @Override
    public void rename(String oldName, String newName) {

    }

    private List<String> listNameHandle(String remoteDirectory) {
        TransferModeUtil.handleTransferMode(clientConfig, transferModeCommand,
                SendToServerUtil.message(CommandToServer.NLST, remoteDirectory));
        List<String> fileList = listNameFromServer();
        StringBuilder fileListString = new StringBuilder();
        if (!fileList.isEmpty()) {
            for (String fileName : fileList) {
                fileListString.append(fileName).append("\n");
            }
            if (!fileListString.isEmpty() && fileListString.charAt(fileListString.length() - 1) == '\n') {
                fileListString.deleteCharAt(fileListString.length() - 1);
            }
        } else {
            fileListString.append("No files found in the directory.");
        }
        PrintUtil.printToConsole(fileListString.toString());
        return fileList;
    }

    private List<String> listNameFromServer() {
        List<String> fileList = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socketData.getSocket().getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    fileList.add(line.trim());
                }
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error retrieving file list: " + e.getMessage());
        }
        return fileList;
    }
}
