package com.thief.reader.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 配置管理器
 */
public class ConfigManager {
    
    private static final String CONFIG_FILE = "app_config.json";
    private static ConfigManager instance;
    private AppConfig config;
    private final Gson gson;
    
    private ConfigManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        loadConfig();
    }
    
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    private void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                config = gson.fromJson(reader, AppConfig.class);
            } catch (IOException e) {
                // 如果读取失败，使用默认配置
                config = new AppConfig();
            }
        } else {
            // 如果配置文件不存在，使用默认配置
            config = new AppConfig();
        }
    }
    
    public void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public AppConfig getConfig() {
        return config;
    }
    
    public void updateConfig(AppConfig newConfig) {
        this.config = newConfig;
        saveConfig();
    }
}