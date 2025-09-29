package com.thief.reader.ui;

import com.thief.reader.config.AppPreferences;
import com.thief.reader.core.DocumentEngine;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * 极简模式控制器
 */
public class MinimalModeController {
    
    private final MainController mainController;
    private final DocumentEngine documentEngine;
    private final AppPreferences appPrefs;
    private final Stage stage;
    private final StackPane root;
    private final WebView webView;
    private File currentFile;
    
    // 用于窗口拖动的变量
    private double xOffset = 0;
    private double yOffset = 0;
    
    // 用于窗口调整大小的变量
    private boolean resizing = false;
    private double resizeStartX = 0;
    private double resizeStartY = 0;
    private double resizeStartWidth = 0;
    private double resizeStartHeight = 0;
    private String resizeDirection = "";
    
    // 监听设置变化的监听器
    private ChangeListener<Number> scaleChangeListener;
    
    public MinimalModeController(MainController mainController, DocumentEngine documentEngine) {
        this.mainController = mainController;
        this.documentEngine = documentEngine;
        this.appPrefs = AppPreferences.getInstance();
        this.stage = new Stage();
        this.root = new StackPane();
        this.webView = new WebView();

        
        initUI();
        setupEventHandlers();
        setupScaleListener();
    }
    
    private void initUI() {
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("极简阅读模式");
        
        // 设置 WebView 背景透明
        webView.setStyle("-fx-background-color: transparent;");
        // 确保 WebView 的页面背景也是透明的
        webView.getEngine().setUserStyleSheetLocation("data:text/css,body,html { background-color: transparent !important; }");
        // 设置 WebView 的背景为透明
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                // 注入 JavaScript 代码来确保背景透明
                webView.getEngine().executeScript(
                    "document.body.style.backgroundColor = 'transparent';" +
                    "document.documentElement.style.backgroundColor = 'transparent';" +
                    "document.body.style.background = 'transparent';" +
                    "document.documentElement.style.background = 'transparent';"
                );
            }
        });
        
        // 创建一个带有圆角和半透明边框的容器
        StackPane container = new StackPane();
        // 初始化时使用设置的背景不透明度
        double initialOpacity = appPrefs.getBackgroundOpacity(0.9);
        container.setStyle(
            "-fx-background-color: rgba(255, 255, 255, " + initialOpacity + ");" +  // 使用设置的背景不透明度
            "-fx-background-radius: 10;" +  // 圆角边框
            "-fx-border-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0, 0, 0);"  // 阴影效果
        );
        
        // 使用HBox包装WebView以添加内边距
        HBox webViewContainer = new HBox();
        webViewContainer.setPadding(new Insets(10));
        webViewContainer.getChildren().add(webView);
        container.getChildren().add(webViewContainer);
        
        // 将容器添加到根节点
        root.getChildren().add(container);
        
        // 创建场景并设置透明背景
        Scene scene = new Scene(root, 800, 600, javafx.scene.paint.Color.TRANSPARENT);
        stage.setScene(scene);
        
        // 设置窗口始终置顶，便于隐藏阅读
        stage.setAlwaysOnTop(true);
        
        // 监听窗口大小变化，确保WebView自适应
        setupWebViewResizeListener();
        
        // 监听窗口关闭事件
        stage.setOnCloseRequest((WindowEvent event) -> {
            event.consume(); // 消费事件，防止窗口真正关闭
            exitMinimalMode(); // 调用退出方法
        });
    }
    
    private void setupWebViewResizeListener() {
        // 监听根容器大小变化，调整WebView大小
        root.widthProperty().addListener((obs, oldVal, newVal) -> {
            // 确保WebView宽度适应容器
            webView.setPrefWidth(newVal.doubleValue() - 20); // 减去左右padding(各10px)
            // 同时调整WebView的宽度，确保内容自适应
            webView.setMinWidth(newVal.doubleValue() - 20);
            webView.setMaxWidth(newVal.doubleValue() - 20);
        });
        
        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            // 确保WebView高度适应容器
            webView.setPrefHeight(newVal.doubleValue() - 20); // 减去上下padding(各10px)
            // 同时调整WebView的高度
            webView.setMinHeight(newVal.doubleValue() - 20);
            webView.setMaxHeight(newVal.doubleValue() - 20);
        });
    }
    
    private void setupEventHandlers() {
        // 不再通过stage.getScene()获取scene对象，避免Scene重复使用问题
        // ESC键和F3键退出极简模式 - 添加到stage上
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.F3) {
                exitMinimalMode();
                event.consume(); // 消费事件，防止传播
            }
        });
        
        // 右键菜单
        webView.setOnContextMenuRequested(e -> {
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem settingsItem = new MenuItem("设置");
            settingsItem.setOnAction(event -> {
                showSettingsDialog();
            });
            
            MenuItem exitItem = new MenuItem("退出极简模式");
            exitItem.setOnAction(event -> {
                exitMinimalMode();
            });
            
            contextMenu.getItems().addAll(settingsItem, exitItem);
            contextMenu.show(webView, e.getScreenX(), e.getScreenY());
        });
        
        // 设置窗口拖动
        setupWindowDragging();
        
        // 设置窗口调整大小
        setupWindowResizing();
    }
    
    private void setupScaleListener() {
        // 创建缩放比例变化监听器
        scaleChangeListener = (obs, oldVal, newVal) -> {
            // 当缩放比例变化时，重新加载当前文件以更新显示
            if (currentFile != null) {
                loadFile(currentFile);
            }
        };
        
        // 添加监听器到Preferences的变化
        // 注意：JavaFX的Preferences变化监听需要特殊处理
        // 这里我们采用轮询方式检查变化
        setupPreferencesPolling();
    }
    
    private void setupPreferencesPolling() {
        // 定期检查Preferences中的缩放比例变化
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            private double lastScale = appPrefs.getFontSize(100.0);
            private String lastFontFamily = appPrefs.getFontFamily("Microsoft YaHei");
            private double lastBackgroundOpacity = appPrefs.getBackgroundOpacity(0.9);  // 添加背景不透明度监听
            private double lastTextOpacity = appPrefs.getTextOpacity(1.0);  // 添加文字不透明度监听
            
            @Override
            public void handle(long now) {
                double currentScale = appPrefs.getFontSize(100.0);
                String currentFontFamily = appPrefs.getFontFamily("Microsoft YaHei");
                double currentBackgroundOpacity = appPrefs.getBackgroundOpacity(0.9);  // 获取当前背景不透明度
                double currentTextOpacity = appPrefs.getTextOpacity(1.0);  // 获取当前文字不透明度
                
                // 检查缩放比例、字体、背景不透明度或文字不透明度是否有变化
                // 降低背景不透明度变化的检测阈值，确保极小的变化也能被检测到
                if (Math.abs(currentScale - lastScale) > 0.1 || 
                    !lastFontFamily.equals(currentFontFamily) ||
                    Math.abs(currentBackgroundOpacity - lastBackgroundOpacity) > 0.001 ||  // 降低阈值以检测极小变化
                    Math.abs(currentTextOpacity - lastTextOpacity) > 0.01) {  // 文字不透明度变化
                    lastScale = currentScale;
                    lastFontFamily = currentFontFamily;
                    lastBackgroundOpacity = currentBackgroundOpacity;  // 更新背景不透明度记录
                    lastTextOpacity = currentTextOpacity;  // 更新文字不透明度记录
                    // 当设置变化时，重新加载当前文件以更新显示
                    if (currentFile != null) {
                        loadFile(currentFile);
                    }
                    // 更新窗口背景不透明度
                    updateWindowBackgroundOpacity(currentBackgroundOpacity);
                }
            }
        };
        timer.start();
    }
    
    /**
     * 更新窗口背景不透明度
     * @param opacity 不透明度值
     */
    private void updateWindowBackgroundOpacity(double opacity) {
        // 遍历root的所有子节点，找到StackPane容器并更新其背景不透明度
        for (javafx.scene.Node node : root.getChildren()) {
            if (node instanceof StackPane) {
                StackPane container = (StackPane) node;
                // 根据透明度调整阴影效果，透明度越低阴影越淡
                // 对于极低的不透明度值，需要特殊处理以确保可见性
                double shadowOpacity = Math.min(0.3, opacity * 0.5);
                
                // 当不透明度非常低时，使用更精细的阴影控制
                if (opacity < 0.05) {
                    shadowOpacity = opacity * 2; // 进一步降低阴影
                }
                
                // 当不透明度极低时（如1%），确保至少有一点可见性
                if (opacity < 0.01) {
                    // 保持最小的背景可见性，但仍保持高度透明
                    container.setStyle(
                        "-fx-background-color: rgba(255, 255, 255, " + Math.max(0.001, opacity) + ");" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, " + shadowOpacity + "), 5, 0, 0, 0);"
                    );
                } else {
                    container.setStyle(
                        "-fx-background-color: rgba(255, 255, 255, " + opacity + ");" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, " + shadowOpacity + "), 10, 0, 0, 0);"
                    );
                }
                
                // 同时更新容器内所有子节点的背景透明度（除了WebView）
                for (javafx.scene.Node child : container.getChildren()) {
                    if (!(child instanceof javafx.scene.web.WebView)) {
                        // 对于非WebView的子节点，应用背景透明度
                        if (child instanceof HBox) {
                            HBox hbox = (HBox) child;
                            hbox.setStyle("-fx-background-color: transparent;");
                        }
                    }
                }
                break;
            }
        }
        
        // 同时更新场景的填充颜色，确保窗口透明度正确
        if (stage.getScene() != null) {
            stage.getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
        }
        
        // 注意：不要设置root的背景样式，以免影响鼠标事件处理
        // root.setStyle("-fx-background-color: transparent;");
    }
    
    private void showSettingsDialog() {
        SettingsDialog settingsDialog = new SettingsDialog(stage);
        settingsDialog.showAndWait();
    }
    
    private void setupWindowDragging() {
        // 鼠标按下时记录起始位置 - 在整个根容器上监听
        root.setOnMousePressed((MouseEvent event) -> {
            // 只有在非调整大小区域才允许拖动
            if (resizeDirection.isEmpty()) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        
        // 鼠标拖动时移动窗口
        root.setOnMouseDragged((MouseEvent event) -> {
            // 只有在非调整大小状态才允许拖动
            if (!resizing && resizeDirection.isEmpty()) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
        
        // 设置鼠标悬停时的光标
        root.setOnMouseEntered((MouseEvent event) -> {
            if (resizeDirection.isEmpty()) {
                root.setCursor(Cursor.MOVE);
            }
        });
        
        root.setOnMouseExited((MouseEvent event) -> {
            if (resizeDirection.isEmpty()) {
                root.setCursor(Cursor.DEFAULT);
            }
        });
    }
    
    private void setupWindowResizing() {
        // 记录上一次拖动的位置，用于优化性能
        final double[] lastDragX = {0};
        final double[] lastDragY = {0};
        final long[] lastDragTime = {0};
        
        // 添加拖动阈值，避免微小移动触发拖动
        final double[] dragThreshold = {5}; // 5像素的拖动阈值
        final boolean[] dragStarted = {false};
        
        // 为根容器添加鼠标事件以实现边缘调整大小
        root.setOnMouseMoved((MouseEvent event) -> {
            double x = event.getX();
            double y = event.getY();
            double width = root.getWidth();
            double height = root.getHeight();
            
            // 边缘检测区域（10像素宽）
            int border = 10;
            
            // 检查鼠标位置并设置相应的光标
            if (x < border && y < border) {
                // 左上角
                root.setCursor(Cursor.NW_RESIZE);
                resizeDirection = "nw";
            } else if (x > width - border && y < border) {
                // 右上角
                root.setCursor(Cursor.NE_RESIZE);
                resizeDirection = "ne";
            } else if (x < border && y > height - border) {
                // 左下角
                root.setCursor(Cursor.SW_RESIZE);
                resizeDirection = "sw";
            } else if (x > width - border && y > height - border) {
                // 右下角
                root.setCursor(Cursor.SE_RESIZE);
                resizeDirection = "se";
            } else if (x < border) {
                // 左边
                root.setCursor(Cursor.W_RESIZE);
                resizeDirection = "w";
            } else if (x > width - border) {
                // 右边
                root.setCursor(Cursor.E_RESIZE);
                resizeDirection = "e";
            } else if (y > height - border) {
                // 下边
                root.setCursor(Cursor.S_RESIZE);
                resizeDirection = "s";
            } else if (y < border) {  // 上边框
                // 上边 - 允许拖动移动窗口，不支持调整高度
                root.setCursor(Cursor.MOVE);
                resizeDirection = ""; // 不设置调整方向，允许拖动
            } else {
                // 不在边缘区域且不在拖动状态
                if (!resizing) {
                    root.setCursor(Cursor.MOVE);
                    resizeDirection = "";
                }
            }
        });
        
        // 鼠标按下时开始调整大小或准备拖动
        root.setOnMousePressed((MouseEvent event) -> {
            // 只有在明确的调整方向时才开始调整大小
            if (!resizeDirection.isEmpty()) {
                resizing = true;
                resizeStartX = event.getScreenX();
                resizeStartY = event.getScreenY();
                resizeStartWidth = stage.getWidth();
                resizeStartHeight = stage.getHeight();
                dragStarted[0] = false; // 重置拖动开始标志
                // 请求鼠标捕获，确保即使鼠标移出窗口也能继续接收事件
                root.requestFocus();
            }
            // 如果在上边框区域，准备拖动窗口
            else if (event.getY() < 20) { // 上边框20像素区域
                // 记录鼠标按下时的屏幕坐标和窗口当前位置的差值
                xOffset = event.getScreenX() - stage.getX();
                yOffset = event.getScreenY() - stage.getY();
                // 初始化拖动位置记录
                lastDragX[0] = event.getScreenX();
                lastDragY[0] = event.getScreenY();
                lastDragTime[0] = System.currentTimeMillis();
                dragStarted[0] = false; // 重置拖动开始标志
                // 请求鼠标捕获，确保即使鼠标移出窗口也能继续接收事件
                root.requestFocus();
            }
        });
        
        // 鼠标拖动时调整窗口大小或移动窗口
        root.setOnMouseDragged((MouseEvent event) -> {
            // 如果正在调整大小
            if (resizing && !resizeDirection.isEmpty()) {
                // 实现拖动阈值检查
                if (!dragStarted[0]) {
                    double deltaX = Math.abs(event.getScreenX() - resizeStartX);
                    double deltaY = Math.abs(event.getScreenY() - resizeStartY);
                    if (Math.max(deltaX, deltaY) < dragThreshold[0]) {
                        return; // 未达到拖动阈值，不执行调整
                    }
                    dragStarted[0] = true;
                }
                
                double deltaX = event.getScreenX() - resizeStartX;
                double deltaY = event.getScreenY() - resizeStartY;
                
                double newWidth = resizeStartWidth;
                double newHeight = resizeStartHeight;
                double newX = stage.getX();
                double newY = stage.getY();
                
                switch (resizeDirection) {
                    case "e": // 右边
                        newWidth = Math.max(200, resizeStartWidth + deltaX);
                        break;
                    case "s": // 下边
                        newHeight = Math.max(150, resizeStartHeight + deltaY);
                        break;
                    case "w": // 左边
                        newWidth = Math.max(200, resizeStartWidth - deltaX);
                        // 修复窗口位置计算，确保窗口不会"飞出去"
                        newX = resizeStartX - (newWidth - resizeStartWidth);
                        break;
                    case "n": // 上边 - 不再支持调整高度
                        // 移除上边调整高度的逻辑，只允许移动窗口
                        break;
                    case "nw": // 左上角
                        newWidth = Math.max(200, resizeStartWidth - deltaX);
                        // 不再调整高度
                        // 修复窗口位置计算
                        newX = resizeStartX - (newWidth - resizeStartWidth);
                        break;
                    case "ne": // 右上角
                        newWidth = Math.max(200, resizeStartWidth + deltaX);
                        // 不再调整高度
                        break;
                    case "sw": // 左下角
                        newWidth = Math.max(200, resizeStartWidth - deltaX);
                        newHeight = Math.max(150, resizeStartHeight + deltaY);
                        // 修复窗口位置计算
                        newX = resizeStartX - (newWidth - resizeStartWidth);
                        break;
                    case "se": // 右下角
                        newWidth = Math.max(200, resizeStartWidth + deltaX);
                        newHeight = Math.max(150, resizeStartHeight + deltaY);
                        break;
                }
                
                // 限制拖动频率，避免卡顿
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDragTime[0] > 16) { // 约60FPS
                    // 应用新的窗口大小和位置
                    stage.setX(newX);
                    stage.setY(newY);
                    stage.setWidth(newWidth);
                    stage.setHeight(newHeight);
                    lastDragTime[0] = currentTime;
                }
            }
            // 如果在上边框区域，拖动窗口 - 修复拖动方向问题
            else if (resizeDirection.isEmpty()) { // 不再检查Y坐标，只要开始拖动就继续
                // 实现拖动阈值检查
                if (!dragStarted[0]) {
                    double deltaX = Math.abs(event.getScreenX() - (lastDragX[0]));
                    double deltaY = Math.abs(event.getScreenY() - (lastDragY[0]));
                    if (Math.max(deltaX, deltaY) < dragThreshold[0]) {
                        return; // 未达到拖动阈值，不执行拖动
                    }
                    dragStarted[0] = true;
                }
                
                // 限制拖动频率，避免卡顿
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDragTime[0] > 16) { // 约60FPS
                    // 修复窗口拖动方向问题，确保可以向任意方向拖动
                    // 使用正确的偏移量计算窗口新位置
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                    lastDragX[0] = event.getScreenX();
                    lastDragY[0] = event.getScreenY();
                    lastDragTime[0] = currentTime;
                }
            }
        });
        
        // 鼠标释放时结束调整大小
        root.setOnMouseReleased((MouseEvent event) -> {
            resizing = false;
            dragStarted[0] = false; // 重置拖动开始标志
        });
    }
    
    public void loadFile(File file) {
        try {
            currentFile = file;
            String content = documentEngine.parseDocument(file);
            
            if (file.getName().toLowerCase().endsWith(".md")) {
                // Markdown 文件使用 WebView 渲染（已包含样式）
                // 为极简模式添加透明背景支持，确保背景透明度设置生效
                String transparentContent = content.replace(
                    "background-color: transparent;", 
                    "background-color: transparent !important;"
                ).replace(
                    "</body>", 
                    "<style>body, html { background: transparent !important; background-color: transparent !important; }</style></body>"
                );
                
                // 确保表格行也使用透明背景
                transparentContent = transparentContent.replace(
                    "table tr {", 
                    "table tr { background-color: transparent !important;"
                ).replace(
                    "table tr:nth-child(2n) {", 
                    "table tr:nth-child(2n) { background-color: rgba(246, 248, 250, 0.7) !important;"
                );
                
                webView.getEngine().loadContent(transparentContent, "text/html");
            } else {
                // 其他文件直接显示文本（添加基本样式）
                // 从设置中获取缩放比例，默认为100%
                double scale = appPrefs.getFontSize(100.0);
                
                // 从设置中获取字体，默认为微软雅黑
                String fontFamily = appPrefs.getFontFamily("Microsoft YaHei");
                
                // 从设置中获取文字不透明度，默认为1.0
                double textOpacity = appPrefs.getTextOpacity(1.0);
                
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
                
                // 将文字不透明度应用到颜色上
                String textColor = String.format("rgba(51, 51, 51, %.2f)", textOpacity);
                
                String styledContent = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset='UTF-8'>\n" +
                        "    <style>\n" +
                        "        body {\n" +
                        "            font-family: " + fontFamily + ", -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji';\n" +
                        "            font-size: " + formattedFontSize + "px;\n" +
                        "            line-height: 1.6;\n" +
                        "            color: " + textColor + ";\n" +  // 应用文字不透明度
                        "            background-color: transparent;\n" +  // 使用透明背景，让极简模式的背景不透明度设置生效
                        "            padding: " + roundedPadding + "px;\n" +
                        "            max-width: none;\n" +
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
        } catch (IOException e) {
            showError("文件读取错误", "无法读取文件: " + e.getMessage());
        } catch (Exception e) {
            showError("文件解析错误", "无法解析文件: " + e.getMessage());
        }
    }
    
    private void exitMinimalMode() {
        // 隐藏极简模式窗口
        stage.hide();
        
        // 切换到正常模式
        if (currentFile != null) {
            mainController.switchToNormalMode(currentFile);
        }
    }
    
    public void showWindow() {
        // 在显示窗口前，更新背景不透明度为最新设置值
        updateWindowBackgroundOpacity(appPrefs.getBackgroundOpacity(0.9));
        
        // 确保场景的透明度设置正确
        if (stage.getScene() != null) {
            stage.getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
        }
        
        stage.show();
        stage.sizeToScene();
        stage.requestFocus();
    }
    
    /**
     * 隐藏窗口
     */
    public void hideWindow() {
        stage.hide();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Scene getScene() {
        // 不再提供getScene方法，避免Scene重复使用问题
        // 改为使用showWindow和hideWindow方法来控制窗口显示
        return null;
    }
}