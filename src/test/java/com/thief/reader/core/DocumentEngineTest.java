package com.thief.reader.core;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * 文档引擎测试类
 */
public class DocumentEngineTest {
    
    private Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
    
    @Test
    public void testMarkdownParser() throws IOException {
        DocumentEngine engine = new DocumentEngine();
        Path markdownFile = tempDir.resolve("test.md");
        String content = "# 测试标题\n\n这是一个测试内容。";
        Files.write(markdownFile, content.getBytes());
        
        try {
            String result = engine.parseDocument(markdownFile.toFile());
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } catch (Exception e) {
            fail("解析Markdown文件时出现异常: " + e.getMessage());
        } finally {
            // 清理测试文件
            Files.deleteIfExists(markdownFile);
        }
    }
    
    @Test
    public void testTextParser() throws IOException {
        DocumentEngine engine = new DocumentEngine();
        Path textFile = tempDir.resolve("test.txt");
        String content = "这是一段测试文本。";
        Files.write(textFile, content.getBytes());
        
        try {
            String result = engine.parseDocument(textFile.toFile());
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } catch (Exception e) {
            fail("解析文本文件时出现异常: " + e.getMessage());
        } finally {
            // 清理测试文件
            Files.deleteIfExists(textFile);
        }
    }
    
    @Test
    public void testUnsupportedFile() throws IOException {
        DocumentEngine engine = new DocumentEngine();
        Path unsupportedFile = tempDir.resolve("test.xyz");
        Files.write(unsupportedFile, "content".getBytes());
        
        try {
            engine.parseDocument(unsupportedFile.toFile());
            fail("应该抛出UnsupportedOperationException异常");
        } catch (UnsupportedOperationException e) {
            // 预期的异常
            assertTrue(e.getMessage().contains("不支持的文件类型"));
        } catch (Exception e) {
            fail("应该抛出UnsupportedOperationException异常，但实际抛出了: " + e.getClass().getName());
        } finally {
            // 清理测试文件
            Files.deleteIfExists(unsupportedFile);
        }
    }
}