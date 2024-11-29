package com.java.gui;

import com.java.client.ftp.enums.CommandOfClient;
import com.java.client.ftp.handle.*;
import com.java.client.ftp.router.Router;
import com.java.client.ftp.system.ClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

@Component
public class Client extends JFrame {
    @Autowired
    private ClientConfig clientConfig;

    @Autowired
    private AuthCommand authCommand;
    @Autowired
    private CommonCommand commonCommand;
    @Autowired
    private ConnectionCommand connectionCommand;
    @Autowired
    private DataTransferCommand transferCommand;
    @Autowired
    private DirectoryCommand directoryCommand;
    @Autowired
    private FileCommand fileCommand;
    @Autowired
    private LocalCommand localCommand;


    private JTree localTree, remoteTree;
    private JTable localTable, remoteTable;
    private JTextArea logArea;
    private DefaultMutableTreeNode currentDefaultMutableTreeNodeInRemoteTree;
    private Node currentNodeInRemoteTree;

    public Client() {
        setTitle("FTP Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Các thành phần giao diện
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionPanel.add(new JLabel("Host:"));
        JTextField hostField = new JTextField(10);
        connectionPanel.add(hostField);
        connectionPanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField(10);
        connectionPanel.add(usernameField);
        connectionPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField(10);
        connectionPanel.add(passwordField);
        connectionPanel.add(new JLabel("Port:"));
        JTextField portField = new JTextField(5);
        connectionPanel.add(portField);
        JButton connectButton = new JButton("Quickconnect");
        connectionPanel.add(connectButton);

        // Khu vực log
        logArea = new JTextArea(50, 20);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        logPanel.setPreferredSize(new Dimension(1200, 150));

        // Cây và bảng local
        localTree = createLocalTree();
        localTable = createLocalTable();

        // Cây và bảng remote
        remoteTree = createRemoteTree();
        remoteTable = createRemoteTable();

        // Thêm sự kiện cho cây
        addRemoteTreeListeners();
        addLocalTreeListeners();

        // Split panes
        JSplitPane localSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(localTree), new JScrollPane(localTable));
        JSplitPane remoteSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(remoteTree), new JScrollPane(remoteTable));
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                localSplitPane, remoteSplitPane);

        // Giao diện chính
        setLayout(new BorderLayout());
        add(connectionPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);

        setVisible(true);
        System.out.println("GUI is visible!");

        // Gọi hàm xử lý khi nút được nhấn
        connectButton.addActionListener(e -> handleConnect(hostField, usernameField, passwordField, portField));
    }

    // connect
    private void handleConnect(JTextField hostField, JTextField usernameField, JPasswordField passwordField, JTextField portField) {
        String host = hostField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String portText = portField.getText();

        try {
            int port = Integer.parseInt(portText);

            // Xử lý logic kết nối (thay bằng logic thực tế nếu cần)
            if (!host.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                logArea.append("Connecting to " + host + ":" + port + " with username " + username + "\n");
                connectionCommand.openConnection(host, port, username, password);
                logArea.append("Connected successfully!\n");
            } else {
                logArea.append("Please fill in all the fields.\n");
            }
        } catch (NumberFormatException ex) {
            logArea.append("Invalid port number.\n");
        } catch (Exception ex) {
            logArea.append("Connection failed: " + ex.getMessage() + "\n");
        }
    }


    // local tree
    private JTree createLocalTree() {
        Node node = Node.builder()
                .name("This Computer")
                .path("")
                .build();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(node);
        File[] roots = File.listRoots(); // Lấy danh sách các ổ đĩa

        if (roots != null) {
            for (File rootDrive : roots) {
                Node nodeTemp = Node.builder()
                        .name(rootDrive.getPath())
                        .path(rootDrive.getPath())
                        .build();
                DefaultMutableTreeNode driveNode = new DefaultMutableTreeNode(
                        nodeTemp
                );
                Node loadingNode =  Node.builder()
                        .name("Loading...")
                        .build();
                driveNode.add(new DefaultMutableTreeNode(loadingNode)); // Placeholder để tải lười
                root.add(driveNode);
            }
        }

        DefaultTreeModel model = new DefaultTreeModel(root);
        JTree tree = new JTree(model);

        // Lắng nghe sự kiện mở rộng để tải nội dung ổ đĩa
        tree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            @Override
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent event) throws javax.swing.tree.ExpandVetoException {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                    if (node.getChildCount() == 1 && node.getFirstChild().toString().equals("Loading...")) {
                    Node objNode = (Node) node.getUserObject();
                    node.removeAllChildren();
                    File file = new File(objNode.getPath());
                        lazyLoaLocal(node, file);
                    ((DefaultTreeModel) tree.getModel()).reload(node);
                }
            }

            @Override
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent event) throws javax.swing.tree.ExpandVetoException {
                // Xử lý sự kiện thu gọn nếu cần
            }
        });

        return tree;
    }

    private void addLocalTreeListeners() {
        localTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode selectedNode =
                            (DefaultMutableTreeNode) localTree.getLastSelectedPathComponent();
                    if (selectedNode != null) {
                        File selectedFile = getFileFromNode(selectedNode);
                        if (selectedFile != null && selectedFile.isDirectory()) {
                            logArea.append("Local folder selected: " + selectedFile.getAbsolutePath() + "\n");
                            updateLocalTable(localTable, selectedFile);
                        }
                    }
                }
            }
        });
    }

    private void lazyLoaLocal(DefaultMutableTreeNode parentNode, File file) {
        File[] files = file.listFiles(File::isDirectory); // Only list directories for lazy loading
        if (files != null) {
            for (File f : files) {
                Node node = Node.builder()
                        .name(f.getName())
                        .path(((Node)parentNode.getUserObject()).getPath() + f.getName() + "\\")
                        .build();
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
                        node
                );
                Node loadingNode =  Node.builder()
                        .name("Loading...")
                        .build();
                childNode.add(new DefaultMutableTreeNode(
                        loadingNode)
                ); // Placeholder
                parentNode.add(childNode);
            }
        }
    }

    // local table
    private JTable createLocalTable() {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified", "Path"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        // Cho phép chọn nhiều dòng
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Ẩn cột Path
        table.getColumnModel().getColumn(4).setMinWidth(0);
        table.getColumnModel().getColumn(4).setMaxWidth(0);
        table.getColumnModel().getColumn(4).setWidth(0);

        // Tạo menu chuột phải
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem uploadItem = new JMenuItem("Upload");
        JMenuItem appendItem = new JMenuItem("Append");

        popupMenu.add(uploadItem);
        popupMenu.add(appendItem);

        // Thêm sự kiện chuột phải
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int[] selectedRows = table.getSelectedRows();

                    // Kiểm tra có ít nhất một dòng được chọn
                    if (selectedRows.length > 0) {
                        boolean hasFolder = false;

                        // Kiểm tra nếu có dòng nào là Folder
                        for (int row : selectedRows) {
                            String type = (String) table.getValueAt(row, 2); // Cột Type
                            if ("Folder".equalsIgnoreCase(type)) {
                                hasFolder = true;
                                break;
                            }
                        }

                        // Cập nhật trạng thái của các item trong menu
                        uploadItem.setEnabled(!hasFolder);  // Disable nếu có Folder
                        appendItem.setEnabled(selectedRows.length == 1 && !hasFolder); // Chỉ cho phép Append khi chọn đúng 1 dòng
                        popupMenu.show(table, e.getX(), e.getY());
                    }
                }
            }
        });

        // Gắn sự kiện cho các item trong menu
        uploadItem.addActionListener(e -> showUploadForm(table));
        appendItem.addActionListener(e -> showAppendForm(table));

        return table;
    }

    private  void showUploadForm(JTable table) {
        JDialog uploadDialog = new JDialog((Frame) null, "Upload", true);
        uploadDialog.setLayout(null);

        // Tạo các thành phần
        JLabel protocolLabel = new JLabel("Protocol:");
        JComboBox<String> protocolComboBox = new JComboBox<>(new String[]{"FTP", "TFTP"});
        JLabel typeLabel = new JLabel("Type:");
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Binary", "Ascii"});
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        // Đặt vị trí
        protocolLabel.setBounds(20, 20, 100, 25);
        protocolComboBox.setBounds(120, 20, 240, 25);
        typeLabel.setBounds(20, 60, 100, 25);
        typeComboBox.setBounds(120, 60, 240, 25);
        okButton.setBounds(80, 150, 80, 30);
        cancelButton.setBounds(200, 150, 100, 30);

        // Thêm vào dialog
        uploadDialog.add(protocolLabel);
        uploadDialog.add(protocolComboBox);
        uploadDialog.add(typeLabel);
        uploadDialog.add(typeComboBox);
        uploadDialog.add(okButton);
        uploadDialog.add(cancelButton);

        // Sự kiện OK
        okButton.addActionListener(e -> {
            String protocol = (String) protocolComboBox.getSelectedItem();
            String type = (String) typeComboBox.getSelectedItem();
            java.util.List<String> filesToUpload = getSelectedFilesFromLocalTable(table, false);
            System.out.println("Uploading Files: " + String.join(", ", filesToUpload));
            System.out.println("Protocol: " + protocol);
            System.out.println("Type: " + type);
            assert protocol != null;
            assert type != null;
            if (!clientConfig.getTransferType().name().equals(type.toUpperCase())){
                if (type.equals("Binary")) {
                    transferCommand.setBinaryMode();
                }
                else if (type.equals("Ascii")) {
                    transferCommand.setAsciiMode();
                }
            }
            if (protocol.equals("FTP")){
//                fileCommand.multiPut(filesToUpload.toArray(new String[0]));
                for (String file : filesToUpload) {
                    String fullPathToServer = currentNodeInRemoteTree.getPath();
                    String name;
                    if (file.contains("\\")){
                        name = file.substring(file.lastIndexOf('\\') + 1);
                    }
                    else {
                        name = file;
                    }
                    if (!fullPathToServer.isEmpty()){
                        fullPathToServer += "/" + name;
                    }
                    else {
                        fullPathToServer += name;
                    }
                    fileCommand.send(file, fullPathToServer);
                }
            }
            else if (protocol.equals("TFTP")){

            }
            uploadDialog.dispose();
        });

        // Sự kiện Cancel
        cancelButton.addActionListener(e -> uploadDialog.dispose());

        uploadDialog.setSize(400, 250);
        uploadDialog.setLocationRelativeTo(null);
        uploadDialog.setResizable(false);
        uploadDialog.setVisible(true);
    }

    private  void showAppendForm(JTable table) {
        JDialog appendDialog = new JDialog((Frame) null, "Append", true);
        appendDialog.setLayout(null);

        // Tạo các thành phần
        JLabel remoteFileLabel = new JLabel("Remote File:");
        JTextField remoteFileField = new JTextField();
        JLabel typeLabel = new JLabel("Type:");
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Binary", "Ascii"});
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        // Đặt vị trí
        remoteFileLabel.setBounds(20, 20, 100, 25);
        remoteFileField.setBounds(120, 20, 240, 25);
        typeLabel.setBounds(20, 60, 100, 25);
        typeComboBox.setBounds(120, 60, 240, 25);
        okButton.setBounds(80, 150, 80, 30);
        cancelButton.setBounds(200, 150, 100, 30);

        // Thêm vào dialog
        appendDialog.add(remoteFileLabel);
        appendDialog.add(remoteFileField);
        appendDialog.add(typeLabel);
        appendDialog.add(typeComboBox);
        appendDialog.add(okButton);
        appendDialog.add(cancelButton);

        // Sự kiện OK
        okButton.addActionListener(e -> {
            String remoteFile = remoteFileField.getText();
            String type = (String) typeComboBox.getSelectedItem();
            java.util.List<String> selectedFiles = getSelectedFilesFromLocalTable(table, false);

            if (remoteFile.isEmpty()) {
                JOptionPane.showMessageDialog(appendDialog, "Remote file path cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedFiles.isEmpty()) {
                JOptionPane.showMessageDialog(appendDialog, "No file selected from the table.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // In thông tin
            System.out.println("Appending to Remote File: " + remoteFile);
            System.out.println("Selected File(s): " + String.join(", ", selectedFiles));
            System.out.println("Type: " + type);

            if (!clientConfig.getTransferType().name().equals(type.toUpperCase())){
                if (type.equals("Binary")) {
                    transferCommand.setBinaryMode();
                }
                else if (type.equals("Ascii")) {
                    transferCommand.setAsciiMode();
                }
            }

            String fullPathToServer = currentNodeInRemoteTree.getPath();

            if (!fullPathToServer.isEmpty()){
                fullPathToServer += "/" + remoteFile;
            }
            else {
                fullPathToServer += remoteFile;
            }
            fileCommand.append(selectedFiles.get(0), fullPathToServer);
            appendDialog.dispose();
        });

        // Sự kiện Cancel
        cancelButton.addActionListener(e -> appendDialog.dispose());

        appendDialog.setSize(400, 250);
        appendDialog.setLocationRelativeTo(null);
        appendDialog.setResizable(false);
        appendDialog.setVisible(true);
    }

    private  java.util.List<String> getSelectedFilesFromLocalTable(JTable table, boolean isDownload) {
        java.util.List<String> selectedFiles = new java.util.ArrayList<>();
        int[] selectedRows = table.getSelectedRows();

        for (int row : selectedRows) {
            String fileName = (String) table.getValueAt(row, isDownload ? 0 : 4);
            selectedFiles.add(fileName);
        }

        return selectedFiles;
    }

    private File getFileFromNode(DefaultMutableTreeNode node) {
        assert node != null;
        Node nodeCrr = (Node)node.getUserObject();
        return new File(nodeCrr.getPath());
    }

    private void updateLocalTable(JTable table, File folder) {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified", "Path"}; // Thêm cột Path
        File[] files = folder.listFiles();
        Object[][] data = {};

        if (files != null) {
            data = new Object[files.length][5]; // Thêm cột thứ 5 cho Path
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                data[i][0] = f.getName(); // Name
                data[i][1] = f.isFile() ? f.length() + " bytes" : "-"; // Size
                data[i][2] = f.isDirectory() ? "Folder" : "File"; // Type
                data[i][3] = new java.util.Date(f.lastModified()); // Last Modified
                data[i][4] = f.getAbsolutePath(); // Path
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table.setModel(model);

        // Ẩn cột Path
        table.getColumnModel().getColumn(4).setMinWidth(0);
        table.getColumnModel().getColumn(4).setMaxWidth(0);
        table.getColumnModel().getColumn(4).setWidth(0);
    }


    // remote tree
    private JTree createRemoteTree() {
        Node nodeRoot = Node.builder()
                .name("Remote")
                .path("")
                .build();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(nodeRoot);
        DefaultTreeModel model = new DefaultTreeModel(root);
        JTree tree = new JTree(model);
        // addloading
        Node nodeLoading = Node.builder()
                .name("Loading...")
                .path("")
                .build();
        DefaultMutableTreeNode loadingNode = new DefaultMutableTreeNode(nodeLoading);
        root.add(loadingNode);

        tree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            @Override
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent event) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                currentNodeInRemoteTree = (Node)node.getUserObject();
                currentDefaultMutableTreeNodeInRemoteTree = node;
                // Kiểm tra nếu node có 1 con (và là node root có "Loading...") để bắt đầu load dữ liệu
                if (node.getChildCount() == 1 && node.getFirstChild().toString().equals("Loading...")) {
                    // Lấy đường dẫn của node (ví dụ lấy thư mục gốc của remote files)
                    // Xóa node "Loading..." và bắt đầu load thư mục
                    node.removeAllChildren();

                    java.util.List<String> response = commonCommand.listDetail(((Node)node.getUserObject()).getPath());
                    java.util.List<String> subFolderName = getFolderNames(response);
                    LazyLoadRemote(node, subFolderName);

                    // Cập nhật lại mô hình cây để hiển thị thư mục con
                    ((DefaultTreeModel) tree.getModel()).reload(node);
                }
            }

            @Override
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent event) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                currentNodeInRemoteTree = (Node)node.getUserObject();
                currentDefaultMutableTreeNodeInRemoteTree = node;
            }
        });

        return tree;
    }

    private void addRemoteTreeListeners() {
        remoteTree.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 2) {
//                    DefaultMutableTreeNode selectedNode =
//                            (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();
//                    if (selectedNode != null) {
//                        String selectedFolder = selectedNode.toString();
//                        logArea.append("Remote folder selected: " + selectedFolder + "\n");
//                        java.util.List<String> response = commonCommand.listDetail(((Node)selectedNode.getUserObject()).getPath());
//                        updateRemoteTable(remoteTable, processListFileAndFolder(response));
//                    }
//                }
//            }
@Override
public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() == 2) {
        TreePath path = remoteTree.getPathForLocation(e.getX(), e.getY()); // Lấy node tại vị trí click
        if (path != null) {
            remoteTree.setSelectionPath(path); // Chọn node
            DefaultMutableTreeNode selectedNode =
                    (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                String selectedFolder = selectedNode.toString();
                logArea.append("Remote folder selected: " + selectedFolder + "\n");
                java.util.List<String> response = commonCommand.listDetail(((Node) selectedNode.getUserObject()).getPath());
                updateRemoteTable(remoteTable, processListFileAndFolder(response));
            }
        }
    }
}
        });
    }

    private void LazyLoadRemote(DefaultMutableTreeNode node, java.util.List<String> subFolder) {
        // Duyệt qua danh sách các thư mục con và thêm chúng vào cây
        for (String folderName : subFolder) {
            String pathCurr = ((Node) node.getUserObject()).getPath();
            if (!pathCurr.equals("")) {
                pathCurr += "/"+ folderName;
            }
            else {
                pathCurr += folderName;
            }
            Node File = Node.builder()
                    .name(folderName)
                    .path(pathCurr)
                    .build();
            DefaultMutableTreeNode subFolderNode = new DefaultMutableTreeNode(File);

            // Thêm node thư mục con vào cây (vẫn giữ node gốc)
            node.add(subFolderNode);

            // Thêm node "Loading..." vào các thư mục con để tiếp tục lazy load khi mở thư mục con
            Node nodeLoading = Node.builder()
                    .name("Loading...")
                    .path("")
                    .build();
            subFolderNode.add(new DefaultMutableTreeNode(nodeLoading));
        }
    }

    private java.util.List<String> getFolderNames(java.util.List<String> ftpData) {

        java.util.List<String> folderNames = new java.util.ArrayList<>();

        for (String line : ftpData) {
            String[] parts = line.split("\\s+", 9); // Tách dòng thành các trường

            if (parts.length < 9) {
                continue; // Bỏ qua dòng không hợp lệ
            }

            String permissions = parts[0]; // Quyền (vd: drwxr-xr-x)
            String name = parts[8];        // Tên file/folder

            if (permissions.startsWith("d")) { // Nếu quyền bắt đầu bằng 'd', là thư mục
                folderNames.add(name);
            }
        }

        return folderNames; // Trả về danh sách thư mục
    }


    // remote table
    private JTable createRemoteTable() {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified"}; // Không có cột Path
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        // Cho phép chọn nhiều dòng
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Tạo menu chuột phải
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem downloadItem = new JMenuItem("Download");
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem uploadItem = new JMenuItem("Upload");
        JMenuItem createItem = new JMenuItem("Create folder");
        JMenuItem renameItem = new JMenuItem("Rename");

        // Thêm các action listener cho các menu item
        downloadItem.addActionListener(ev -> showDownloadForm(table));
        deleteItem.addActionListener(ev -> handleDeleteFiles(table));
        uploadItem.addActionListener(ev -> handleUploadFile(table));
        createItem.addActionListener(ev -> handleCreateFolder(table));
        renameItem.addActionListener(ev -> handleRenameFile(table, table.getSelectedRow()));

        popupMenu.add(renameItem);
        popupMenu.add(downloadItem);
        popupMenu.add(deleteItem);
        popupMenu.add(uploadItem);
        popupMenu.add(createItem);

        // Thêm sự kiện chuột phải
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int[] selectedRows = table.getSelectedRows();
                    boolean hasFolderSelected = false;

                    // Kiểm tra nếu có ít nhất một dòng là thư mục
                    for (int row : selectedRows) {
                        String type = (String) table.getValueAt(row, 2); // Giả định cột "Type" chứa kiểu dữ liệu
                        if ("Folder".equalsIgnoreCase(type)) {
                            hasFolderSelected = true;
                            break;
                        }
                    }

                    // Cập nhật trạng thái menu
                    downloadItem.setEnabled(!hasFolderSelected && selectedRows.length >= 1);
                    renameItem.setEnabled(selectedRows.length == 1);
                    deleteItem.setEnabled(selectedRows.length >= 1);

                    // Hiển thị menu chuột phải
                    popupMenu.show(table, e.getX(), e.getY());
                }
            }
        });

        return table;
    }

    private void showDownloadForm(JTable table) {
        JDialog downloadDialog = new JDialog((Frame) null, "Download", true);
        downloadDialog.setLayout(null);

        // Tạo các thành phần
        JLabel protocolLabel = new JLabel("Protocol:");
        JComboBox<String> protocolComboBox = new JComboBox<>(new String[]{"FTP", "TFTP"});
        JLabel typeLabel = new JLabel("Type:");
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Binary", "Ascii"});
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        // Đặt vị trí
        protocolLabel.setBounds(20, 20, 100, 25);
        protocolComboBox.setBounds(120, 20, 240, 25);
        typeLabel.setBounds(20, 60, 100, 25);
        typeComboBox.setBounds(120, 60, 240, 25);
        okButton.setBounds(80, 150, 80, 30);
        cancelButton.setBounds(200, 150, 100, 30);

        // Thêm vào dialog
        downloadDialog.add(protocolLabel);
        downloadDialog.add(protocolComboBox);
        downloadDialog.add(typeLabel);
        downloadDialog.add(typeComboBox);
        downloadDialog.add(okButton);
        downloadDialog.add(cancelButton);

        // Sự kiện OK
        okButton.addActionListener(e -> {
            String protocol = (String) protocolComboBox.getSelectedItem();
            String type = (String) typeComboBox.getSelectedItem();
            java.util.List<String> filesToDownload = getSelectedFilesFromLocalTable(table, true);

            // Lấy nút hiện tại từ remoteTree
            TreePath selectedPath = remoteTree.getSelectionPath();
            if (selectedPath == null) {
                JOptionPane.showMessageDialog(null, "Please select a folder in the tree!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (String file : filesToDownload) {
                // Tạo đường dẫn đầy đủ cho thư mục mới
                String fullPath = currentNodeInRemoteTree.getPath();
                if (!fullPath.isEmpty()){
                    fullPath += "/" + file;
                }
                else {
                    fullPath += file;
                }
                fileCommand.get(fullPath);
                System.out.println("dowloading Files: " +fullPath);
                System.out.println("Protocol: " + protocol);
                System.out.println("Type: " + type);
            }

            downloadDialog.dispose();
        });

        // Sự kiện Cancel
        cancelButton.addActionListener(e -> downloadDialog.dispose());

        downloadDialog.setSize(400, 250);
        downloadDialog.setLocationRelativeTo(null);
        downloadDialog.setResizable(false);
        downloadDialog.setVisible(true);
    }

    private void handleDeleteFiles(JTable table) {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(null, "No files selected to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Hiển thị xác nhận xóa
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to delete " + selectedRows.length + " file(s)?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            TreePath selectedPath = remoteTree.getSelectionPath();
            if (selectedPath == null) {
                JOptionPane.showMessageDialog(null, "Please select a folder in the tree!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (int row : selectedRows) {
                String fullPath = currentNodeInRemoteTree.getPath();
                if (!fullPath.isEmpty()){
                    fullPath += "/" + table.getValueAt(row, 0);
                }
                else {
                    fullPath += table.getValueAt(row, 0);
                }
                String type = (String) table.getValueAt(row, 2);
                if(type.equals("File")){
                    fileCommand.delete(fullPath);
                }
                else if(type.equals("Folder")){
                    directoryCommand.removeDirectory(fullPath);
                }
            }

            // Xóa file khỏi model (giả lập xóa file)
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                model.removeRow(selectedRows[i]);
            }


            JOptionPane.showMessageDialog(null, "Deleted " + selectedRows.length + " file(s).", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleRenameFile(JTable table, int rowIndex){
        String oldName = (String) table.getValueAt(rowIndex, 0);
        String newName = JOptionPane.showInputDialog(table, "new name:", oldName);
        String fullPathOld = currentNodeInRemoteTree.getPath();
        if (!fullPathOld.isEmpty()){
            fullPathOld += "/" + oldName;
        }
        else {
            fullPathOld += oldName;
        }

        String fullPathNew = currentNodeInRemoteTree.getPath();
        if (!fullPathNew.isEmpty()){
            fullPathNew += "/" + newName;
        }
        else {
            fullPathNew += newName;
        }
        if (newName != null && !newName.trim().isEmpty()) {
            table.setValueAt(newName, rowIndex, 0);
//            JOptionPane.showMessageDialog(null, "Renamed " + oldName + " to " + newName);
            commonCommand.rename(fullPathOld, fullPathNew);
        }
    }

    private void handleCreateFolder(JTable table) {
        // Hiển thị hộp thoại nhập tên thư mục
        String folderName = JOptionPane.showInputDialog(table, "Enter folder name:");

        // Kiểm tra nếu người dùng nhấn "Cancel"
        if (folderName == null) {
            System.out.println("Folder creation canceled.");
            return;
        }

        // Kiểm tra tên thư mục hợp lệ
        if (folderName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Folder name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Lấy nút hiện tại từ remoteTree
        TreePath selectedPath = remoteTree.getSelectionPath();
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(null, "Please select a folder in the tree!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fullPath = currentNodeInRemoteTree.getPath();
        if (!fullPath.isEmpty()){
            fullPath += "/" + folderName;
        }
        else {
            fullPath += folderName;
        }

        // Gọi lệnh tạo thư mục
        try {
            directoryCommand.makeDirectory(fullPath);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Failed to create folder: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cập nhật bảng hiển thị
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addRow(new Object[]{folderName, "-", "Folder", new java.util.Date()});

        // Thêm nút mới vào remoteTree
        DefaultTreeModel treeModel = (DefaultTreeModel) remoteTree.getModel();
        Node newNode = Node.builder()
                .name(folderName)
                .path(fullPath)
                .build();
        Node loading = Node.builder()
                .name("Loading...")
                .path("")
                .build();
        DefaultMutableTreeNode newFolderNode = new DefaultMutableTreeNode(newNode);
        newFolderNode.add(new DefaultMutableTreeNode(loading));
        currentDefaultMutableTreeNodeInRemoteTree.add(newFolderNode);
        treeModel.reload(currentDefaultMutableTreeNodeInRemoteTree); // Làm mới cây để hiển thị nút mới

        // In thông tin tạo thư mục ra console
        System.out.println("New folder created at: " + fullPath);

        // Hiển thị thông báo thành công
        JOptionPane.showMessageDialog(null, "Folder '" + fullPath + "' created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleUploadFile(JTable table) {
        // Create a JFileChooser instance
        JFileChooser fileChooser = new JFileChooser();

        // Allow multiple file selection
        fileChooser.setMultiSelectionEnabled(true);

        // Set filter to allow only file selection (not directories)
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(null);

        // Check if the user clicked "Open"
        if (result == JFileChooser.APPROVE_OPTION) {
            // Get the selected files
            File[] selectedFiles = fileChooser.getSelectedFiles();

            // Validate if any files were selected
            if (selectedFiles.length == 0) {
                JOptionPane.showMessageDialog(null, "No files selected!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] fileUpload = new String[selectedFiles.length];
            int i = 0;
            // Print the file paths to the console
            System.out.println("Selected files for upload:");
            for (File file : selectedFiles) {
                System.out.println(file.getAbsolutePath());
                fileUpload[i++] = file.getAbsolutePath();
            }

            for (String file : fileUpload) {
                String fullPathToServer = currentNodeInRemoteTree.getPath();
                String name;
                if (file.contains("\\")){
                    name = file.substring(file.lastIndexOf('\\') + 1);
                }
                else {
                    name = file;
                }
                if (!fullPathToServer.isEmpty()){
                    fullPathToServer += "/" + name;
                }
                else {
                    fullPathToServer += name;
                }
                fileCommand.send(file, fullPathToServer);
            }
//            fileCommand.multiPut(fileUpload);

            // Optionally, you can add these files to the table
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (File file : selectedFiles) {
                model.addRow(new Object[]{file.getName(), file.length() + " bytes", "File", new java.util.Date(file.lastModified())});
            }
        } else if (result == JFileChooser.CANCEL_OPTION) {
            // If the user canceled the operation
            System.out.println("File upload canceled.");
        }
    }

    private void updateRemoteTable(JTable table, Object[][] data) {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified"};
        // if ...
        // Cập nhật model bảng
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table.setModel(model);
    }

    private Object[][] processListFileAndFolder(java.util.List<String> ftpData) {
        Object[][] tableData = new Object[ftpData.size()][4];

        for (int i = 0; i < ftpData.size(); i++) {
            String line = ftpData.get(i);
            String[] parts = line.split("\\s+", 9); // Giới hạn split để giữ tên tệp đầy đủ từ phần tử thứ 8 trở đi

            // Phân tích từng trường
            String permissions = parts[0];
            String type = permissions.startsWith("d") ? "Folder" : "File";
            String size = type.equals("Folder") ? "-" : parts[4] + " bytes"; // Folder không hiển thị kích thước
            String date = parts[5] + " " + parts[6] + " " + parts[7];
            String name = parts.length > 8 ? parts[8] : ""; // Lấy phần tên (nếu có)

            // Gán dữ liệu vào mảng
            tableData[i][0] = name;     // Name
            tableData[i][1] = size;     // Size
            tableData[i][2] = type;     // Type
            tableData[i][3] = date;     // Last Modified
        }

        return tableData;
    }

    // LOG
    private void prependText(String message) {
        String currentText = logArea.getText(); // Lấy nội dung hiện tại
        logArea.setText(message + "\n" + currentText); // Ghi nội dung mới lên đầu
    }
}