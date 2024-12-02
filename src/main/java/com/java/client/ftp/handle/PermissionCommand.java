package com.java.client.ftp.handle;

import java.util.*;

public interface PermissionCommand {
    boolean createPermission(String argToServer);
    boolean deletePermission(String argToServer);
    List<String> getPermission(long itemId);
}
