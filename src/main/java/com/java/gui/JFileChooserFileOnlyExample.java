package com.java.gui;
import javax.swing.*;
import java.io.File;

public class JFileChooserFileOnlyExample {
    public static void main(String[] args) {
        // Tạo một JFrame để chứa JFileChooser
        JFrame frame = new JFrame("Chọn tệp - Không cho phép thư mục");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null); // Hiển thị ở giữa màn hình

        // Tạo JFileChooser
        JFileChooser fileChooser = new JFileChooser();

        // Đặt chế độ chỉ cho phép chọn tệp
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Mở hộp thoại chọn tệp
        int result = fileChooser.showOpenDialog(frame);

        // Kiểm tra kết quả lựa chọn
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Kiểm tra xem lựa chọn có phải là tệp không
            if (selectedFile.isFile()) {
                System.out.println("Bạn đã chọn tệp: " + selectedFile.getAbsolutePath());
            } else {
                System.out.println("Lỗi: Vui lòng chỉ chọn tệp, không chọn thư mục.");
            }
        } else if (result == JFileChooser.CANCEL_OPTION) {
            System.out.println("Người dùng đã hủy lựa chọn.");
        }

        // Đóng chương trình sau khi chọn (hoặc giữ JFrame mở)
        frame.dispose();
    }
}
