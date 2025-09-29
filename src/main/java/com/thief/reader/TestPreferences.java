package com.thief.reader;

import com.thief.reader.config.AppPreferences;
import java.util.prefs.Preferences;

public class TestPreferences {
    public static void main(String[] args) {
        AppPreferences appPrefs = AppPreferences.getInstance();
        
        // 检查当前存储的字体大小
        double currentFontSize = appPrefs.getFontSize(16.0);
        System.out.println("当前存储的字体大小: " + currentFontSize);
        
        // 检查底层Preferences中的值
        Preferences prefs = appPrefs.getPreferences();
        double rawValue = prefs.getDouble("font_size", 16.0);
        System.out.println("底层Preferences中的值: " + rawValue);
        
        // 测试设置字体大小
        System.out.println("设置字体大小为20.0");
        appPrefs.setFontSize(20.0);
        
        // 测试读取字体大小
        double fontSize = appPrefs.getFontSize(16.0);
        System.out.println("读取到的字体大小: " + fontSize);
        
        // 再次检查底层Preferences中的值
        rawValue = prefs.getDouble("font_size", 16.0);
        System.out.println("设置后底层Preferences中的值: " + rawValue);
        
        // 再次设置为其他值测试
        System.out.println("设置字体大小为18.5");
        appPrefs.setFontSize(18.5);
        
        // 再次读取
        fontSize = appPrefs.getFontSize(16.0);
        System.out.println("读取到的字体大小: " + fontSize);

        // 再次检查底层Preferences中的值
        rawValue = prefs.getDouble("font_size", 16.0);
        System.out.println("设置后底层Preferences中的值: " + rawValue);
    }
}