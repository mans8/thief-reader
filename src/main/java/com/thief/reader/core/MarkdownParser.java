package com.thief.reader.core;

import com.thief.reader.config.AppPreferences;
import com.thief.reader.util.FileUtils;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Markdown文档解析器
 */
public class MarkdownParser implements DocumentParser {
    
    private final Parser parser;
    private final HtmlRenderer renderer;
    private final AppPreferences appPrefs;
    
    public MarkdownParser() {
        MutableDataSet options = new MutableDataSet();
        // 启用表格扩展
        options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));
        
        // 配置解析选项
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
        this.appPrefs = AppPreferences.getInstance();
    }
    
    @Override
    public String parse(File file) throws IOException {
        // 检测文件编码
        Charset charset = FileUtils.detectCharset(file);
        String content = new String(Files.readAllBytes(Paths.get(file.toURI())), charset);
        
        // 解析Markdown并渲染为HTML
        String htmlContent = renderer.render(parser.parse(content));
        
        // 包装在带样式的HTML模板中
        return wrapInStyledTemplate(htmlContent);
    }
    
    /**
     * 将Markdown生成的HTML内容包装在带样式的模板中
     * @param content Markdown生成的HTML内容
     * @return 带样式的完整HTML文档
     */
    private String wrapInStyledTemplate(String content) {
        // 从设置中获取缩放比例，默认为100%
        double scale = appPrefs.getFontSize(100.0);
        
        // 从设置中获取字体，默认为微软雅黑
        String fontFamily = appPrefs.getFontFamily("Microsoft YaHei");
        
        // 从设置中获取文字不透明度，默认为1.0
        double textOpacity = appPrefs.getTextOpacity(1.0);
        
        // 计算实际字体大小（基于默认16px）
        double baseFontSize = 16.0;
        double actualFontSize = baseFontSize * (scale / 100.0);
        
        // 四舍五入到整数
        int roundedFontSize = (int) Math.round(actualFontSize);
        String formattedFontSize = String.valueOf(roundedFontSize);
        
        // 计算其他基于缩放比例的尺寸
        double scaledPadding = 20.0 * (scale / 100.0);
        // 四舍五入padding到整数
        int roundedPadding = (int) Math.round(scaledPadding);
        
        // 将文字不透明度应用到颜色上
        String textColor = String.format("rgba(51, 51, 51, %.2f)", textOpacity);
        
        // 极简模式下不设置固定的背景色，让背景透明度设置生效
        // 正常模式下使用纯白色背景
        String backgroundColor = "transparent";  // 使用透明背景，让极简模式的背景不透明度设置生效
        
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: '" + fontFamily + "', -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji';\n" +
                "            font-size: " + formattedFontSize + "px;\n" +
                "            line-height: 1.6;\n" +
                "            color: " + textColor + ";\n" +  // 应用文字不透明度
                "            background-color: " + backgroundColor + ";\n" +  // 使用透明背景
                "            padding: " + roundedPadding + "px;\n" +
                "            margin: 0 auto;\n" +
                "            width: 100%;\n" +
                "            max-width: 100%;\n" +
                "            box-sizing: border-box;\n" +
                "            word-wrap: break-word;\n" +
                "            -webkit-font-smoothing: antialiased;\n" +
                "            -moz-osx-font-smoothing: grayscale;\n" +
                "            text-rendering: optimizeLegibility;\n" +
                "            -webkit-text-size-adjust: 100%;\n" +  // 防止iOS Safari自动调整字体大小
                "            -ms-text-size-adjust: 100%;\n" +      // 防止IE自动调整字体大小
                "            text-shadow: 1px 1px 1px rgba(0,0,0,0.004);\n" +  // 轻微阴影提升清晰度
                "        }\n" +
                "        h1, h2, h3, h4, h5, h6 {\n" +
                "            margin-top: 24px;\n" +
                "            margin-bottom: 16px;\n" +
                "            font-weight: 600;\n" +
                "            line-height: 1.25;\n" +
                "        }\n" +
                "        h1 {\n" +
                "            padding-bottom: 0.3em;\n" +
                "            font-size: 2em;\n" +
                "            border-bottom: 1px solid #eaecef;\n" +
                "        }\n" +
                "        h2 {\n" +
                "            padding-bottom: 0.3em;\n" +
                "            font-size: 1.5em;\n" +
                "            border-bottom: 1px solid #eaecef;\n" +
                "        }\n" +
                "        p {\n" +
                "            margin-top: 0;\n" +
                "            margin-bottom: 16px;\n" +
                "            word-wrap: break-word;\n" +
                "        }\n" +
                "        a {\n" +
                "            color: #0366d6;\n" +
                "            text-decoration: none;\n" +
                "        }\n" +
                "        a:hover {\n" +
                "            text-decoration: underline;\n" +
                "        }\n" +
                "        code {\n" +
                "            padding: 0.2em 0.4em;\n" +
                "            margin: 0;\n" +
                "            font-size: 85%;\n" +
                "            background-color: rgba(27,31,35,0.05);\n" +
                "            border-radius: 3px;\n" +
                "            font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;\n" +
                "            word-wrap: break-word;\n" +
                "            white-space: pre-wrap;\n" +
                "        }\n" +
                "        pre {\n" +
                "            padding: 16px;\n" +
                "            overflow: auto;\n" +
                "            font-size: 85%;\n" +
                "            line-height: 1.45;\n" +
                "            background-color: #f6f8fa;\n" +
                "            border-radius: 3px;\n" +
                "            max-width: 100%;\n" +
                "            box-sizing: border-box;\n" +
                "        }\n" +
                "        pre > code {\n" +
                "            padding: 0;\n" +
                "            margin: 0;\n" +
                "            font-size: 100%;\n" +
                "            word-break: normal;\n" +
                "            white-space: pre;\n" +
                "            background: transparent;\n" +
                "            border: 0;\n" +
                "        }\n" +
                "        blockquote {\n" +
                "            padding: 0 1em;\n" +
                "            color: #6a737d;\n" +
                "            border-left: 0.25em solid #dfe2e5;\n" +
                "            margin: 0;\n" +
                "        }\n" +
                "        ul, ol {\n" +
                "            padding-left: 2em;\n" +
                "            margin-top: 0;\n" +
                "            margin-bottom: 16px;\n" +
                "        }\n" +
                "        li {\n" +
                "            margin-top: 0.25em;\n" +
                "            word-wrap: break-word;\n" +
                "        }\n" +
                "        li + li {\n" +
                "            margin-top: 0.25em;\n" +
                "        }\n" +
                "        table {\n" +
                "            display: block;\n" +
                "            width: 100%;\n" +
                "            overflow: auto;\n" +
                "            border-collapse: collapse;\n" +
                "            margin-bottom: 16px;\n" +
                "        }\n" +
                "        table th {\n" +
                "            font-weight: bold;\n" +
                "            background-color: #f6f8fa;\n" +
                "        }\n" +
                "        table th, table td {\n" +
                "            padding: 6px 13px;\n" +
                "            border: 1px solid #dfe2e5;\n" +
                "            word-wrap: break-word;\n" +
                "        }\n" +
                "        table tr {\n" +
                "            background-color: transparent !important;  // 使用透明背景并确保优先级，让背景不透明度设置生效\n" +
                "            border-top: 1px solid #c6cbd1;\n" +
                "        }\n" +
                "        \n" +
                "        table tr:nth-child(2n) {\n" +
                "            background-color: rgba(246, 248, 250, 0.7) !important;  // 使用半透明背景色并确保优先级\n" +
                "        }\n" +
                "        \n" +
                "        /* 美化滚动条样式 */\n" +
                "        ::-webkit-scrollbar {\n" +
                "            width: 8px;\n" +
                "            height: 8px;\n" +
                "        }\n" +
                "        \n" +
                "        ::-webkit-scrollbar-track {\n" +
                "            background: rgba(0, 0, 0, 0.05);\n" +
                "            border-radius: 4px;\n" +
                "        }\n" +
                "        \n" +
                "        ::-webkit-scrollbar-thumb {\n" +
                "            background: rgba(0, 0, 0, 0.2);\n" +
                "            border-radius: 4px;\n" +
                "        }\n" +
                "        \n" +
                "        ::-webkit-scrollbar-thumb:hover {\n" +
                "            background: rgba(0, 0, 0, 0.3);\n" +
                "        }\n" +
                "        \n" +
                "        ::-webkit-scrollbar-corner {\n" +
                "            background: rgba(0, 0, 0, 0.05);\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                content + "\n" +
                "</body>\n" +
                "</html>";
    }
    
    @Override
    public boolean supports(File file) {
        return file.getName().toLowerCase().endsWith(".md");
    }
}