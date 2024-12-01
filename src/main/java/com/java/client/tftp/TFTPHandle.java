package com.java.client.tftp;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class TFTPHandle {
    public static final int BUFFER_SIZE = 516;
    public static final short OP_RRQ = 1;
    public static final short OP_WRQ = 2;
    public static final short OP_DAT = 3;
    public static final short OP_ACK = 4;
    public static final short OP_ERR = 5;


    public void handleRequest(short requestType, String filePath, String type) {
        String server = "localhost";
        String portText = "69";
        String mode = "";
        if (type.equals("Binary")){
            mode = "octet";
        } else if (type.equals("Ascii")){
            mode = "netascii";
        }

//        if (server.isEmpty()) {
//            System.out.println("Error: Server address cannot be empty.");
//            return;
//        }
//        if (portText.isEmpty() || !portText.matches("\\d+")) {
//            System.out.println("Error: Port must be a valid number.");
//            return;
//        }
        if (filePath.isEmpty()) {
            System.out.println("Error: File path cannot be empty.");
            return;
        }

        File file = new File(filePath);
        String fileName = file.getName();

//        if (requestType == OP_RRQ) {
//            fileName = filePath;
//        }

        if (requestType == OP_WRQ && (!file.exists() || !file.canRead())) {
            System.out.println("TFTP: Can't read from local file '" + filePath + "'");
            return;
        }

        int port = Integer.parseInt(portText);

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(server);

            byte[] request = createRequest(requestType, fileName, mode);
            DatagramPacket packet = new DatagramPacket(request, request.length, serverAddress, port);
            socket.send(packet);

            if (requestType == OP_RRQ) {
                handleDownload(socket, fileName, mode);
            } else if (requestType == OP_WRQ) {
                handleUpload(socket, filePath, serverAddress, mode);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public byte[] createRequest(short requestType, String fileName, String mode) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(ByteBuffer.allocate(2).putShort(requestType).array());
            baos.write(fileName.getBytes());
            baos.write(0);
            baos.write(mode.getBytes());
            baos.write(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public void handleDownload(DatagramSocket socket, String fileName, String mode) throws IOException {
        long startTime = System.currentTimeMillis();
        int totalBytes = 0;

        try (FileOutputStream fos = new FileOutputStream(fileName);
             OutputStream os = (mode.equals("netascii")
                     ? new NetAsciiOutputStream(fos)
                     : fos)) {

            boolean receiving = true;
            short expectedBlock = 1;

            while (receiving) {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                ByteBuffer wrap = ByteBuffer.wrap(packet.getData());
                short opcode = wrap.getShort();

                if (opcode == OP_DAT) {
                    short block = wrap.getShort();
                    if (block == expectedBlock) {
                        int dataLength = packet.getLength() - 4;
                        os.write(packet.getData(), 4, dataLength);
                        totalBytes += dataLength;

                        sendAck(socket, packet.getAddress(), packet.getPort(), block);
                        expectedBlock++;

                        if (packet.getLength() < BUFFER_SIZE) {
                            receiving = false;
                        }
                    }
                } else if (opcode == OP_ERR) {
                    handleError(wrap);
                    return;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        double durationSeconds = (endTime - startTime) / 1000.0;
        double speed = totalBytes / durationSeconds;

//        System.out.println(String.format("Download complete: %s (%d bytes in %.2f second(s), %.2f bytes/s)",
//            fileName, totalBytes, durationSeconds, speed));
        System.out.println(String.format("Transfer successful: %d bytes in %.2f second(s), %.2f bytes/s",
                totalBytes, durationSeconds, speed));
    }


    public void handleUpload(DatagramSocket socket, String filePath, InetAddress serverAddress , String mode) throws IOException {
        long startTime = System.currentTimeMillis();
        int totalBytes = 0;

        try (FileInputStream fis = new FileInputStream(filePath);
             InputStream is = (mode.equals("netascii")
                     ? new NetAsciiInputStream(fis)
                     : fis)) {

            short blockNumber = 0;

            byte[] ackBuffer = new byte[516]; // Tăng kích thước buffer để nhận cả các gói lỗi lớn hơn
            DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);

            socket.receive(ackPacket);
            ByteBuffer wrap = ByteBuffer.wrap(ackBuffer, 0, ackPacket.getLength()); // Chỉ đọc dữ liệu thực tế trong gói
            short opcode = wrap.getShort();

            if (opcode == OP_ERR) { // Gói lỗi
                handleError(wrap);
                return;
            } else if (opcode != OP_ACK) {
                System.out.println("Error: Expected ACK but received different opcode");
                return;
            }

            short ackBlock = wrap.getShort();
            if (ackBlock != 0) {
                System.out.println("Error: Expected ACK for block 0 but received ACK for block " + ackBlock);
                return;
            }

            int serverPort = ackPacket.getPort();

            boolean sending = true;
            while (sending) {
                byte[] buffer = new byte[512];
                int bytesRead = is.read(buffer);

                if (bytesRead == -1) {
                    bytesRead = 0; // EOF
                }

                totalBytes += bytesRead;

                DatagramPacket dataPacket = createDataPacket(++blockNumber, buffer, bytesRead, serverAddress, serverPort);
                socket.send(dataPacket);

                ackBuffer = new byte[516]; // Tạo lại buffer mới với kích thước đầy đủ
                ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);

                socket.setSoTimeout(5000); // Đặt thời gian chờ 5 giây
                try {
                    socket.receive(ackPacket);
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout waiting for ACK, resending block " + blockNumber);
                    socket.send(dataPacket);
                    continue;
                }

                wrap = ByteBuffer.wrap(ackBuffer, 0, ackPacket.getLength());
                opcode = wrap.getShort();

                if (opcode == OP_ACK) {
                    ackBlock = wrap.getShort();
                    if (ackBlock == blockNumber) {
                        if (bytesRead < 512) {
                            sending = false; // Gói cuối cùng, kết thúc
                        }
                    }
                } else if (opcode == OP_ERR) {
                    handleError(wrap);
                    return;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        double durationSeconds = (endTime - startTime) / 1000.0;
        double speed = totalBytes / durationSeconds;

//      System.out.println(String.format("Transfer successful: %s (%d bytes in %.2f second(s), %.2f bytes/s)",
//      filePath, totalBytes, durationSeconds, speed));
        System.out.println(String.format("Transfer successful: %d bytes in %.2f second(s), %.2f bytes/s",
                totalBytes, durationSeconds, speed));
    }

    public DatagramPacket createDataPacket(short blockNumber, byte[] data, int length, InetAddress serverAddress, int port) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + length);
        buffer.putShort(OP_DAT);
        buffer.putShort(blockNumber);
        buffer.put(data, 0, length);

        return new DatagramPacket(buffer.array(), buffer.position(), serverAddress, port);
    }

    private void sendAck(DatagramSocket socket, InetAddress address, int port, short blockNumber) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putShort(OP_ACK);
        buffer.putShort(blockNumber);

        DatagramPacket ackPacket = new DatagramPacket(buffer.array(), buffer.position(), address, port);
        socket.send(ackPacket);
    }

    public void handleError(ByteBuffer buffer) {
        short errorCode = buffer.getShort();
        byte[] errorMsg = new byte[buffer.remaining()];
        buffer.get(errorMsg);
        System.out.println("Error: " + errorCode + " - " + new String(errorMsg));
    }
}