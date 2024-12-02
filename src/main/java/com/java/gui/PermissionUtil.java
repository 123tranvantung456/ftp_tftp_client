package com.java.gui;

import java.util.*;

public class PermissionUtil {
    public static boolean checkEnableMenuPermission(String permission) {
        return true;
    }

    public static List<String> searchByUsername(String keyword, List<String> usernames) {
        // Danh sách kết quả trả về
        List<String> result = new ArrayList<>();

        if (keyword == null || keyword.isEmpty() || usernames == null) {
            return result; // Trả về danh sách rỗng nếu từ khóa hoặc danh sách đầu vào không hợp lệ
        }

        // Duyệt qua từng tên người dùng
        for (String username : usernames) {
            if (username != null && username.toLowerCase().contains(keyword.toLowerCase())) {
                result.add(username); // Thêm tên người dùng khớp vào danh sách kết quả
            }
        }

        return result;
    }

    public static List<String> getUser(List<String> responseFromServer) {
        List<String> result = new ArrayList<>();
        if(responseFromServer != null && !responseFromServer.isEmpty()) {
            if (responseFromServer.get(0).equals("USER")){
                for (int i = 1; i < responseFromServer.size(); i++) {
                    if(responseFromServer.get(i).equals("USER PERMISSION")){
                        return result;
                    }
                    result.add(responseFromServer.get(i));
                }
            }
        }
        return result;
    }

    public static List<UserPermission> getUserPermission(List<String> responseFromServer) {
        List<UserPermission> result = new ArrayList<>();
        if(responseFromServer != null && !responseFromServer.isEmpty()) {
            if (responseFromServer.get(0).equals("USER")){
                for (int i = responseFromServer.size() - 1; i >= 0; i--) {
                    if (responseFromServer.get(i).equals("USER PERMISSION")) {
                        return result;
                    }
                    String[] data = responseFromServer.get(i).split(" ");
                    result.add(
                            UserPermission.builder()
                                    .username(data[0])
                                    .permission(data[1])
                                    .build()
                    );
                }
            }
        }
        return result;
    }
}
