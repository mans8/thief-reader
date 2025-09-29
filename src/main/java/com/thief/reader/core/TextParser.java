package com.thief.reader.core;

import com.thief.reader.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 纯文本文档解析器
 */
public class TextParser implements DocumentParser {
    
    @Override
    public String parse(File file) throws IOException {
        // 检测文件编码
        Charset charset = FileUtils.detectCharset(file);
        return new String(Files.readAllBytes(Paths.get(file.toURI())), charset);
    }
    
    @Override
    public boolean supports(File file) {
        return file.getName().toLowerCase().endsWith(".txt");
    }
}