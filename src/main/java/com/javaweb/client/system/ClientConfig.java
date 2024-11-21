package com.javaweb.client.system;

import com.javaweb.client.system.enums.TransferMode;
import com.javaweb.client.system.enums.TransferType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class ClientConfig {
    private String currentDirectory = Const.DEFAULT_DIRECTORY;
    private TransferType transferType = TransferType.ASCII;
    private TransferMode transferMode = TransferMode.ACTIVE;
}
