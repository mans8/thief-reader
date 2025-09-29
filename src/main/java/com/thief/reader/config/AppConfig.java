package com.thief.reader.config;

/**
 * 应用程序配置类
 */
public class AppConfig {
    
    // 默认打开模式 (normal/minimal)
    private String defaultMode = "normal";
    
    // 字体大小
    private int fontSize = 14;
    
    // 极简模式背景不透明度 (0.0-1.0)
    private double minimalBackgroundOpacity = 0.9;
    
    // 极简模式文字不透明度 (0.0-1.0)
    private double minimalTextOpacity = 1.0;
    
    // PDF渲染质量 (low/medium/high)
    private String pdfRenderQuality = "medium";
    
    // 是否自动保存阅读位置
    private boolean autoSavePosition = true;
    
    // getter和setter方法
    public String getDefaultMode() {
        return defaultMode;
    }
    
    public void setDefaultMode(String defaultMode) {
        this.defaultMode = defaultMode;
    }
    
    public int getFontSize() {
        return fontSize;
    }
    
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
    
    public double getMinimalBackgroundOpacity() {
        return minimalBackgroundOpacity;
    }
    
    public void setMinimalBackgroundOpacity(double minimalBackgroundOpacity) {
        this.minimalBackgroundOpacity = minimalBackgroundOpacity;
    }
    
    public double getMinimalTextOpacity() {
        return minimalTextOpacity;
    }
    
    public void setMinimalTextOpacity(double minimalTextOpacity) {
        this.minimalTextOpacity = minimalTextOpacity;
    }
    
    public String getPdfRenderQuality() {
        return pdfRenderQuality;
    }
    
    public void setPdfRenderQuality(String pdfRenderQuality) {
        this.pdfRenderQuality = pdfRenderQuality;
    }
    
    public boolean isAutoSavePosition() {
        return autoSavePosition;
    }
    
    public void setAutoSavePosition(boolean autoSavePosition) {
        this.autoSavePosition = autoSavePosition;
    }
}