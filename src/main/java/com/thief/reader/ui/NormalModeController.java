package com.thief.reader.ui;

import com.thief.reader.config.AppPreferences;
import com.thief.reader.config.ConfigManager;
import com.thief.reader.core.DocumentEngine;
import com.thief.reader.util.FileUtils;
import com.thief.reader.util.RecentFilesManager;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * 正常模式控制器
 */
public class NormalModeController {
    
    private final MainController mainController;
    private final DocumentEngine documentEngine;
    private final ConfigManager configManager;
    private final AppPreferences appPrefs;
    private final RecentFilesManager recentFilesManager;
    private BorderPane root;
    private final WebView webView;
    private final TreeView<String> sidebarTree;
    private final Label statusLabel;
    private File currentFile;
    
    public NormalModeController(MainController mainController, DocumentEngine documentEngine) {
        this.mainController = mainController;
        this.documentEngine = documentEngine;
        this.configManager = mainController.getConfigManager();
        this.appPrefs = AppPreferences.getInstance();
        this.recentFilesManager = new RecentFilesManager();
        this.webView = new WebView();
        this.sidebarTree = new TreeView<>();
        this.statusLabel = new Label("就绪");
        initUI();
        setupEventHandlers();
        updateRecentFilesList();
    }
    
    private void initUI() {
        // 初始化正常模式界面
        root = new BorderPane();
        
        // 创建菜单栏
        MenuBar menuBar = createMenuBar();
        
        // 创建工具栏
        HBox toolBar = createToolBar();
        
        // 创建主内容区域
        HBox mainContent = createMainContentArea();
        
        // 创建状态栏
        HBox statusBar = new HBox(statusLabel);
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 5;");
        
        // 组装界面
        VBox topContainer = new VBox(menuBar, toolBar);
        root.setTop(topContainer);
        root.setCenter(mainContent);
        root.setBottom(statusBar);
        // 正常模式下使用纯白色背景，不应用不透明度设置
        root.setStyle("-fx-background-color: #ffffff;");
        
        // 设置拖拽支持
        setupDragAndDrop();
        
        // 监听设置变化以实现实时更新
        setupSettingsListener();
    }
    
