package com.thief.reader.core;

import java.io.File;
import java.io.IOException;

/**
 * 文档解析器接口
 */
public interface DocumentParser {
    
    /**
     * 解析文档文件
     * @param file 文档文件
     * @return 解析后的内容
     * @throws IOException 文件读取异常
     */
    String parse(File file) throws IOException;
    
    /**
     * 检查是否支持该文件类型
     * @param file 文件
     * @return 是否支持
     */
    boolean supports(File file);
}