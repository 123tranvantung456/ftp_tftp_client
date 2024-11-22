package com.java.client.ftp.system;

import com.java.client.ftp.util.PrintUtil;
import org.springframework.stereotype.Component;
import java.io.*;
import java.net.*;

@Component
public class FTPClient {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public void connect(String serverAddress, Integer port) throws IOException {
        socket = new Socket(serverAddress, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        PrintUtil.printToConsole("Connected to server: " + serverAddress + " on port " + port);
    }

    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            PrintUtil.printToConsole("Connection closed.");
        }
    }

    public void sendCommand(String command) {
        try{
            writer.write(command);
            writer.newLine();
            writer.flush();
            PrintUtil.printToConsole("Sent to server: " + command);
        }
        catch (IOException e){
            PrintUtil.printToConsole(e.getMessage());
        }
    }

    public String receiveCommand() {
        String response = null;
        try {
            response = reader.readLine();
        } catch (IOException e) {
            PrintUtil.printToConsole(e.getMessage());
        }
        PrintUtil.printToConsole("Received from server: " + response);
        return response;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }
}
