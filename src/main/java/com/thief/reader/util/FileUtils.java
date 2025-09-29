package com.thief.reader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 文件工具类
 */
public class FileUtils {
    
    /**
     * 检测文件编码
     * @param file 文件
     * @return 推测的字符编码
     */
    public static Charset detectCharset(File file) {
        // 首先尝试检测BOM
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bom = new byte[4];
            int bytesRead = fis.read(bom);
            
            if (bytesRead >= 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
                return StandardCharsets.UTF_8;
            }
            
            if (bytesRead >= 4 && bom[0] == (byte) 0x00 && bom[1] == (byte) 0x00 && 
                bom[2] == (byte) 0xFE && bom[3] == (byte) 0xFF) {
                return Charset.forName("UTF-32BE");
            }
            
            if (bytesRead >= 4 && bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE && 
                bom[2] == (byte) 0x00 && bom[3] == (byte) 0x00) {
                return Charset.forName("UTF-32LE");
            }
            
            if (bytesRead >= 2 && bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF) {
                return Charset.forName("UTF-16BE");
            }
            
            if (bytesRead >= 2 && bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
                return Charset.forName("UTF-16LE");
            }
        } catch (IOException e) {
            // 忽略异常，使用默认编码
        }
        
        // 如果没有BOM，使用默认的UTF-8编码
        return StandardCharsets.UTF_8;
    }
    
    /**
     * 获取文件扩展名
     * @param file 文件
     * @return 扩展名（小写）
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return name.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * 检查文件是否为支持的文档类型
     * @param file 文件
     * @return 是否支持
     */
    public static boolean isSupportedDocument(File file) {
        String ext = getFileExtension(file);
        return "md".equals(ext) || "txt".equals(ext) || "pdf".equals(ext);
    }
}