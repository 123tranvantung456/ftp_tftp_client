package com.java.client.ftp.handle.impl;

import com.java.client.ftp.handle.LocalCommand;
import com.java.client.ftp.system.ClientConfig;
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
