package com.java.client.ftp.handle.impl;

import com.java.client.ftp.enums.CommandToServer;
import com.java.client.ftp.handle.PermissionCommand;
import com.java.client.ftp.handle.TransferModeCommand;
import com.java.client.ftp.router.SocketData;
import com.java.client.ftp.system.ClientConfig;
import com.java.client.ftp.system.FTPClient;
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
public class PermissionCommandImpl implements PermissionCommand {
    private final ClientConfig clientConfig;
    private final FTPClient ftpClient;
    private final TransferModeCommand transferModeCommand;
    private final SocketData socketData;

    @Override
    public boolean createPermission(String argToServer) {
//        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.CPER, argToServer));
//        if (ftpClient.receiveCommand().startsWith("200")){
//            return true;
//        }
//        return false;
        System.out.println("createPermission: " + argToServer);
        return true;
    }

    @Override
    public boolean deletePermission(String argToServer ) {
//        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.DPER, argToServer));
//        if (ftpClient.receiveCommand().startsWith("200")){
//            return true;
//        }
        System.out.println("deletePermission: " + argToServer);
        return false;
    }

    @Override
    public List<String> getPermission(long itemId) {
//        TransferModeUtil.handleTransferMode(clientConfig, transferModeCommand, SendToServerUtil.message(CommandToServer.PER, itemId + ""));
//        if (ftpClient.receiveCommand().startsWith("200")) {
//            return dataFromServer();
//        }
//        return null;
        return Arrays.asList(
                "USER",
                "tung",
                "van",
                "khanh",
                "dev",
                "USER PERMISSION",
                "dang READ",
                "xuan WRITE",
                "heo ALL"
        );
    }

    private List<String> dataFromServer() {
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
