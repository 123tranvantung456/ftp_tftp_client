package com.java.client.ftp.handle.impl;

import com.java.client.ftp.handle.AuthCommand;
import com.java.client.ftp.system.Const;
import com.java.client.ftp.system.FTPClient;
import com.java.client.ftp.enums.CommandToServer;
import com.java.client.ftp.handle.ConnectionCommand;
import com.java.client.ftp.util.PrintUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConnectionCommandImpl implements ConnectionCommand {

    private final FTPClient ftpClient;
    private final AuthCommand authCommand;

    @Override
    public void openConnection(String serverAddress, Integer serverPort, String username, String password) {
        try {
                serverAddress = Const.FTP_ADDRESS;
                serverPort = Const.FTP_PORT;
                username = "tranvantung";
                password = "hellotung";

            ftpClient.connect(serverAddress, serverPort);
            ftpClient.receiveCommand();
            authCommand.login(username, password);
        } catch (Exception e) {
            PrintUtil.printToConsole("Error while connecting: " + e.getMessage());
        }
    }

    @Override
    public void closeConnection() {
        try {
            ftpClient.close();
        } catch (Exception e) {
            PrintUtil.printToConsole("Error while closing connection: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        closeConnection();
    }

    @Override
    public void bye() {
        try {
            ftpClient.sendCommand(CommandToServer.QUIT.name());
            closeConnection();
        } catch (Exception e) {
            PrintUtil.printToConsole("Error while sending 'QUIT' command: " + e.getMessage());
        }
    }

    @Override
    public void quit() {
        bye();
    }
}
