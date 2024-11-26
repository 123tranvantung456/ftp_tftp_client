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
    private final Scanner scanner;

    @Override
    public void login() {
        System.out.print("User: ");
        String username = scanner.nextLine().trim();
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.USER, username));
        String responseWithUsername = ftpClient.receiveCommand();
        if (ResponseCodeUtil.getResponseCode(responseWithUsername) == ResponseCode.NEED_PASSWORD){
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.PASS, password));
        }
        String responseWithPassword = ftpClient.receiveCommand();
        if(ResponseCodeUtil.getResponseCode(responseWithPassword) == ResponseCode.USER_LOGGED_IN){
            clientConfig.setLogin(true);
        }
    }
}
