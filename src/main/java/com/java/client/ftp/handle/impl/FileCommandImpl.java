package com.java.client.ftp.handle.impl;

import com.java.client.ftp.enums.CommandToServer;
import com.java.client.ftp.enums.ResponseCode;
import com.java.client.ftp.enums.TransferMode;
import com.java.client.ftp.enums.TransferType;
import com.java.client.ftp.handle.CommonCommand;
import com.java.client.ftp.handle.FileCommand;
import com.java.client.ftp.handle.TransferModeCommand;
import com.java.client.ftp.router.SocketData;
import com.java.client.ftp.system.ClientConfig;
import com.java.client.ftp.system.FTPClient;
import com.java.client.ftp.util.PrintUtil;
import com.java.client.ftp.util.ResponseCodeUtil;
import com.java.client.ftp.util.SendToServerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.io.*;

@Component
@RequiredArgsConstructor
public class FileCommandImpl implements FileCommand {

    private final FTPClient ftpClient;
    private final ClientConfig clientConfig;
    private final SocketData socketData;

    private final TransferModeCommand transferModeCommand;
    private final CommonCommand commonCommand;

    @Override
    public void put(String filename) {
        File file = new File(clientConfig.getCurrentDirectory() + "/" + filename);
        if (!file.exists()) {
            PrintUtil.printToConsole(file + " does not exist");
        }
        else {
            if (clientConfig.getTransferModeDefault() == TransferMode.ACTIVE){
                transferModeCommand.activeMode();
            }
            else if (clientConfig.getTransferModeDefault() == TransferMode.PASSIVE){
                transferModeCommand.passiveMode();
            }
            ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.STOR, filename));
            if (clientConfig.getTransferType() == TransferType.ASCII){
                putWithAsciiMode(file);
            }
            else if(clientConfig.getTransferType() == TransferType.BINARY){
                putWithBinaryMode(file);
            }
            socketData.checkConnection();
            socketData.closeSockets();
            ftpClient.receiveCommand();
        }
    }

    @Override
    public void multiPut(String[] filenames) {
        for (String filename : filenames) {
            put(filename);
        }
    }

    @Override
    public void get(String filename) {
        File file = new File(clientConfig.getCurrentDirectory() + "/" + filename);
        if (clientConfig.getTransferModeDefault() == TransferMode.ACTIVE){
            transferModeCommand.activeMode();
        }
        else if (clientConfig.getTransferModeDefault() == TransferMode.PASSIVE){
            transferModeCommand.passiveMode();
        }

        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.RETR, filename));

        if (clientConfig.getTransferType() == TransferType.ASCII){
            getWithAsciiMode(file);
        }
        else if(clientConfig.getTransferType() == TransferType.BINARY){
            getWithBinaryMode(file);
        }
        socketData.checkConnection();
        ftpClient.receiveCommand();
    }

    @Override
    public void multiGet(String[] filenames) {
        for (final String filename : filenames) {
            List<String> filenamesFromServer = commonCommand.listName(filename);
            for (String filenameFromServer : filenamesFromServer) {
                get(filenameFromServer);
            }
        }
    }

    @Override
    public void delete(String remoteFilePath) {
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.DELE, remoteFilePath));
        ftpClient.receiveCommand();
    }

    @Override
    public void multiDelete(String[] remoteFilePaths) {
        for (final String filename : remoteFilePaths) {
            List<String> filenamesFromServer = commonCommand.listName(filename);
            for (String filenameFromServer : filenamesFromServer) {
                get(filenameFromServer);
            }
        }
    }

    @Override
    public void send(String localFilePath, String remoteFilePath) {
        File file = new File(clientConfig.getCurrentDirectory() + "/" + localFilePath);
        if (!file.exists()) {
            PrintUtil.printToConsole(file + " does not exist");
        }
        else {
            if (clientConfig.getTransferModeDefault() == TransferMode.ACTIVE){
                transferModeCommand.activeMode();
            }
            else if (clientConfig.getTransferModeDefault() == TransferMode.PASSIVE){
                transferModeCommand.passiveMode();
            }
            ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.STOR, remoteFilePath));
            if (clientConfig.getTransferType() == TransferType.ASCII){
                putWithAsciiMode(file);
            }
            else if(clientConfig.getTransferType() == TransferType.BINARY){
                putWithBinaryMode(file);
            }
