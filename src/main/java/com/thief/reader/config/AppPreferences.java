package com.thief.reader.config;

import java.util.prefs.Preferences;

/**
 * 应用程序偏好设置管理类
 * 提供统一的Preferences节点访问
 */
public class AppPreferences {
    
    private static final String NODE_NAME = "com.thief.reader";
    private static AppPreferences instance;
    private final Preferences prefs;
    
    private AppPreferences() {
        this.prefs = Preferences.userRoot().node(NODE_NAME);
    }
    
    public static synchronized AppPreferences getInstance() {
        if (instance == null) {
            instance = new AppPreferences();
        }
        return instance;
    }
    
    /**
     * 获取Preferences实例
     * @return Preferences实例
     */
    public Preferences getPreferences() {
        return prefs;
    }
    
    /**
     * 获取字体大小/缩放比例设置
     * @param defaultValue 默认值
     * @return 字体大小或缩放比例
     */
    public double getFontSize(double defaultValue) {
        return prefs.getDouble("font_size", defaultValue);
    }
    
    /**
     * 设置字体大小/缩放比例
     * @param fontSize 字体大小或缩放比例
     */
    public void setFontSize(double fontSize) {
        prefs.putDouble("font_size", fontSize);
    }
    
    /**
     * 获取字体设置
     * @param defaultValue 默认值
     * @return 字体名称
     */
    public String getFontFamily(String defaultValue) {
        return prefs.get("font_family", defaultValue);
    }
    
    /**
     * 设置字体
     * @param fontFamily 字体名称
     */
    public void setFontFamily(String fontFamily) {
        prefs.put("font_family", fontFamily);
    }
    
    /**
     * 获取背景不透明度设置
     * @param defaultValue 默认值
     * @return 背景不透明度
     */
    public double getBackgroundOpacity(double defaultValue) {
        return prefs.getDouble("background_opacity", defaultValue);
    }
    
    /**
     * 设置背景不透明度
     * @param opacity 不透明度
     */
    public void setBackgroundOpacity(double opacity) {
        prefs.putDouble("background_opacity", opacity);
    }
    
    /**
     * 获取文字不透明度设置
     * @param defaultValue 默认值
     * @return 文字不透明度
     */
    public double getTextOpacity(double defaultValue) {
        return prefs.getDouble("text_opacity", defaultValue);
    }
    
    /**
     * 设置文字不透明度
     * @param opacity 不透明度
     */
    public void setTextOpacity(double opacity) {
        prefs.putDouble("text_opacity", opacity);
    }
    
    /**
     * 获取PDF质量设置
     * @param defaultValue 默认值
     * @return PDF质量
     */
    public String getPdfQuality(String defaultValue) {
        return prefs.get("pdf_quality", defaultValue);
    }
    
    /**
     * 设置PDF质量
     * @param quality 质量
     */
    public void setPdfQuality(String quality) {
        prefs.put("pdf_quality", quality);
    }
    
    /**
     * 获取自动保存位置设置
     * @param defaultValue 默认值
     * @return 是否自动保存位置
     */
    public boolean getAutoSavePosition(boolean defaultValue) {
        return prefs.getBoolean("auto_save_position", defaultValue);
    }
    
    /**
     * 设置自动保存位置
     * @param autoSave 是否自动保存
     */
    public void setAutoSavePosition(boolean autoSave) {
        prefs.putBoolean("auto_save_position", autoSave);
    }
}