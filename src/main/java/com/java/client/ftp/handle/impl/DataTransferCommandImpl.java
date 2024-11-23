package com.java.client.ftp.handle.impl;

import com.java.client.ftp.system.ClientConfig;
import com.java.client.ftp.system.FTPClient;
import com.java.client.ftp.enums.CommandToServer;
import com.java.client.ftp.enums.ResponseCode;
import com.java.client.ftp.enums.TransferType;
import com.java.client.ftp.handle.DataTransferCommand;
import com.java.client.ftp.util.ResponseCodeUtil;
import com.java.client.ftp.util.SendToServerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataTransferCommandImpl implements DataTransferCommand {
    private final FTPClient ftpClient;
    private final ClientConfig clientConfig;

    @Override
    public void setAsciiMode() {
        clientConfig.setTransferType(TransferType.ASCII);
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.TYPE, "A"));
        String response = ftpClient.receiveCommand();
        ResponseCode responseCode = ResponseCodeUtil.getResponseCode(response);
        // if fail => set lai o local
    }

    @Override
    public void setBinaryMode() {
        clientConfig.setTransferType(TransferType.BINARY);
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.TYPE, "I"));
        String response = ftpClient.receiveCommand();
        ResponseCode responseCode = ResponseCodeUtil.getResponseCode(response);
        // if fail => set lai o local
    }
}
