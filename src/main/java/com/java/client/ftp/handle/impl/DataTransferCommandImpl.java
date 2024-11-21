package com.java.client.ftp.handle.impl;

import com.java.client.ftp.system.ClientConfig;
import com.java.client.ftp.system.FTPClient;
import com.java.client.ftp.enums.CommandToServer;
import com.java.client.ftp.enums.ResponseCode;
import com.java.client.ftp.enums.TransferType;
import com.java.client.ftp.handle.DataTransferCommand;
import com.java.client.ftp.util.ResponseCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class DataTransferCommandImpl implements DataTransferCommand {
    private final FTPClient ftpClient;
    private final ClientConfig clientConfig;

    @Override
    public void setAsciiMode() {
        clientConfig.setTransferType(TransferType.ASCII);
        try {
            ftpClient.sendToServer(CommandToServer.TYPE.name() + " A");
            String response = ftpClient.receiveFromServer();
            ResponseCode responseCode = ResponseCodeUtil.getResponseCode(response);

            if (responseCode == ResponseCode.OK) {
                System.out.println("Switched to ASCII mode.");
            } else {
                System.out.println("Failed to switch to ASCII mode. Response: " + response);
            }
        } catch (IOException e) {
            System.out.println("Error while setting ASCII mode: " + e.getMessage());
        }
    }

    @Override
    public void setBinaryMode() {
        clientConfig.setTransferType(TransferType.BINARY);
        try {
            ftpClient.sendToServer(CommandToServer.TYPE.name() + " I");
            String response = ftpClient.receiveFromServer();
            ResponseCode responseCode = ResponseCodeUtil.getResponseCode(response);

            if (responseCode == ResponseCode.OK) {
                System.out.println("Switched to Binary mode.");
            } else {
                System.out.println("Failed to switch to Binary mode. Response: " + response);
            }
        } catch (IOException e) {
            System.out.println("Error while setting Binary mode: " + e.getMessage());
        }
    }

    @Override
    public void activeMode() {
        try {
            ftpClient.sendToServer(CommandToServer.EPRT.name() + " |1|127.0.0.1|12345|");
            String response = ftpClient.receiveFromServer();
            ResponseCode responseCode = ResponseCodeUtil.getResponseCode(response);

            if (responseCode == ResponseCode.OK) {
                System.out.println("Switched to Active mode.");
            } else {
                System.out.println("Failed to switch to Active mode. Response: " + response);
            }
        } catch (IOException e) {
            System.out.println("Error while switching to Active mode: " + e.getMessage());
        }
    }

    @Override
    public void passiveMode() {
        try {
            ftpClient.sendToServer(CommandToServer.EPSV.name());
            String response = ftpClient.receiveFromServer();
            ResponseCode responseCode = ResponseCodeUtil.getResponseCode(response);

            if (responseCode == ResponseCode.OK) {
                System.out.println("Switched to Passive mode.");
            } else {
                System.out.println("Failed to switch to Passive mode. Response: " + response);
            }
        } catch (IOException e) {
            System.out.println("Error while switching to Passive mode: " + e.getMessage());
        }
    }
}
