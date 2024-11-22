package com.java.client.ftp.handle.impl;

import com.java.client.ftp.enums.CommandToServer;
import com.java.client.ftp.enums.ResponseCode;
import com.java.client.ftp.handle.TransferModeCommand;
import com.java.client.ftp.router.SocketData;
import com.java.client.ftp.system.FTPClient;
import com.java.client.ftp.util.PrintUtil;
import com.java.client.ftp.util.ResponseCodeUtil;
import com.java.client.ftp.util.SendToServerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

@Component
@RequiredArgsConstructor
public class TransferModeCommandImpl implements TransferModeCommand {
    private final FTPClient ftpClient;
    private final SocketData socketData;

    @Override
    public void activeMode() {
        try {
            socketData.setServerSocket(new ServerSocket(0));
            int localPort = socketData.getServerSocket().getLocalPort();
            String localHost = InetAddress.getLocalHost().getHostAddress();
            String extendPortArg = "|1|" + localHost + "|" + localPort + "|";
            ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.EPRT, extendPortArg));
            Socket dataSocket = socketData.getServerSocket().accept();
            socketData.setSocket(dataSocket);
            String response = ftpClient.receiveCommand();
            PrintUtil.printToConsole(response);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void passiveMode() {
        try {
            ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.PASV));
            String response = ftpClient.receiveCommand();
            PrintUtil.printToConsole(response);
            ResponseCode responseCode = ResponseCodeUtil.getResponseCode(response);
            if(responseCode == ResponseCode.USER_EXIT_ACKNOWLEDGED) {
                String[] parts = response.split("\\(")[1].split("\\)")[0].split(",");
                String serverAddress = String.join(".", parts[0], parts[1], parts[2], parts[3]);
                int serverPort = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);
                Socket dataSocket = new Socket(serverAddress, serverPort);
                socketData.setSocket(dataSocket);
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}