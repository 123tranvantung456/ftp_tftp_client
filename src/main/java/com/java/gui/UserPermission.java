package com.java.gui;

import com.java.client.ftp.enums.Permission;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserPermission {
    private String username;
    private String permission;
}
