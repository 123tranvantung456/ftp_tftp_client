package com.java.client.ftp.handle.impl;

import com.java.client.ftp.enums.TransferMode;
import com.java.client.ftp.handle.LocalCommand;
import com.java.client.ftp.system.ClientConfig;
import com.java.client.ftp.util.PrintUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalCommandImpl implements LocalCommand {
    private final ClientConfig clientConfig;

    @Override
    public void debug() {
        clientConfig.setDebug(!clientConfig.isDebug());
    }

    @Override
    public void active() {
        clientConfig.setTransferModeDefault(TransferMode.ACTIVE);
        PrintUtil.printToConsole("change to active mode");
    }

    @Override
    public void passive() {
        clientConfig.setTransferModeDefault(TransferMode.PASSIVE);
        PrintUtil.printToConsole("change to passive mode");
    }

    @Override
    public void changeLocalDirectory(String localDirectory) {
        clientConfig.setCurrentDirectory(localDirectory);
        PrintUtil.printToConsole("change to current directory " + localDirectory);
    }

    @Override
    public void sendLiteralCommand(String command) {

    }

    @Override
    public void togglePromptMode(boolean enable) {

    }

    @Override
    public void toggleGlobMode(boolean enable) {

    }

    @Override
    public void toggleTraceMode(boolean enable) {

    }

    @Override
    public void toggleVerboseMode(boolean enable) {

    }

    @Override
    public void showStatus() {

        PrintUtil.printToConsole("debug: " + (clientConfig.isDebug() ? "on" : "off"));

        PrintUtil.printToConsole("Transfer mode default: " + clientConfig.getTransferModeDefault());

        PrintUtil.printToConsole("Active mode default: " + clientConfig.getActiveTypeDefault());

        PrintUtil.printToConsole("Passive mode default: " + clientConfig.getPassiveTypeDefault());

        PrintUtil.printToConsole("Transfer type: " + clientConfig.getTransferType());

        PrintUtil.printToConsole("Current directory: " + clientConfig.getCurrentDirectory());

        PrintUtil.printToConsole("Login status: " + (clientConfig.isLogin() ? "Logged in" : "Not logged in"));
    }

    @Override
    public void toggleHashMode(boolean enable) {

    }

    @Override
    public void displayHelp() {

    }
}
