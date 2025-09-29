package com.thief.reader.ui;

import com.thief.reader.config.ConfigManager;
import com.thief.reader.config.AppPreferences;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.text.DecimalFormat;
import java.util.prefs.Preferences;

/**
 * 设置对话框
 */
public class SettingsDialog extends Dialog<Void> {
    
    private final ConfigManager configManager;
    private final AppPreferences appPrefs;
    
    // UI组件
    private ComboBox<String> defaultModeCombo;
    private Slider fontSizeSlider;  // 现在用于缩放比例
    private ComboBox<String> fontFamilyCombo;  // 字体选择下拉框
    private Slider backgroundOpacitySlider;
    private Slider textOpacitySlider;
    private ComboBox<String> pdfQualityCombo;
    private CheckBox autoSavePositionCheckBox;
    
    public SettingsDialog(Window owner) {
        this.configManager = ConfigManager.getInstance();
        this.appPrefs = AppPreferences.getInstance();
        
        initOwner(owner);
        setTitle("首选项设置");
        setHeaderText("应用程序设置");
        
        // 创建对话框面板
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        
        // 添加设置项
        root.getChildren().addAll(
            createDefaultModeSettings(),
            createFontSizeSettings(),  // 现在创建缩放比例设置
            createFontFamilySettings(),  // 添加字体设置
            createOpacitySettings(),
            createPdfSettings(),
            createAutoSaveSettings()
        );
        
        // 创建按钮
        ButtonType okButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);
        
        // 设置内容
        getDialogPane().setContent(root);
        
