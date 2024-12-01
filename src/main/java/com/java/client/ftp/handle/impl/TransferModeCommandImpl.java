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
    public void activeMode(String commandToServer) {
        ActiveType activeType = clientConfig.getActiveTypeDefault();
        try {
            switch (activeType) {
                case PORT:
                    executePortMode(commandToServer);
                    break;
                case EPRT:
                    executeEprtMode(commandToServer);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported active mode type: " + activeType);
            }
        } catch (IOException e) {
            PrintUtil.printErrorToConsole("Active mode error: " + e.getMessage());
        }
    }

    private void executePortMode(String commandToServer) throws IOException {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int localPort = serverSocket.getLocalPort();
            String localHost = InetAddress.getLocalHost().getHostAddress();
            String[] ipParts = localHost.split("\\.");
            int p1 = localPort / 256;
            int p2 = localPort % 256;
            String portCommandArg = String.join(",", ipParts[0], ipParts[1], ipParts[2], ipParts[3], String.valueOf(p1), String.valueOf(p2));
            ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.PORT, portCommandArg));
            handleResponseActiveMode(commandToServer, serverSocket);
        } catch (Exception e) {
            PrintUtil.printErrorToConsole("Active mode error: " + e.getMessage());
        }
    }

    private void executeEprtMode(String commandToServer) throws IOException {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int localPort = serverSocket.getLocalPort();
            String localHost = InetAddress.getLocalHost().getHostAddress();
            String extendPortArg = "|1|" + "192.168.1.15" + "|" + localPort + "|";
            ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.EPRT, extendPortArg));
            handleResponseActiveMode(commandToServer, serverSocket);
        } catch (Exception e) {
            PrintUtil.printErrorToConsole("Active mode error: " + e.getMessage());
        }
    }

    private void handleResponseActiveMode(String commandToServer, ServerSocket serverSocket) throws IOException {
        ftpClient.receiveCommand();
        ftpClient.sendCommand(commandToServer);
        Socket socket = serverSocket.accept();
        String response = ftpClient.receiveCommand();
        if (ResponseCodeUtil.getResponseCode(response) != ResponseCode.FILE_STARTING_TRANSFER) {
            PrintUtil.printToConsole("Error: transfer did not start.");
            return;
        }
        socketData.setServerSocket(serverSocket);
        socketData.setSocket(socket);
    }

    @Override
    public void passiveMode(String commandToServer) {
        PassiveType passiveType = clientConfig.getPassiveTypeDefault();
        try {
            switch (passiveType) {
                case PASV:
                    executePasvMode(commandToServer);
                    break;
                case EPSV:
                    executeEpsvMode(commandToServer);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported passive mode type: " + passiveType);
            }
        } catch (IOException e) {
            PrintUtil.printErrorToConsole("Passive mode error: " + e.getMessage());
        }
    }

    private void executePasvMode(String commandToServer) throws IOException {
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.PASV));
        String response = ftpClient.receiveCommand();
        handlePasvResponse(response, commandToServer);
    }

    private void executeEpsvMode(String commandToServer) throws IOException {
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.EPSV));
        String response = ftpClient.receiveCommand();
        handleEpsvResponse(response, commandToServer);
    }

    private void handlePasvResponse(String responsePASV, String commandToServer) throws IOException {
        ResponseCode responseCode = ResponseCodeUtil.getResponseCode(responsePASV);
        if (responseCode == ResponseCode.USER_EXIT_ACKNOWLEDGED) {
            String[] parts = responsePASV.split("\\(")[1].split("\\)")[0].split(",");
            String serverAddress = String.join(".", parts[0], parts[1], parts[2], parts[3]);
            int serverPort = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);
            Socket dataSocket = new Socket(serverAddress, serverPort);
            socketData.setSocket(dataSocket);
            ftpClient.sendCommand(commandToServer);
            String response = ftpClient.receiveCommand();
            if (ResponseCodeUtil.getResponseCode(response) != ResponseCode.FILE_STARTING_TRANSFER) {
                PrintUtil.printToConsole("Error: transfer did not start.");
            }
        } else {
            PrintUtil.printErrorToConsole("Invalid response for PASV mode: " + responsePASV);
        }
    }

    private void handleEpsvResponse(String responseEPSV, String commandToServer) throws IOException {
        ResponseCode responseCode = ResponseCodeUtil.getResponseCode(responseEPSV);
        if (responseCode == ResponseCode.USER_EXIT_ACKNOWLEDGED) {
            String portString = responseEPSV.split("\\|")[3];
            int serverPort = Integer.parseInt(portString);
            String serverAddress = ftpClient.getServerIpAddress();
            if (serverAddress == null) {
                PrintUtil.printErrorToConsole("Server address is not available for EPSV mode.");
                return;
            }
            ftpClient.sendCommand(commandToServer);
            Socket dataSocket = new Socket(serverAddress, serverPort); // de duoi String response = ftpClient.receiveCommand(); la cook : 425 Failed to establish connection.
            socketData.setSocket(dataSocket); // de tren ftpClient.sendCommand(commandToServer); cung duoc
            String response = ftpClient.receiveCommand();
            if (ResponseCodeUtil.getResponseCode(response) != ResponseCode.FILE_STARTING_TRANSFER) {
                PrintUtil.printToConsole("Error: transfer did not start.");
            }
        } else {
            PrintUtil.printErrorToConsole("Invalid response for EPSV mode: " + responseEPSV);
        }
    }
}