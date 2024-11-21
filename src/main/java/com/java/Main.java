package com.java;

import com.java.client.ftp.console.FTPConsole;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        System.out.println("client start");
        ApplicationContext context = new AnnotationConfigApplicationContext("com.java");
        FTPConsole ftpConsole = context.getBean(FTPConsole.class);
        ftpConsole.start();
    }
}