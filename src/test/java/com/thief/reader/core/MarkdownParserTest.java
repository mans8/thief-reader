package com.thief.reader.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MarkdownParserTest {
    
    @Test
    public void testTableRendering() throws IOException {
        // 创建一个包含表格的Markdown文件
        String markdownContent = "# 测试表格\n\n" +
                "这是一个表格测试：\n\n" +
                "| 姓名 | 年龄 | 城市 |\n" +
                "|------|------|------|\n" +
                "| 张三 | 25   | 北京 |\n" +
                "| 李四 | 30   | 上海 |\n" +
                "| 王五 | 28   | 广州 |\n";
        
        // 写入临时文件
        File tempFile = File.createTempFile("test_table", ".md");
        Files.write(tempFile.toPath(), markdownContent.getBytes("UTF-8"));
        
        // 使用MarkdownParser解析
        MarkdownParser parser = new MarkdownParser();
        String htmlResult = parser.parse(tempFile);
        
        // 验证结果包含表格HTML标签
        assertTrue("HTML结果应包含<table>标签", htmlResult.contains("<table>"));
        assertTrue("HTML结果应包含<th>标签", htmlResult.contains("<th>"));
        assertTrue("HTML结果应包含<td>标签", htmlResult.contains("<td>"));
        assertTrue("HTML结果应包含表格数据", htmlResult.contains("张三"));
        
        // 清理临时文件
        tempFile.delete();
    }
}