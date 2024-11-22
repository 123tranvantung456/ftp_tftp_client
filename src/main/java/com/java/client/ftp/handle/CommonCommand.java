package com.java.client.ftp.handle;

import java.util.*;

public interface CommonCommand {
    void listDetail(String remoteDirectory);
    void listDetailAndStore(String remoteDirectory, String outputFile);
    List<String> listName(String remoteDirectory);
    void listNameAndStore(String remoteDirectory, String outputFile);
    void rename(String oldName, String newName);
}
