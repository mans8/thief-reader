package com.thief.reader.ui;

import com.thief.reader.config.ConfigManager;
import com.thief.reader.core.DocumentEngine;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

/**
 * 主控制器
 */
public class MainController {
    
    private final Stage primaryStage;
    private final DocumentEngine documentEngine;
    private final ConfigManager configManager;
    private NormalModeController normalModeController;
    private MinimalModeController minimalModeController;
    private Scene normalModeScene; // 保存正常模式的Scene对象
    
    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.documentEngine = new DocumentEngine();
        this.configManager = ConfigManager.getInstance();
        initUI();
    }
    
    private void initUI() {
        // 初始化正常模式
        normalModeController = new NormalModeController(this, documentEngine);
        // 创建新的Scene对象并保存引用
        normalModeScene = new Scene(normalModeController.getRoot(), 1000, 700);
        primaryStage.setTitle("Thief Java Reader");
        primaryStage.setScene(normalModeScene);
        
        // 根据配置决定启动模式
        if ("minimal".equals(configManager.getConfig().getDefaultMode())) {
            // 启动极简模式（需要先打开文件）
            // 这里简化处理，实际应用中可能需要其他逻辑
        }
        
        primaryStage.show();
    }
    
    /**
     * 切换到极简模式
     */
    public void switchToMinimalMode(File currentFile) {
        if (minimalModeController == null) {
            minimalModeController = new MinimalModeController(this, documentEngine);
        }
        minimalModeController.loadFile(currentFile);
        // 直接显示极简模式窗口，不通过Scene对象
        minimalModeController.showWindow();
        
        // 隐藏正常模式窗口
        primaryStage.hide();
    }
    
    /**
     * 切换到正常模式
     */
    public void switchToNormalMode(File currentFile) {
        // 隐藏极简模式窗口（在创建新场景之前）
        if (minimalModeController != null) {
            minimalModeController.hideWindow();
        }
        
        // 重新创建NormalModeController实例，避免Scene重复使用问题
        normalModeController = new NormalModeController(this, documentEngine);
        normalModeController.loadFile(currentFile);
        
        // 创建新的Scene对象并保存引用
        normalModeScene = new Scene(normalModeController.getRoot(), 1000, 700);
        primaryStage.setScene(normalModeScene);
        primaryStage.show(); // 确保正常模式窗口显示
        primaryStage.sizeToScene();
        primaryStage.requestFocus();
    }
    
    /**
     * 获取主舞台
     * @return 主舞台
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * 获取配置管理器
     * @return 配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
}