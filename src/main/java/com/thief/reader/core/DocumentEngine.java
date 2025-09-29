package com.thief.reader.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档引擎核心类
 */
public class DocumentEngine {
    
    public DocumentEngine() {
        // 构造函数保持空实现
    }
    
    /**
     * 解析文档
     * @param file 文档文件
     * @return 解析后的内容
     * @throws IOException 文件读取异常
     * @throws UnsupportedOperationException 不支持的文件类型
     */
    public String parseDocument(File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".md")) {
            // 每次都创建新的MarkdownParser实例以确保获取最新的设置
            return new MarkdownParser().parse(file);
        } else if (file.getName().toLowerCase().endsWith(".txt")) {
            return new TextParser().parse(file);
        } else if (file.getName().toLowerCase().endsWith(".pdf")) {
            return new PdfParser().parse(file);
        }
        throw new UnsupportedOperationException("不支持的文件类型: " + file.getName());
    }
}