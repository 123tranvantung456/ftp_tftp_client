package com.java.client.ftp.console;

import com.java.client.ftp.router.Router;
import com.java.client.ftp.system.ClientConfig;
import com.java.client.ftp.system.Const;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class FTPConsole {
    private final Router router;
    private final Scanner scanner;
    private final ClientConfig clientConfig;

    public void start() {
        String commandString;

        System.out.println("FTP Console is running... Type 'exit' to quit.");
        while (true) {
            if(clientConfig.isLogin()){
                System.out.print("ftp> ");
            }
            else {
                System.out.print(Const.DEFAULT_DIRECTORY + "> ");
            }
            commandString = scanner.nextLine().trim();
            if (commandString.equalsIgnoreCase("exit")) {
                System.out.println("Exiting FTP Console.");
                break;
            }
            router.routeCommand(commandString);
        }
    }
}
