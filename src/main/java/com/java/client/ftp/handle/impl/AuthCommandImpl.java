package com.java.client.ftp.handle.impl;

import com.java.client.ftp.enums.CommandToServer;
import com.java.client.ftp.enums.ResponseCode;
import com.java.client.ftp.handle.AuthCommand;
import com.java.client.ftp.system.ClientConfig;
import com.java.client.ftp.system.FTPClient;
import com.java.client.ftp.util.PrintUtil;
import com.java.client.ftp.util.ResponseCodeUtil;
import com.java.client.ftp.util.SendToServerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class AuthCommandImpl implements AuthCommand {
    private final FTPClient ftpClient;
    private final ClientConfig clientConfig;

    @Override
    public void login() {
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine().trim();
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.USER, username));
        String response = ftpClient.receiveCommand();
        PrintUtil.printToConsole(response);
        if (ResponseCodeUtil.getResponseCode(response) == ResponseCode.NEED_PASSWORD){
            String password = scanner.nextLine().trim();
            ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.PASS, password));
        }
        scanner.close();
    }
}