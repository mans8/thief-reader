package com.thief.reader;

import com.thief.reader.ui.MainController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * 应用程序入口类
 */
public class App extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // 启动应用程序
        new MainController(primaryStage);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}