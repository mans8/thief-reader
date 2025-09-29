# Thief Java Reader

一个轻量级的文档阅读器，支持多种文档格式（Markdown、TXT、PDF）和两种使用模式（正常模式和极简模式）。

## 功能特性

### 支持的文档格式
- Markdown (.md)
- 纯文本 (.txt)
- PDF (.pdf)

### 两种使用模式
1. **正常模式**：完整的功能界面，包含文件浏览、最近打开列表等
2. **极简模式**：专注阅读模式，无干扰界面

### 特色功能
- 双模式切换（ESC键可在极简模式下返回正常模式）
- 极简模式窗口支持：
  - 通过拖动上边框移动位置
  - 通过左、右、下边框调整窗口大小
- 最近打开文件历史记录
- 支持JDK 8环境运行
- 美化的Markdown渲染样式

## 系统要求

- Java 8 或更高版本
- Windows 7 或更高版本

## 安装和运行

### 方法一：使用预编译的JAR包
```bash
java -jar thief-java-reader-1.0.0.jar
```

### 方法二：从源码构建
```bash
# 克隆项目
git clone <repository-url>

# 进入项目目录
cd thief-java-reader

# 构建项目
mvn clean package

# 运行应用
java -jar target/thief-java-reader-1.0.0.jar
```

## 使用说明

1. **打开文件**：点击"打开文件"按钮或拖拽文件到窗口
2. **切换模式**：
   - 从正常模式切换到极简模式：点击工具栏的"极简模式"按钮
   - 从极简模式返回正常模式：按ESC键
3. **极简模式操作**：
   - 拖动上边框：移动窗口位置
   - 拖动左、右、下边框：调整窗口大小
4. **查看最近文件**：在正常模式下，左侧会显示最近打开的文件列表

## 配置文件

应用会在用户目录下创建配置文件 `thief-reader-config.json`，用于保存用户偏好设置：
- 默认启动模式
- 窗口位置和大小
- 最近打开的文件列表

## 技术架构

- **核心框架**：JavaFX 8
- **构建工具**：Maven
- **文档解析**：
  - Markdown: flexmark-java
  - PDF: Apache PDFBox
- **配置管理**：Gson (JSON处理)

## Markdown渲染改进

为了提升用户体验，我们对Markdown文档的渲染进行了以下改进：

1. **添加了现代化的CSS样式**：
   - 采用GitHub风格的样式设计
   - 优化了字体、行高和颜色搭配
   - 改进了代码块、表格、列表等元素的显示效果

2. **支持完整的Markdown语法**：
   - 标题、段落、列表
   - 链接、图片、引用
   - 代码块、行内代码
   - 表格、分隔线
   - 文本强调（粗体、斜体）

3. **响应式设计**：
   - 内容居中显示，最大宽度限制
   - 适合阅读的字体大小和行间距
   - 良好的可读性

## 开发说明

### 项目结构
```
src/
├── main/
│   ├── java/
│   │   └── com/thief/reader/
│   │       ├── App.java              # 应用入口
│   │       ├── core/                 # 核心模块
│   │       │   └── DocumentEngine.java # 文档解析引擎
│   │       ├── ui/                   # 用户界面
│   │       │   ├── MainController.java    # 主控制器
│   │       │   ├── NormalModeController.java # 正常模式控制器
│   │       │   └── MinimalModeController.java # 极简模式控制器
│   │       ├── config/               # 配置管理
│   │       │   └── ConfigManager.java
│   │       └── util/                 # 工具类
│   │           └── FileUtils.java
│   └── resources/
└── test/                             # 测试代码
```

## 常见问题

### 1. 启动时报错"JavaFX runtime components are missing"
确保使用包含JavaFX的JDK版本，或单独安装JavaFX运行时。

### 2. PDF文件显示乱码
检查PDF文件是否加密或使用特殊字体，部分复杂PDF可能无法完美渲染。

## 版本历史

### v1.0.0
- 初始版本发布
- 支持三种文档格式
- 实现双模式切换
- 添加配置管理功能
- 改进Markdown渲染样式

## 许可证

MIT License