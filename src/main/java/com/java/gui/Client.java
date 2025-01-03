package com.java.gui;

import com.java.client.ftp.enums.Permission;
import com.java.client.ftp.enums.TransferMode;
import com.java.client.ftp.handle.*;
import com.java.client.ftp.system.ClientConfig;
import com.java.client.tftp.TFTPHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Array;

@Component
public class Client extends JFrame {

    @Autowired
    private ClientConfig clientConfig;

    @Autowired
    private CommonCommand commonCommand;
    @Autowired
    private ConnectionCommand connectionCommand;
    @Autowired
    private DataTransferCommand transferCommand;
    @Autowired
    private DirectoryCommand directoryCommand;
    @Autowired
    private PermissionCommand permissionCommand;
    @Lazy
    @Autowired
    private FileCommand fileCommand;

    private TFTPHandle tftpHandle = new TFTPHandle();

    private JTree localTree, remoteTree;
    private JTable localTable, remoteTable;
    private JTextArea logArea;
    private DefaultMutableTreeNode currentDefaultMutableTreeNodeInRemoteTree;
    private DefaultMutableTreeNode currentDefaultMutableTreeNodeInLocalTree;
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
        JButton btnSetting = new JButton("Setting");
        connectionPanel.add(btnSetting);

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
        btnSetting.addActionListener(e -> handleSettingsDialog());
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
//                logArea.append("Connecting to " + host + ":" + port + " with username " + username + "\n");
                connectionCommand.openConnection(host, port, username, password);
//                logArea.append("Connected successfully!\n");
            } else {
                logArea.append("Please fill in all the fields.\n");
            }
        } catch (NumberFormatException ex) {
            logArea.append("Invalid port number.\n");
        } catch (Exception ex) {
            logArea.append("Connection failed: " + ex.getMessage() + "\n");
        }
    }

    private void handleSettingsDialog() {
        JDialog settingsDialog = new JDialog((Frame) null, "Settings", true);
        settingsDialog.setLayout(null);

        // Tạo các thành phần
        JLabel transferModeLabel = new JLabel("Transfer Mode:");
        JComboBox<String> transferModeComboBox = new JComboBox<>(new String[]{"Active", "Passive"});

        JButton confirmButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        // Đặt vị trí
        transferModeLabel.setBounds(20, 20, 100, 25);
        transferModeComboBox.setBounds(120, 20, 240, 25);
        confirmButton.setBounds(80, 60, 80, 30);
        cancelButton.setBounds(200, 60, 100, 30);

        // Thiết lập giá trị mặc định cho combobox
        TransferMode currentTransferMode = clientConfig.getTransferModeDefault();
        if (currentTransferMode == TransferMode.ACTIVE) {
            transferModeComboBox.setSelectedItem("Active");
        } else if (currentTransferMode == TransferMode.PASSIVE) {
            transferModeComboBox.setSelectedItem("Passive");
        }

        // Thêm các thành phần vào dialog
        settingsDialog.add(transferModeLabel);
        settingsDialog.add(transferModeComboBox);
        settingsDialog.add(confirmButton);
        settingsDialog.add(cancelButton);

        // Xử lý sự kiện nút OK
        confirmButton.addActionListener(e -> {
            String selectedTransferMode = (String) transferModeComboBox.getSelectedItem();
            if ("Active".equals(selectedTransferMode)) {
                clientConfig.setTransferModeDefault(TransferMode.ACTIVE);
            } else if ("Passive".equals(selectedTransferMode)) {
                clientConfig.setTransferModeDefault(TransferMode.PASSIVE);
            }
            settingsDialog.dispose();
        });

        // Xử lý sự kiện nút Cancel
        cancelButton.addActionListener(e -> settingsDialog.dispose());

        // Cài đặt cho dialog
        settingsDialog.setSize(400, 150);
        settingsDialog.setLocationRelativeTo(null);
        settingsDialog.setResizable(false);
        settingsDialog.setVisible(true);
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
                Node loadingNode = Node.builder()
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
                currentDefaultMutableTreeNodeInLocalTree = node;
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
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                currentDefaultMutableTreeNodeInLocalTree = node;
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
//                            logArea.append("Local folder selected: " + selectedFile.getAbsolutePath() + "\n");
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
                        .path(((Node) parentNode.getUserObject()).getPath() + f.getName() + "\\")
                        .build();
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
                        node
                );
                Node loadingNode = Node.builder()
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

    private void showUploadForm(JTable table) {
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
            int[] filesToUpload = getSelectedFilesFromLocalTable(table);

            assert protocol != null;
            assert type != null;
            if (!clientConfig.getTransferType().name().equals(type.toUpperCase())) {
                if (type.equals("Binary")) {
                    transferCommand.setBinaryMode();
                } else if (type.equals("Ascii")) {
                    transferCommand.setAsciiMode();
                }
            }
//                fileCommand.multiPut(filesToUpload.toArray(new String[0]));
            for (int row : filesToUpload) {
                String file = (String) table.getValueAt(row, 4);
                String fullPathToServer = currentNodeInRemoteTree.getPath();
                String name;
                if (file.contains("\\")) {
                    name = file.substring(file.lastIndexOf('\\') + 1);
                } else {
                    name = file;
                }
                if (!fullPathToServer.isEmpty()) {
                    fullPathToServer += "/" + name;
                } else {
                    fullPathToServer += name;
                }
                if (protocol.equals("FTP")) {
                    fileCommand.send(file, fullPathToServer);
                    // if success : ghi log
                } else if (protocol.equals("TFTP")) {
                    tftpHandle.handleRequest(TFTPHandle.OP_WRQ, file, type, logArea);
                }
            }
            java.util.List<String> response = commonCommand.listDetail(currentNodeInRemoteTree.getPath());
            updateRemoteTable(remoteTable, processListFileAndFolder(response));
            uploadDialog.dispose();
        });

        // Sự kiện Cancel
        cancelButton.addActionListener(e -> uploadDialog.dispose());

        uploadDialog.setSize(400, 250);
        uploadDialog.setLocationRelativeTo(null);
        uploadDialog.setResizable(false);
        uploadDialog.setVisible(true);
    }

    private void showAppendForm(JTable table) {
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
            int[] selectedFiles = getSelectedFilesFromLocalTable(table);

            if (remoteFile.isEmpty()) {
                JOptionPane.showMessageDialog(appendDialog, "Remote file path cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedFiles.length == 0) {
                JOptionPane.showMessageDialog(appendDialog, "No file selected from the table.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!clientConfig.getTransferType().name().equals(type.toUpperCase())) {
                if (type.equals("Binary")) {
                    transferCommand.setBinaryMode();
                } else if (type.equals("Ascii")) {
                    transferCommand.setAsciiMode();
                }
            }

            String fullPathToServer = currentNodeInRemoteTree.getPath();

            if (!fullPathToServer.isEmpty()) {
                fullPathToServer += "/" + remoteFile;
            } else {
                fullPathToServer += remoteFile;
            }
            fileCommand.append((String) table.getValueAt(selectedFiles[0], 4), fullPathToServer);
            // if success : log
            java.util.List<String> response = commonCommand.listDetail(currentNodeInRemoteTree.getPath());
            updateRemoteTable(remoteTable, processListFileAndFolder(response));
            appendDialog.dispose();
        });

        // Sự kiện Cancel
        cancelButton.addActionListener(e -> appendDialog.dispose());

        appendDialog.setSize(400, 250);
        appendDialog.setLocationRelativeTo(null);
        appendDialog.setResizable(false);
        appendDialog.setVisible(true);
    }

    private int[] getSelectedFilesFromLocalTable(JTable table) {
        return table.getSelectedRows();
    }

    private File getFileFromNode(DefaultMutableTreeNode node) {
        assert node != null;
        Node nodeCrr = (Node) node.getUserObject();
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
        currentNodeInRemoteTree = (Node) root.getUserObject();
        currentDefaultMutableTreeNodeInRemoteTree = root;
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
                currentNodeInRemoteTree = (Node) node.getUserObject();
                currentDefaultMutableTreeNodeInRemoteTree = node;
                // Kiểm tra nếu node có 1 con (và là node root có "Loading...") để bắt đầu load dữ liệu
                if (node.getChildCount() == 1 && node.getFirstChild().toString().equals("Loading...")) {
                    // Lấy đường dẫn của node (ví dụ lấy thư mục gốc của remote files)
                    // Xóa node "Loading..." và bắt đầu load thư mục
                    node.removeAllChildren();

                    java.util.List<String> response = commonCommand.listDetail(((Node) node.getUserObject()).getPath());
                    java.util.List<String> subFolderName = getFolderNames(response);
                    LazyLoadRemote(node, subFolderName);

                    // Cập nhật lại mô hình cây để hiển thị thư mục con
                    ((DefaultTreeModel) tree.getModel()).reload(node);
                }
            }

            @Override
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent event) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                currentNodeInRemoteTree = (Node) node.getUserObject();
                currentDefaultMutableTreeNodeInRemoteTree = node;
            }
        });

        return tree;
    }

    private void addRemoteTreeListeners() {
        remoteTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = remoteTree.getPathForLocation(e.getX(), e.getY()); // Lấy node tại vị trí click
                    if (path != null) {
                        remoteTree.setSelectionPath(path); // Chọn node
                        DefaultMutableTreeNode selectedNode =
                                (DefaultMutableTreeNode) remoteTree.getLastSelectedPathComponent();
                        if (selectedNode != null) {
//                            String selectedFolder = selectedNode.toString();
//                            logArea.append("Remote folder selected: " + selectedFolder + "\n");
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
                pathCurr += "/" + folderName;
            } else {
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
            String[] parts = line.split("\\s+", 7); // Tách dòng thành các trường

            if (parts.length < 6) {
                continue; // Bỏ qua dòng không hợp lệ
            }

            String permissions = parts[0]; // Quyền (vd: drwxr-xr-x)
            String name = parts[6];        // Tên file/folder

            if (permissions.startsWith("d")) { // Nếu quyền bắt đầu bằng 'd', là thư mục
                folderNames.add(name);
            }
        }

        return folderNames; // Trả về danh sách thư mục
    }


    // remote table
    private JTable createRemoteTable() {
        // Thêm cột itemId, owner, và isPermission
        String[] columnNames = {"Name", "Size", "Type", "Last Modified", "itemId", "isPublic", "isOwner"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        // Ẩn các cột itemId, owner, và isPermission
        table.getColumnModel().getColumn(4).setMinWidth(0);
        table.getColumnModel().getColumn(4).setMaxWidth(0);
        table.getColumnModel().getColumn(4).setPreferredWidth(0);

        table.getColumnModel().getColumn(5).setMinWidth(0);
        table.getColumnModel().getColumn(5).setMaxWidth(0);
        table.getColumnModel().getColumn(5).setPreferredWidth(0);

        table.getColumnModel().getColumn(6).setMinWidth(0);
        table.getColumnModel().getColumn(6).setMaxWidth(0);
        table.getColumnModel().getColumn(6).setPreferredWidth(0);

        // Cho phép chọn nhiều dòng
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Tạo menu chuột phải
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem downloadItem = new JMenuItem("Download");
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem uploadItem = new JMenuItem("Upload");
        JMenuItem createItem = new JMenuItem("Create folder");
        JMenuItem renameItem = new JMenuItem("Rename");
        JMenuItem permissionItem = new JMenuItem("Permissions");

        // Thêm các action listener cho các menu item
        downloadItem.addActionListener(ev -> showDownloadForm(table));
        deleteItem.addActionListener(ev -> handleDeleteFiles(table));
        uploadItem.addActionListener(ev -> handleUploadFile(table));
        createItem.addActionListener(ev -> handleCreateFolder(table));
        renameItem.addActionListener(ev -> handleRenameFile(table, table.getSelectedRow()));
        permissionItem.addActionListener(ev -> handlePermission());

        popupMenu.add(renameItem);
        popupMenu.add(downloadItem);
        popupMenu.add(deleteItem);
        popupMenu.add(uploadItem);
        popupMenu.add(createItem);
        popupMenu.add(permissionItem);

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
                    permissionItem.setEnabled(PermissionUtil.checkEnableMenuPermission(null) && selectedRows.length == 1);

                    // Hiển thị menu chuột phải
                    popupMenu.show(table, e.getX(), e.getY());
                }
            }
        });

        return table;
    }

    private void handlePermission() {
        DefaultMutableTreeNode selectedNode = currentDefaultMutableTreeNodeInRemoteTree;
        if (selectedNode != null) {
            JDialog permissionDialog = new JDialog((Frame) null, "Access Permissions", true);
            permissionDialog.setLayout(new BorderLayout());
            permissionDialog.setSize(500, 500);

            // Search Panel
            JPanel searchPanel = new JPanel(new BorderLayout());
            JTextField searchField = new JTextField();
            JButton searchButton = new JButton("Search");
            searchPanel.add(new JLabel("Search user:"), BorderLayout.WEST);
            searchPanel.add(searchField, BorderLayout.CENTER);
            searchPanel.add(searchButton, BorderLayout.EAST);


            //id item
            int selectedRow = remoteTable.getSelectedRow();
            String itemId = (String) remoteTable.getValueAt(selectedRow, 4);

//            long itemId = 0;
            java.util.List<String> permissions = permissionCommand.getPermission(itemId);

            // Search Result List
            DefaultListModel<String> searchListModel = new DefaultListModel<>();
            JList<String> searchList = new JList<>(searchListModel);
//            java.util.List<String> usernames = java.util.Arrays.asList(
//                    "tung",
//                    "van",
//                    "khanh",
//                    "dev"
//            );

            java.util.List<String> usernames = PermissionUtil.getUser(permissions);
            for (String username : usernames) {
                searchListModel.addElement(username);
            }
            JScrollPane searchScrollPane = new JScrollPane(searchList);
            searchScrollPane.setBorder(BorderFactory.createTitledBorder("Search Results"));

            // User Permissions Table
//            java.util.List<UserPermission> userPermissions = java.util.Arrays.asList(
//              UserPermission.builder()
//                      .username("us1")
//                      .permission(Permission.ALL)
//                      .build(),
//                    UserPermission.builder()
//                            .username("us3")
//                            .permission(Permission.WRITE)
//                            .build(),
//                    UserPermission.builder()
//                            .username("us4")
//                            .permission(Permission.WRITE)
//                            .build(),
//                    UserPermission.builder()
//                            .username("us2")
//                            .permission(Permission.READ)
//                            .build()
//            );
            java.util.List<UserPermission> userPermissions = PermissionUtil.getUserPermission(permissions);
            DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Username", "Permission", "Delete"}, 0);
            JTable userTable = new JTable(tableModel);

            for (UserPermission userPermission : userPermissions) {
                tableModel.addRow(new Object[]{userPermission.getUsername(), userPermission.getPermission(), false});
            }
            // Creating a checkbox column (last column in the table)
            TableColumn deleteColumn = userTable.getColumnModel().getColumn(2);
            deleteColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));
            deleteColumn.setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JCheckBox checkBox = new JCheckBox();
                    checkBox.setSelected((Boolean) value);
                    return checkBox;
                }
            });

            // Dropdown chọn quyền
            JComboBox<String> permissionComboBox = new JComboBox<>(new String[]{"READ", "WRITE", "ALL"});
            userTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(permissionComboBox));

            JScrollPane tableScrollPane = new JScrollPane(userTable);

            // Add User Button
            JButton addUserButton = new JButton("Add User");
            addUserButton.addActionListener(event -> {
                String selectedUser = searchList.getSelectedValue();
                if (selectedUser != null) {
                    boolean alreadyAdded = false;
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        if (tableModel.getValueAt(i, 0).equals(selectedUser)) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded) {
                        tableModel.addRow(new Object[]{selectedUser, "READ", false});
                    } else {
                        JOptionPane.showMessageDialog(permissionDialog, "User already exists in the list.");
                    }
                } else {
                    JOptionPane.showMessageDialog(permissionDialog, "Please select a user from the search results.");
                }
            });

            // Search Button Action
            searchButton.addActionListener(event -> {
                String searchQuery = searchField.getText().trim();
                searchListModel.clear();
                if (!searchQuery.isEmpty()) {
                    java.util.List<String> usernameSearchList = PermissionUtil.searchByUsername(searchQuery, usernames);
                    for (String username : usernameSearchList) {
                        searchListModel.addElement(username);
                    }
                    if (searchListModel.isEmpty()) {
                        JOptionPane.showMessageDialog(permissionDialog, "No users found.");
                    }
                } else {
                    for (String username : usernames) {
                        searchListModel.addElement(username);
                    }
                }
            });

            // Action Panel
            JPanel actionPanel = new JPanel(new FlowLayout());
            JButton applyButton = new JButton("Apply");
            JButton deleteButton = new JButton("Delete");
            JButton cancelButton = new JButton("Cancel");

            applyButton.addActionListener(event -> {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String username = (String) tableModel.getValueAt(i, 0);
                    String permission = (String) tableModel.getValueAt(i, 1);
                    permissionCommand.createPermission(itemId + "/" + username + "/" + permission);
                    //
                }
                JOptionPane.showMessageDialog(permissionDialog, "Permissions applied successfully.");
                permissionDialog.dispose();
            });

            cancelButton.addActionListener(event -> permissionDialog.dispose());

            deleteButton.addActionListener(event -> {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Boolean isSelected = (Boolean) tableModel.getValueAt(i, 2);
                    if (isSelected != null && isSelected) {
                        String username = (String) tableModel.getValueAt(i, 0);
//                        System.out.println(username);
                        permissionCommand.deletePermission(itemId + "/" + username);
                        permissionDialog.dispose();
                    }
                }
            });

            actionPanel.add(applyButton);
            actionPanel.add(cancelButton);
            actionPanel.add(deleteButton);

            // Combine Panels
            // Panel chứa danh sách tìm kiếm và nút thêm
            JPanel searchResultPanel = new JPanel(new BorderLayout());
            searchResultPanel.add(searchScrollPane, BorderLayout.CENTER);
            searchResultPanel.add(addUserButton, BorderLayout.SOUTH);

            // Chia layout chính
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchResultPanel, tableScrollPane);
            splitPane.setResizeWeight(0.5);

            // Thêm các thành phần vào dialog
            permissionDialog.add(searchPanel, BorderLayout.NORTH);
            permissionDialog.add(splitPane, BorderLayout.CENTER);
            permissionDialog.add(actionPanel, BorderLayout.SOUTH);

            permissionDialog.setLocationRelativeTo(remoteTree);
            permissionDialog.setVisible(true);
        }

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
            int[] filesToDownload = getSelectedFilesFromLocalTable(table);

            // Lấy nút hiện tại từ remoteTree
            TreePath selectedPath = remoteTree.getSelectionPath();
            if (selectedPath == null) {
                JOptionPane.showMessageDialog(null, "Please select a folder in the tree!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!clientConfig.getTransferType().name().equals(type.toUpperCase())) {
                if (type.equals("Binary")) {
                    transferCommand.setBinaryMode();
                } else if (type.equals("Ascii")) {
                    transferCommand.setAsciiMode();
                }
            }
            for (int row : filesToDownload) {
                String file = (String) table.getValueAt(row, 0);
                // Tạo đường dẫn đầy đủ cho thư mục mới
                String fullPath = currentNodeInRemoteTree.getPath();
                if (!fullPath.isEmpty()) {
                    fullPath += "/" + file;
                } else {
                    fullPath += file;
                }
                if (protocol.equals("FTP")) {
                    fileCommand.get(fullPath);
                    // if success : ghi log
                } else if (protocol.equals("TFTP")) {
                    tftpHandle.handleRequest(TFTPHandle.OP_RRQ, file, type, logArea);
                }
            }
            updateLocalTable(localTable, getFileFromNode(currentDefaultMutableTreeNodeInLocalTree));
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

            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (int row : selectedRows) {
                String fullPath = currentNodeInRemoteTree.getPath();
                if (!fullPath.isEmpty()) {
                    fullPath += "/" + table.getValueAt(row, 0);
                } else {
                    fullPath += table.getValueAt(row, 0);
                }
                String type = (String) table.getValueAt(row, 2);
                if (type.equals("File")) {
                    // check success =>
                    if (fileCommand.delete(fullPath)) model.removeRow(row);
                } else if (type.equals("Folder")) {
                    // check success =>
                    if (directoryCommand.removeDirectory(fullPath)) {
                        // table
                        // Xóa hàng khỏi bảng
                        if (row >= 0 && row < model.getRowCount()) {
                            model.removeRow(row); // Xóa hàng
                        }

                        //tree
                        // Xóa node khỏi cây
                        DefaultTreeModel treeModel = (DefaultTreeModel) remoteTree.getModel();
                        DefaultMutableTreeNode selectedNode = currentDefaultMutableTreeNodeInRemoteTree;

                        if (selectedNode != null && row >= 0 && row < selectedNode.getChildCount()) {
                            // Lấy và xóa node con tại vị trí `row`
                            DefaultMutableTreeNode nodeToRemove = (DefaultMutableTreeNode) selectedNode.getChildAt(row);
                            selectedNode.remove(row);

                            // Thông báo cho TreeModel rằng một node đã bị xóa
                            treeModel.nodesWereRemoved(selectedNode, new int[]{row}, new Object[]{nodeToRemove});
                        }
                    }
                }
            }
//            java.util.List<String> response = commonCommand.listDetail(currentNodeInRemoteTree.getPath());
//            updateRemoteTable(remoteTable, processListFileAndFolder(response));
        }
    }

    private void handleRenameFile(JTable table, int rowIndex) {
        String oldName = (String) table.getValueAt(rowIndex, 0);
        String newName = JOptionPane.showInputDialog(table, "new name:", oldName);
        if (newName == null || newName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(table, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kiểm tra tên trùng lặp
        if (isNameDuplicate(table, newName.trim(), rowIndex)) {
            JOptionPane.showMessageDialog(table, "A file with the same name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fullPathOld = currentNodeInRemoteTree.getPath();
        if (!fullPathOld.isEmpty()) {
            fullPathOld += "/" + oldName;
        } else {
            fullPathOld += oldName;
        }

        String fullPathNew = currentNodeInRemoteTree.getPath();
        if (!fullPathNew.isEmpty()) {
            fullPathNew += "/" + newName;
        } else {
            fullPathNew += newName;
        }
        if (newName != null && !newName.trim().isEmpty()) {
            // if success =>
            if (commonCommand.rename(fullPathOld, fullPathNew)) {
                //render table
                table.setValueAt(newName, rowIndex, 0);
                //render tree

                // Cập nhật tên node trong cây
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)currentDefaultMutableTreeNodeInRemoteTree.getChildAt(rowIndex);

                if (selectedNode != null) {
                    // Thay đổi tên node trong cây
                    Node node = (Node) selectedNode.getUserObject();
                    node.setName(newName);

                    // Cập nhật mô hình cây
                    DefaultTreeModel treeModel = (DefaultTreeModel) remoteTree.getModel();
                    treeModel.nodeChanged(selectedNode); // Thông báo cây đã thay đổi
                }
            }
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
        if (!fullPath.isEmpty()) {
            fullPath += "/" + folderName;
        } else {
            fullPath += folderName;
        }
        // if success
        if (directoryCommand.makeDirectory(fullPath)) {
            DefaultTreeModel treeModel = (DefaultTreeModel) remoteTree.getModel();
            DefaultMutableTreeNode selectedNode = currentDefaultMutableTreeNodeInRemoteTree; // Node đang được chọn

            // Tạo node mới
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

            // Thêm node mới vào node đang được chọn
            selectedNode.add(newFolderNode);

            // Thông báo cho TreeModel rằng node đã thay đổi
            treeModel.nodesWereInserted(selectedNode, new int[]{selectedNode.getChildCount() - 1});

        }
        ;

        // render lại
        java.util.List<String> response = commonCommand.listDetail(currentNodeInRemoteTree.getPath());
        updateRemoteTable(remoteTable, processListFileAndFolder(response));

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
                if (file.contains("\\")) {
                    name = file.substring(file.lastIndexOf('\\') + 1);
                } else {
                    name = file;
                }
                if (!fullPathToServer.isEmpty()) {
                    fullPathToServer += "/" + name;
                } else {
                    fullPathToServer += name;
                }
                fileCommand.send(file, fullPathToServer);
            }
//            fileCommand.multiPut(fileUpload);

            java.util.List<String> response = commonCommand.listDetail(currentNodeInRemoteTree.getPath());
            updateRemoteTable(remoteTable, processListFileAndFolder(response));

        } else if (result == JFileChooser.CANCEL_OPTION) {
            // If the user canceled the operation
            System.out.println("File upload canceled.");
        }
    }

    private void updateRemoteTable(JTable table, Object[][] data) {
        String[] columnNames = {"Name", "Size", "Type", "Last Modified", "itemId", "isPublic", "isOwner"};
        // if ...
        // Cập nhật model bảng
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table.setModel(model);
    }

    private boolean isNameDuplicate(JTable table, String name, int rowIndex) {
        int rowCount = table.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            if (i != rowIndex) { // Bỏ qua dòng hiện tại
                String existingName = (String) table.getValueAt(i, 0);
                if (name.equals(existingName)) {
                    return true; // Tên trùng
                }
            }
        }
        return false; // Không trùng
    }

    private Object[][] processListFileAndFolder(java.util.List<String> ftpData) {
        Object[][] tableData = new Object[ftpData.size()][7];

        for (int i = 0; i < ftpData.size(); i++) {
            String line = ftpData.get(i);
            String[] parts = line.split("\\s+", 9); // Giới hạn split để giữ tên tệp đầy đủ từ phần tử thứ 8 trở đi

            // Phân tích từng trường
            String permissions = parts[0];
            String type = permissions.startsWith("d") ? "Folder" : "File";
            String size = type.equals("Folder") ? "-" : parts[1] + " bytes"; // Folder không hiển thị kích thước
            String date = parts[2];
            String itemId = parts[3];
            String isPublic = parts[4];
            String isOwner = parts[5];
            String name = parts.length > 5 ? parts[6] : ""; // Lấy phần tên (nếu có)

            // Gán dữ liệu vào mảng
            tableData[i][0] = name;     // Name
            tableData[i][1] = size;     // Size
            tableData[i][2] = type;     // Type
            tableData[i][3] = date;     // Last Modified
            tableData[i][4] = itemId;
            tableData[i][5] = isPublic;
            tableData[i][6] = isOwner;
        }

        return tableData;
    }

    // LOG
    public void prependText(String message) {
        String currentText = logArea.getText(); // Lấy nội dung hiện tại
        logArea.setText(message + "\n" + currentText); // Ghi nội dung mới lên đầu
    }
}