        // 处理确定按钮点击事件
        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                saveSettings();
            }
            return null;
        });
        
        // 加载现有设置
        loadSettings();
    }
    
    /**
     * 创建默认模式设置面板
     */
    private VBox createDefaultModeSettings() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label("默认打开模式:");
        defaultModeCombo = new ComboBox<>();
        defaultModeCombo.getItems().addAll("正常模式", "极简模式");
        defaultModeCombo.setValue("正常模式");
        
        box.getChildren().addAll(label, defaultModeCombo);
        return box;
    }
    
    /**
     * 创建缩放比例设置面板
     */
    private VBox createFontSizeSettings() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label("页面缩放比例:");
        // 修改滑块范围为20-300，默认100
        fontSizeSlider = new Slider(20, 300, 100);
        fontSizeSlider.setShowTickLabels(true);
        fontSizeSlider.setShowTickMarks(true);
        fontSizeSlider.setMajorTickUnit(50);
        fontSizeSlider.setBlockIncrement(10);
        // 设置最小单位为10
        fontSizeSlider.setMinorTickCount(4); // 50/10-1=4个小刻度
        
        Label valueLabel = new Label("100%");
        // 使用DecimalFormat确保显示整数百分比
        DecimalFormat df = new DecimalFormat("#");
        fontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // 四舍五入到最近的10的倍数
            int roundedValue = (int) Math.round(newVal.doubleValue() / 10.0) * 10;
            fontSizeSlider.setValue(roundedValue);
            valueLabel.setText(roundedValue + "%");
        });
        
        HBox sliderBox = new HBox(10, fontSizeSlider, valueLabel);
        box.getChildren().addAll(label, sliderBox);
        return box;
    }
    
    /**
     * 创建字体设置面板
     */
    private VBox createFontFamilySettings() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label("字体选择:");
        fontFamilyCombo = new ComboBox<>();
        // 添加常用的中文字体选项
        fontFamilyCombo.getItems().addAll(
            "Microsoft YaHei",  // 微软雅黑
            "SimHei",           // 黑体
            "SimSun",           // 宋体
            "KaiTi",            // 楷体
            "FangSong",         // 仿宋
            "Arial",
            "Helvetica",
            "Times New Roman",
            "Courier New"
        );
        fontFamilyCombo.setValue("Microsoft YaHei");  // 默认微软雅黑
        
        box.getChildren().addAll(label, fontFamilyCombo);
        return box;
    }
    
    /**
     * 创建不透明度设置面板
     */
    private VBox createOpacitySettings() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("极简模式不透明度设置:");
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        // 背景不透明度
        Label bgLabel = new Label("背景不透明度:");
        backgroundOpacitySlider = new Slider(0, 1, 0.9);
        backgroundOpacitySlider.setShowTickLabels(true);
        backgroundOpacitySlider.setShowTickMarks(true);
        backgroundOpacitySlider.setMajorTickUnit(0.2);
        backgroundOpacitySlider.setBlockIncrement(0.01); // 更精细的调节步长
        backgroundOpacitySlider.setMinorTickCount(10); // 增加小刻度数量
        
        Label bgValueLabel = new Label("90%");
        backgroundOpacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // 优化显示，对于极小的值也能够清晰显示
            double value = newVal.doubleValue();
            if (value < 0.01) {
                bgValueLabel.setText(String.format("%.2f%%", value * 100));
            } else {
                bgValueLabel.setText(String.format("%.0f%%", value * 100));
            }
        });
        
        HBox bgSliderBox = new HBox(10, backgroundOpacitySlider, bgValueLabel);
        
        // 文字不透明度
        Label textLabel = new Label("文字不透明度:");
        textOpacitySlider = new Slider(0, 1, 1.0);
        textOpacitySlider.setShowTickLabels(true);
        textOpacitySlider.setShowTickMarks(true);
        textOpacitySlider.setMajorTickUnit(0.2);
        textOpacitySlider.setBlockIncrement(0.01); // 更精细的调节步长
        textOpacitySlider.setMinorTickCount(10); // 增加小刻度数量
        
        Label textValueLabel = new Label("100%");
        textOpacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // 优化显示，对于极小的值也能够清晰显示
            double value = newVal.doubleValue();
            if (value < 0.01) {
                textValueLabel.setText(String.format("%.2f%%", value * 100));
            } else {
                textValueLabel.setText(String.format("%.0f%%", value * 100));
            }
        });
        
        HBox textSliderBox = new HBox(10, textOpacitySlider, textValueLabel);
        
        box.getChildren().addAll(titleLabel, bgLabel, bgSliderBox, textLabel, textSliderBox);
        return box;
    }
    
    /**
     * 创建PDF设置面板
     */
    private VBox createPdfSettings() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label("PDF渲染质量:");
        pdfQualityCombo = new ComboBox<>();
        pdfQualityCombo.getItems().addAll("低质量", "中等质量", "高质量");
        pdfQualityCombo.setValue("中等质量");
        
        box.getChildren().addAll(label, pdfQualityCombo);
        return box;
    }
    
    /**
     * 创建自动保存设置面板
     */
    private VBox createAutoSaveSettings() {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        
        autoSavePositionCheckBox = new CheckBox("自动保存阅读位置");
        autoSavePositionCheckBox.setSelected(true);
        
        box.getChildren().addAll(autoSavePositionCheckBox);
        return box;
    }
    
    /**
     * 加载现有设置
     */
    private void loadSettings() {
        // 默认模式
        String defaultMode = configManager.getConfig().getDefaultMode();
        defaultModeCombo.setValue("minimal".equals(defaultMode) ? "极简模式" : "正常模式");
        
        // 缩放比例（之前是字体大小）
        double scale = appPrefs.getFontSize(100.0);  // 现在存储的是缩放比例，默认100
        fontSizeSlider.setValue(scale);
        
        // 字体设置
        String fontFamily = appPrefs.getFontFamily("Microsoft YaHei");
        fontFamilyCombo.setValue(fontFamily);
        
        // 背景不透明度
        double bgOpacity = appPrefs.getBackgroundOpacity(0.9);
        backgroundOpacitySlider.setValue(bgOpacity);
        
        // 文字不透明度
        double textOpacity = appPrefs.getTextOpacity(1.0);
        textOpacitySlider.setValue(textOpacity);
        
        // PDF质量
        String pdfQuality = appPrefs.getPdfQuality("中等质量");
        pdfQualityCombo.setValue(pdfQuality);
        
        // 自动保存位置
        boolean autoSave = appPrefs.getAutoSavePosition(true);
        autoSavePositionCheckBox.setSelected(autoSave);
    }
    
    /**
     * 保存设置
     */
    private void saveSettings() {
        // 保存默认模式
        String selectedMode = defaultModeCombo.getValue();
        configManager.getConfig().setDefaultMode("极简模式".equals(selectedMode) ? "minimal" : "normal");
        
        // 保存缩放比例（之前是字体大小）
        appPrefs.setFontSize(fontSizeSlider.getValue());  // 现在存储的是缩放比例
        
        // 保存字体设置
        appPrefs.setFontFamily(fontFamilyCombo.getValue());
        
        // 保存背景不透明度
        appPrefs.setBackgroundOpacity(backgroundOpacitySlider.getValue());
        
        // 保存文字不透明度
        appPrefs.setTextOpacity(textOpacitySlider.getValue());
        
        // 保存PDF质量
        appPrefs.setPdfQuality(pdfQualityCombo.getValue());
        
        // 保存自动保存位置设置
        appPrefs.setAutoSavePosition(autoSavePositionCheckBox.isSelected());
        
        // 保存配置
        configManager.saveConfig();
    }
}