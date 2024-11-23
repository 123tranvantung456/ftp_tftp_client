package com.java.client.ftp.handle.impl;

import com.java.client.ftp.enums.CommandToServer;
import com.java.client.ftp.enums.ResponseCode;
import com.java.client.ftp.enums.ActiveType;
import com.java.client.ftp.enums.PassiveType;
import com.java.client.ftp.handle.TransferModeCommand;
import com.java.client.ftp.router.SocketData;
import com.java.client.ftp.system.ClientConfig;
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
    private final ClientConfig clientConfig;

    @Override
    public void activeMode() {
        ActiveType activeType = clientConfig.getActiveTypeDefault();
        try {
            switch (activeType) {
                case PORT:
                    executePortMode();
                    break;
                case EPRT:
                    executeEprtMode();
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported active mode type: " + activeType);
            }
        } catch (IOException e) {
            PrintUtil.printErrorToConsole("Active mode error: " + e.getMessage());
        }
    }

    private void executePortMode() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {  // Đảm bảo đóng serverSocket sau khi sử dụng
            int localPort = serverSocket.getLocalPort();
            String localHost = InetAddress.getLocalHost().getHostAddress();
            String[] ipParts = localHost.split("\\.");
            int p1 = localPort / 256;
            int p2 = localPort % 256;
            String portCommandArg = String.join(",", ipParts[0], ipParts[1], ipParts[2], ipParts[3], String.valueOf(p1), String.valueOf(p2));
            ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.PORT, portCommandArg));
            Socket dataSocket = serverSocket.accept();
            socketData.setSocket(dataSocket);
            ftpClient.receiveCommand();
        }
    }

    private void executeEprtMode() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {  // Đảm bảo đóng serverSocket sau khi sử dụng
            int localPort = serverSocket.getLocalPort();
            String localHost = InetAddress.getLocalHost().getHostAddress();
            String extendPortArg = "|1|" + localHost + "|" + localPort + "|";
            ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.EPRT, extendPortArg));
            Socket dataSocket = serverSocket.accept();
            socketData.setSocket(dataSocket);
            ftpClient.receiveCommand();
        }
    }

    @Override
    public void passiveMode() {
        PassiveType passiveType = clientConfig.getPassiveTypeDefault();
        try {
            switch (passiveType) {
                case PASV:
                    executePasvMode();
                    break;
                case EPSV:
                    executeEpsvMode();
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported passive mode type: " + passiveType);
            }
        } catch (IOException e) {
            PrintUtil.printErrorToConsole("Passive mode error: " + e.getMessage());
        }
    }

    private void executePasvMode() throws IOException {
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.PASV));
        String response = ftpClient.receiveCommand();
        handlePasvResponse(response);
    }

    private void executeEpsvMode() throws IOException {
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.EPSV));
        String response = ftpClient.receiveCommand();
        handleEpsvResponse(response);
    }

    private void handlePasvResponse(String response) throws IOException {
        ResponseCode responseCode = ResponseCodeUtil.getResponseCode(response);
        if (responseCode == ResponseCode.USER_EXIT_ACKNOWLEDGED) {
            String[] parts = response.split("\\(")[1].split("\\)")[0].split(",");
            String serverAddress = String.join(".", parts[0], parts[1], parts[2], parts[3]);
            int serverPort = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);
            Socket dataSocket = new Socket(serverAddress, serverPort);
            socketData.setSocket(dataSocket);
        } else {
            PrintUtil.printErrorToConsole("Invalid response for PASV mode: " + response);
        }
    }

    private void handleEpsvResponse(String response) throws IOException {
        ResponseCode responseCode = ResponseCodeUtil.getResponseCode(response);
        if (responseCode == ResponseCode.USER_EXIT_ACKNOWLEDGED) {
            String portString = response.split("\\|")[3];
            int serverPort = Integer.parseInt(portString);
            String serverAddress = ftpClient.getServerIpAddress();
            if (serverAddress == null) {
                PrintUtil.printErrorToConsole("Server address is not available for EPSV mode.");
                return;
            }
            Socket dataSocket = new Socket(serverAddress, serverPort);
            socketData.setSocket(dataSocket);
        } else {
            PrintUtil.printErrorToConsole("Invalid response for EPSV mode: " + response);
        }
    }
}