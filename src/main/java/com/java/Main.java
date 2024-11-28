package com.java;

import com.java.client.ftp.console.FTPConsole;
import com.java.gui.Client;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("client start");
        ApplicationContext context = new AnnotationConfigApplicationContext("com.java");
        FTPConsole ftpConsole = context.getBean(FTPConsole.class);
        ftpConsole.start();
        SwingUtilities.invokeLater(Client::new);
    }
}