//            socketData.closeSockets();
            socketData.checkConnection();
            ftpClient.receiveCommand();
        }
    }

    @Override
    public void receive(String remoteFilePath, String localFilePath) {
        File file = new File(clientConfig.getCurrentDirectory() + "/" + localFilePath);

        if (clientConfig.getTransferModeDefault() == TransferMode.ACTIVE){
            transferModeCommand.activeMode();
        }
        else if (clientConfig.getTransferModeDefault() == TransferMode.PASSIVE){
            transferModeCommand.passiveMode();
        }
        ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.RETR, remoteFilePath));
        if (clientConfig.getTransferType() == TransferType.ASCII){
            getWithAsciiMode(file);
        }
        else if(clientConfig.getTransferType() == TransferType.BINARY){
            getWithBinaryMode(file);
        }
        socketData.checkConnection();
        ftpClient.receiveCommand();
    }

    @Override
    public void append(String localFilePath, String remoteFilePath) {
        File file = new File(clientConfig.getCurrentDirectory() + "/" + localFilePath);
        if (!file.exists()) {
            PrintUtil.printToConsole(file + " does not exist");
        }
        else {
            if (clientConfig.getTransferModeDefault() == TransferMode.ACTIVE){
                transferModeCommand.activeMode();
            }
            else if (clientConfig.getTransferModeDefault() == TransferMode.PASSIVE){
                transferModeCommand.passiveMode();
            }
            ftpClient.sendCommand(SendToServerUtil.message(CommandToServer.APPE, remoteFilePath));

            if (clientConfig.getTransferType() == TransferType.ASCII){
                putWithAsciiMode(file);
            }
            else if(clientConfig.getTransferType() == TransferType.BINARY){
                putWithBinaryMode(file);
            }
//            socketData.closeSockets();
            socketData.checkConnection();
            ftpClient.receiveCommand();
        }
    }

    private void putWithAsciiMode(File file){
        if (ResponseCodeUtil.getResponseCode(ftpClient.receiveCommand()) != ResponseCode.FILE_STARTING_TRANSFER){
            PrintUtil.printToConsole(ftpClient.receiveCommand());
            return;
        }
        BufferedReader rin = null;
        PrintWriter rout = null;

        try {
            rin = new BufferedReader(new FileReader(file));
            rout = new PrintWriter(socketData.getSocket().getOutputStream(), true);
        }
        catch (IOException e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }
        catch (Exception e) {
            PrintUtil.printErrorToConsole("err: " + e.getMessage());
        }

        String s;

        try {
            while ((s = rin.readLine()) != null) {
                rout.println(s);
            }
        } catch (IOException e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }

        try {
            rout.close();
            rin.close();
        } catch (IOException e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }
    }

    private void putWithBinaryMode(File file){
        if (ResponseCodeUtil.getResponseCode(ftpClient.receiveCommand()) != ResponseCode.FILE_STARTING_TRANSFER){
            PrintUtil.printToConsole("failed");
            return;
        }
        BufferedOutputStream fout = null;
        BufferedInputStream fin = null;

        try {
            fout = new BufferedOutputStream(socketData.getSocket().getOutputStream());
            fin = new BufferedInputStream(new FileInputStream(file));
        }
        catch (IOException e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }
        catch (Exception e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }

        byte[] buf = new byte[1024];
        int l = 0;
        try {
            while ((l = fin.read(buf, 0, 1024)) != -1) {
                fout.write(buf, 0, l);
            }
        } catch (IOException e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }
        try {
            fin.close();
            fout.close();
        } catch (IOException e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }
    }

    private void getWithAsciiMode(File file) {
        if (ResponseCodeUtil.getResponseCode(ftpClient.receiveCommand()) != ResponseCode.FILE_STARTING_TRANSFER){
            PrintUtil.printToConsole("failed");
            return;
        }
        BufferedReader rin = null;
        PrintWriter rout = null;
        try {
            rin = new BufferedReader(new InputStreamReader(socketData.getSocket().getInputStream()));
            rout = new PrintWriter(new FileOutputStream(file), true);
        }
        catch (IOException e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }
        catch (Exception e) {
            PrintUtil.printErrorToConsole("err: " + e.getMessage());
        }
        try {
            String s = "";
            while ((s = rin.readLine()) != null) {
                if (rout != null) {
                    rout.println(s);
                }
            }
        } catch (IOException e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }
        try {
            if (rout != null) {
                rout.close();
            }
            rin.close();
        } catch (IOException e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }
    }

    private void getWithBinaryMode(File file){
        if (ResponseCodeUtil.getResponseCode(ftpClient.receiveCommand()) != ResponseCode.FILE_STARTING_TRANSFER){
            PrintUtil.printToConsole("failed");
            return;
        }
        BufferedOutputStream fout = null;
        BufferedInputStream fin = null;
        try {
            fout = new BufferedOutputStream(new FileOutputStream(file));
            fin = new BufferedInputStream(socketData.getSocket().getInputStream());
        }
        catch (IOException e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }
        catch (Exception e) {
            PrintUtil.printErrorToConsole("err: " + e.getMessage());
        }

        byte[] buf = new byte[1024];
        int l = 0;
        try {
            while ((l = fin.read(buf, 0, 1024)) != -1) {
                if (fout != null) {
                    fout.write(buf, 0, l);
                }
            }
        } catch (IOException e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }
        try {
            fin.close();
            if (fout != null) {
                fout.close();
            }
        } catch (IOException e) {
            PrintUtil.printErrorToConsole(e.getMessage());
        }
    }
}