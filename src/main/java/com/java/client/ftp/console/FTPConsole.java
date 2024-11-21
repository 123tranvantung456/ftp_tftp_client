package com.java.client.ftp.console;

import com.java.client.ftp.router.Router;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class FTPConsole {

    private final Router router;

    public void start() {
        Scanner scanner = new Scanner(System.in);
        String commandString;

        System.out.println("FTP Console is running... Type 'exit' to quit.");
        while (true) {
            System.out.print("Enter command: ");
            commandString = scanner.nextLine().trim();

            if (commandString.equalsIgnoreCase("exit")) {
                System.out.println("Exiting FTP Console.");
                break;
            }

            router.routeCommand(commandString);
        }
        scanner.close();
    }

}
