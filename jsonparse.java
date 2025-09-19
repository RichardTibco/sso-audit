package com.example.jsonanalyzer;

import com.example.jsonanalyzer.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(JsonAnalyzer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        String jsonFilePath = "sample.json";
        String outputMode = "full"; // full: 完整输出, table: 仅表格
        
        if (args.length > 0) {
            jsonFilePath = args[0];
        }
        
        if (args.length > 1) {
            outputMode = args[1];
        }

        JsonAnalyzer analyzer = new JsonAnalyzer();
        analyzer.analyzeJsonFile(jsonFilePath, outputMode);
    }

    public void analyzeJsonFile(String filePath, String outputMode) {
        try {
            logger.info("开始分析JSON文件: {}", filePath);
            
            JsonData jsonData = objectMapper.readValue(new File(filePath), JsonData.class);
            
            if ("table".equals(outputMode)) {
                // 仅输出表格数据
                printTableData(jsonData);
            } else {
                // 完整输出
                printAnalysisResults(jsonData);
            }
            
        } catch (IOException e) {
            logger.error("分析JSON文件时出错: {}", e.getMessage());
            System.err.println("错误: " + e.getMessage());
        }
    }

    public void analyzeJsonFile(String filePath) {
        analyzeJsonFile(filePath, "full");
    }

    private void printAnalysisResults(JsonData jsonData) {
        System.out.println("\n=== JSON文件分析结果 ===");
        System.out.println("页码: " + jsonData.getPageNum());
        System.out.println("数据项数量: " + (jsonData.getData() != null ? jsonData.getData().size() : 0));
        
        // 打印表格数据
        System.out.println("\n=== 数据表格 ===");
        printTableData(jsonData);
        
        if (jsonData.getData() != null) {
            System.out.println("\n--- 详细数据分析 ---");
            
            for (int i = 0; i < jsonData.getData().size(); i++) {
                DataItem item = jsonData.getData().get(i);
                System.out.println("\n数据项 #" + (i + 1) + ":");
                System.out.println("  Cosmic ID: " + item.getCosmicId());
                System.out.println("  创建日期: " + item.getCreatedDate());
                
                analyzeEntities(item.getEntity());
                analyzeBeneficials(item.getBeneficail());
            }
        }
        
        System.out.println("\n=== 统计信息 ===");
        printStatistics(jsonData);
    }

    private void analyzeEntities(List<Entity> entities) {
        if (entities != null) {
            System.out.println("  实体数量: " + entities.size());
            
            for (int j = 0; j < entities.size(); j++) {
                Entity entity = entities.get(j);
                System.out.println("    实体 #" + (j + 1) + ":");
                System.out.println("      实体ID: " + entity.getEntryid());
                
                if (entity.getAccount() != null) {
                    System.out.println("      账户信息:");
                    System.out.println("        账户号: " + entity.getAccount().getAccountNum());
                    System.out.println("        账户名: " + entity.getAccount().getAccountName());
                }
                
                if (entity.getNationalities() != null) {
                    System.out.println("      国籍信息 (" + entity.getNationalities().size() + " 个):");
                    for (int k = 0; k < entity.getNationalities().size(); k++) {
                        Nationality nationality = entity.getNationalities().get(k);
                        System.out.println("        国籍 #" + (k + 1) + ":");
                        System.out.println("          国家: " + nationality.getCountry());
                        System.out.println("          ID号: " + nationality.getIdNumber());
                        System.out.println("          状态: " + nationality.getStatus());
                    }
                }
            }
        }
    }

    private void analyzeBeneficials(List<Beneficial> beneficials) {
        if (beneficials != null) {
            System.out.println("  受益人数量: " + beneficials.size());
            
            for (int j = 0; j < beneficials.size(); j++) {
                Beneficial beneficial = beneficials.get(j);
                System.out.println("    受益人 #" + (j + 1) + ":");
                System.out.println("      金融机构: " + beneficial.getBeneficailFI());
                System.out.println("      账户号: " + beneficial.getBeneficailAccount());
            }
        }
    }

    private void printTableData(JsonData jsonData) {
        if (jsonData.getData() == null || jsonData.getData().isEmpty()) {
            System.out.println("没有数据可供显示");
            return;
        }

        // 表头
        System.out.printf("%-20s | %-10s | %-15s | %-15s | %-15s | %-15s | %-15s%n", 
            "entryid", "country", "idNumber", "accountNum", "accountName", "beneficailFI", "beneficailAccount");
        System.out.println("---------------------+------------+-----------------+-----------------+-----------------+-----------------+-----------------");

        // 数据行
        for (DataItem item : jsonData.getData()) {
            if (item.getEntity() != null) {
                for (Entity entity : item.getEntity()) {
                    String entryid = String.valueOf(entity.getEntryid());
                    String accountNum = entity.getAccount() != null ? String.valueOf(entity.getAccount().getAccountNum()) : "";
                    String accountName = entity.getAccount() != null ? entity.getAccount().getAccountName() : "";
                    
                    // 处理国籍信息
                    if (entity.getNationalities() != null && !entity.getNationalities().isEmpty()) {
                        for (Nationality nationality : entity.getNationalities()) {
                            String country = nationality.getCountry();
                            String idNumber = nationality.getIdNumber();
                            
                            // 处理受益人信息
                            if (item.getBeneficail() != null && !item.getBeneficail().isEmpty()) {
                                for (Beneficial beneficial : item.getBeneficail()) {
                                    String beneficailFI = beneficial.getBeneficailFI();
                                    String beneficailAccount = String.valueOf(beneficial.getBeneficailAccount());
                                    
                                    System.out.printf("%-20s | %-10s | %-15s | %-15s | %-15s | %-15s | %-15s%n",
                                        entryid, country, idNumber, accountNum, accountName, beneficailFI, beneficailAccount);
                                }
                            } else {
                                // 如果没有受益人信息，留空
                                System.out.printf("%-20s | %-10s | %-15s | %-15s | %-15s | %-15s | %-15s%n",
                                    entryid, country, idNumber, accountNum, accountName, "", "");
                            }
                        }
                    } else {
                        // 如果没有国籍信息，留空
                        if (item.getBeneficail() != null && !item.getBeneficail().isEmpty()) {
                            for (Beneficial beneficial : item.getBeneficail()) {
                                String beneficailFI = beneficial.getBeneficailFI();
                                String beneficailAccount = String.valueOf(beneficial.getBeneficailAccount());
                                
                                System.out.printf("%-20s | %-10s | %-15s | %-15s | %-15s | %-15s | %-15s%n",
                                    entryid, "", "", accountNum, accountName, beneficailFI, beneficailAccount);
                            }
                        } else {
                            // 既没有国籍也没有受益人信息
                            System.out.printf("%-20s | %-10s | %-15s | %-15s | %-15s | %-15s | %-15s%n",
                                entryid, "", "", accountNum, accountName, "", "");
                        }
                    }
                }
            }
        }
    }

    private void printStatistics(JsonData jsonData) {
        if (jsonData.getData() != null) {
            int totalEntities = 0;
            int totalNationalities = 0;
            int totalBeneficials = 0;
            
            for (DataItem item : jsonData.getData()) {
                if (item.getEntity() != null) {
                    totalEntities += item.getEntity().size();
                    
                    for (Entity entity : item.getEntity()) {
                        if (entity.getNationalities() != null) {
                            totalNationalities += entity.getNationalities().size();
                        }
                    }
                }
                
                if (item.getBeneficail() != null) {
                    totalBeneficials += item.getBeneficail().size();
                }
            }
            
            System.out.println("总实体数: " + totalEntities);
            System.out.println("总国籍数: " + totalNationalities);
            System.out.println("总受益人数: " + totalBeneficials);
            System.out.println("平均每个数据项的实体数: " + String.format("%.2f", (double) totalEntities / jsonData.getData().size()));
            System.out.println("平均每个实体的国籍数: " + (totalEntities > 0 ? String.format("%.2f", (double) totalNationalities / totalEntities) : "0"));
        }
    }
}
