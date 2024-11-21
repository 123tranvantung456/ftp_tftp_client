package com.java.client.ftp.handle.impl;

import com.java.client.ftp.handle.LocalCommand;
import org.springframework.stereotype.Component;

@Component
public class LocalCommandImpl implements LocalCommand {
    @Override
    public void changeLocalDirectory(String localDirectory) {

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

    }

    @Override
    public void toggleHashMode(boolean enable) {

    }

    @Override
    public void displayHelp() {

    }
}