    private void setupSettingsListener() {
        // 定期检查Preferences中的缩放比例变化
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            private double lastScale = appPrefs.getFontSize(100.0);
            private String lastFontFamily = appPrefs.getFontFamily("Microsoft YaHei");  // 添加字体监听
            
            @Override
            public void handle(long now) {
                double currentScale = appPrefs.getFontSize(100.0);
                String currentFontFamily = appPrefs.getFontFamily("Microsoft YaHei");  // 获取当前字体设置
                
                // 检查缩放比例或字体是否有变化
                if (Math.abs(currentScale - lastScale) > 0.1 || !lastFontFamily.equals(currentFontFamily)) {
                    lastScale = currentScale;
                    lastFontFamily = currentFontFamily;  // 更新字体记录
                    // 当缩放比例或字体变化时，重新加载当前文件以更新显示
                    if (currentFile != null) {
                        loadFile(currentFile);
                    }
                }
            }
        };
        timer.start();
    }
    
    private void setupEventHandlers() {
        // 支持拖拽打开文件
        root.setOnDragOver(event -> {
            // 处理拖拽事件
        });
        
        root.setOnDragDropped(event -> {
            // 处理拖拽放置事件
        });
        
        // 支持快捷键
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.O) {
                openFile();
                event.consume();
            } else if (event.getCode() == KeyCode.F3) {  // 修改为F3键
                switchToMinimalMode();
                event.consume();
            }
        });
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // 文件菜单
        Menu fileMenu = new Menu("文件");
        MenuItem openItem = new MenuItem("打开文件");
        openItem.setOnAction(e -> openFile());
        fileMenu.getItems().add(openItem);
        
        // 视图菜单
        Menu viewMenu = new Menu("视图");
        MenuItem minimalModeItem = new MenuItem("切换极简模式 (F3)");  // 修改菜单项显示文本
        minimalModeItem.setOnAction(e -> switchToMinimalMode());
        viewMenu.getItems().add(minimalModeItem);
        
        // 设置菜单
        Menu settingsMenu = new Menu("设置");
        MenuItem settingsItem = new MenuItem("首选项");
        settingsItem.setOnAction(e -> showSettings());
        settingsMenu.getItems().add(settingsItem);
        
        // 帮助菜单
        Menu helpMenu = new Menu("帮助");
        MenuItem aboutItem = new MenuItem("关于");
        aboutItem.setOnAction(e -> showAbout());
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(fileMenu, viewMenu, settingsMenu, helpMenu);
        return menuBar;
    }
    
    private HBox createToolBar() {
        HBox toolBar = new HBox();
        toolBar.setSpacing(10);
        toolBar.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 5;");
        
        Button openButton = new Button("打开文件");
        openButton.setOnAction(e -> openFile());
        
        Button minimalModeButton = new Button("极简模式");
        minimalModeButton.setOnAction(e -> switchToMinimalMode());
        
        toolBar.getChildren().addAll(openButton, minimalModeButton);
        return toolBar;
    }
    
    private HBox createMainContentArea() {
        HBox mainContent = new HBox();
        mainContent.setSpacing(5);
        
        // 左侧边栏
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc;");
        
        Label sidebarTitle = new Label("最近文件");
        sidebarTitle.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
        
        sidebar.getChildren().addAll(sidebarTitle, sidebarTree);
        VBox.setVgrow(sidebarTree, Priority.ALWAYS);
        
        // 右侧内容区域
        VBox contentArea = new VBox(webView);
        VBox.setVgrow(webView, Priority.ALWAYS);
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(sidebar, contentArea);
        return mainContent;
    }
    
    private void setupDragAndDrop() {
        // 设置拖拽支持
        root.setOnDragOver(event -> {
            // 处理拖拽事件
        });
        
        root.setOnDragDropped(event -> {
            // 处理拖拽放置事件
        });
    }
    
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("所有支持的文件", "*.md", "*.txt", "*.pdf"),
            new FileChooser.ExtensionFilter("Markdown文件", "*.md"),
            new FileChooser.ExtensionFilter("文本文件", "*.txt"),
            new FileChooser.ExtensionFilter("PDF文件", "*.pdf")
        );
        
        File selectedFile = fileChooser.showOpenDialog(mainController.getPrimaryStage());
        if (selectedFile != null) {
            loadFile(selectedFile);
            recentFilesManager.addFile(selectedFile);
            updateRecentFilesList(); // 更新最近打开文件列表显示
        }
    }
    
    public void loadFile(File file) {
        try {
            currentFile = file;
            String content = documentEngine.parseDocument(file);
            
            if (file.getName().toLowerCase().endsWith(".md")) {
                // Markdown 文件使用 WebView 渲染（已包含样式）
                webView.getEngine().loadContent(content, "text/html");
            } else {
                // 其他文件直接显示文本（添加基本样式）
                // 从设置中获取缩放比例，默认为100%
                double scale = appPrefs.getFontSize(100.0);
                
                // 从设置中获取字体，默认为系统默认字体
                String fontFamily = appPrefs.getFontFamily("sans-serif");
                
                // 计算实际字体大小（基于默认16px）
                double baseFontSize = 16.0;
                double actualFontSize = baseFontSize * (scale / 100.0);
                
                // 四舍五入到整数
                int roundedFontSize = (int) Math.round(actualFontSize);
                String formattedFontSize = String.valueOf(roundedFontSize);
                
                // 计算其他基于缩放比例的尺寸
                double scaledPadding = 20.0 * (scale / 100.0);
                // 四舍五入padding到整数
                int roundedPadding = (int) Math.round(scaledPadding);
                
                // 正常模式下使用纯白色背景，极简模式下使用透明背景
                String backgroundColor = "transparent";  // 使用透明背景，让极简模式的背景不透明度设置生效
                
                String styledContent = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset='UTF-8'>\n" +
                        "    <style>\n" +
                        "        body {\n" +
                        "            font-family: " + fontFamily + ", -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji';\n" +
                        "            font-size: " + formattedFontSize + "px;\n" +
                        "            line-height: 1.6;\n" +
                        "            color: #333;\n" +
                        "            background-color: transparent;\n" +  // 使用透明背景，让极简模式的背景不透明度设置生效
                        "            padding: " + roundedPadding + "px;\n" +
                        "            margin: 0 auto;\n" +
                        "            white-space: pre-wrap;\n" +
                        "            width: 100%;\n" +
                        "            max-width: 100%;\n" +
                        "            box-sizing: border-box;\n" +
                        "            word-wrap: break-word;\n" +
                        "            -webkit-font-smoothing: antialiased;\n" +
                        "            -moz-osx-font-smoothing: grayscale;\n" +
                        "            text-rendering: optimizeLegibility;\n" +
                        "            -webkit-text-size-adjust: 100%;\n" +  // 防止iOS Safari自动调整字体大小
                        "            -ms-text-size-adjust: 100%;\n" +      // 防止IE自动调整字体大小
                        "            text-shadow: 1px 1px 1px rgba(0,0,0,0.004);\n" +  // 轻微阴影提升清晰度
                        "        }\n" +
                        "        \n" +
                        "        /* 美化滚动条样式 */\n" +
                        "        ::-webkit-scrollbar {\n" +
                        "            width: 8px;\n" +
                        "            height: 8px;\n" +
                        "        }\n" +
                        "        \n" +
                        "        ::-webkit-scrollbar-track {\n" +
                        "            background: rgba(0, 0, 0, 0.05);\n" +
                        "            border-radius: 4px;\n" +
                        "        }\n" +
                        "        \n" +
                        "        ::-webkit-scrollbar-thumb {\n" +
                        "            background: rgba(0, 0, 0, 0.2);\n" +
                        "            border-radius: 4px;\n" +
                        "        }\n" +
                        "        \n" +
                        "        ::-webkit-scrollbar-thumb:hover {\n" +
                        "            background: rgba(0, 0, 0, 0.3);\n" +
                        "        }\n" +
                        "        \n" +
                        "        ::-webkit-scrollbar-corner {\n" +
                        "            background: rgba(0, 0, 0, 0.05);\n" +
                        "        }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        content + "\n" +
                        "</body>\n" +
                        "</html>";
                webView.getEngine().loadContent(styledContent, "text/html");
            }
            
            statusLabel.setText("已加载: " + file.getName() + " | 类型: " + FileUtils.getFileExtension(file));
        } catch (IOException e) {
            showError("文件读取错误", "无法读取文件: " + e.getMessage());
        } catch (Exception e) {
            showError("文件解析错误", "无法解析文件: " + e.getMessage());
        }
    }
    
    /**
     * 更新最近打开文件列表显示
     */
    private void updateRecentFilesList() {
        TreeItem<String> rootItem = new TreeItem<>("最近文件");
        rootItem.setExpanded(true);
        
        List<String> recentFiles = recentFilesManager.getRecentFiles();
        for (String filePath : recentFiles) {
            File file = new File(filePath);
            TreeItem<String> fileItem = new TreeItem<>(file.getName());
            rootItem.getChildren().add(fileItem);
        }
        
        sidebarTree.setRoot(rootItem);
        sidebarTree.setShowRoot(false); // 不显示根节点
        
        // 为 TreeView 添加鼠标点击监听器
        sidebarTree.setOnMouseClicked((MouseEvent event) -> {
            TreeItem<String> selectedItem = sidebarTree.getSelectionModel().getSelectedItem();
            if (selectedItem != null && event.getClickCount() == 2) {
                // 双击文件项时加载文件
                String selectedFileName = selectedItem.getValue();
                for (String filePath : recentFiles) {
                    File file = new File(filePath);
                    if (file.getName().equals(selectedFileName)) {
                        loadFile(file);
                        break;
                    }
                }
            }
        });
    }
    
    private void switchToMinimalMode() {
        if (currentFile != null) {
            mainController.switchToMinimalMode(currentFile);
        } else {
            showError("未选择文件", "请先打开一个文件");
        }
    }
    
    private void showSettings() {
        // 显示设置对话框
        SettingsDialog settingsDialog = new SettingsDialog(mainController.getPrimaryStage());
        settingsDialog.showAndWait();
    }
    
    private void showAbout() {
        // 显示关于对话框
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText("Thief Java Reader v1.0");
        alert.setContentText("一款轻量级文档阅读器\n支持 Markdown、TXT、PDF 格式");
        alert.showAndWait();
    }
    
    private void applyConfig() {
        // 应用配置设置
        // 例如字体大小、主题等
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Parent getRoot() {
        return root;
    }
}