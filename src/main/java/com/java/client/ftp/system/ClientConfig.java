package com.java.client.ftp.system;

import com.java.client.ftp.enums.TransferMode;
import com.java.client.ftp.enums.TransferType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class ClientConfig {
    private String currentDirectory = Const.DEFAULT_DIRECTORY;
    private TransferType transferType = TransferType.ASCII;
    private TransferMode transferModeDefault = TransferMode.ACTIVE;
}