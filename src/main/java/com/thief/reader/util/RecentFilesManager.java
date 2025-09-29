package com.thief.reader.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 最近打开文件管理器
 */
public class RecentFilesManager {
    
    private static final int MAX_RECENT_FILES = 10;
    private final List<String> recentFiles;
    
    public RecentFilesManager() {
        recentFiles = new ArrayList<>();
        loadRecentFiles();
    }
    
    /**
     * 添加文件到最近打开列表
     * @param file 文件
     */
    public void addFile(File file) {
        String filePath = file.getAbsolutePath();
        
        // 如果文件已存在，先移除
        recentFiles.remove(filePath);
        
        // 添加到列表开头
        recentFiles.add(0, filePath);
        
        // 保持列表大小在限制范围内
        if (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles.remove(recentFiles.size() - 1);
        }
        
        saveRecentFiles();
    }
    
    /**
     * 获取最近打开的文件列表
     * @return 文件路径列表
     */
    public List<String> getRecentFiles() {
        return new ArrayList<>(recentFiles);
    }
    
    /**
     * 加载最近打开的文件列表
     */
    private void loadRecentFiles() {
        // 简单实现，实际项目中可以从配置文件或数据库加载
        // 这里为了演示，暂时留空
    }
    
    /**
     * 保存最近打开的文件列表
     */
    private void saveRecentFiles() {
        // 简单实现，实际项目中可以保存到配置文件或数据库
    }
}