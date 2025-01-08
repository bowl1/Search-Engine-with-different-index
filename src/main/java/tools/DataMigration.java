package tools;

import searchengine.SQLiteConnection;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

public class DataMigration {
    public static void main(String[] args) {
        System.out.println("DataMigration started...");
        String filePath = "data/enwiki-medium.txt";

        try (Connection connection = SQLiteConnection.getConnection()) {
            System.out.println("Database connection established.");

            // 清空现有数据（可选）
            clearDatabase(connection);
            System.out.println("Database cleared.");

            // 迁移数据
            migrateData(filePath, connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("DataMigration completed.");
    }

    private static void migrateData(String filePath, Connection connection) throws Exception {
        System.out.println("Starting data migration...");

        // 定义 SQL 语句
        String insertWebpage = "INSERT INTO Webpages (url, title, content) VALUES (?, ?, ?)";
        String insertForwardIndex = "INSERT INTO ForwardIndex (page_id, word, frequency) VALUES (?, ?, ?)";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String url = null;
            String title = null;
            StringBuilder contentBuilder = new StringBuilder();
            int pageCount = 0; // 已处理的页面数量
            int lineCount = 0; // 已处理的行数

            while ((line = reader.readLine()) != null) {
                lineCount++;

                // 每 10,000 行打印一次日志
                if (lineCount % 10000 == 0) {
                    System.out.println("Processed lines: " + lineCount);
                }

                if (line.startsWith("*PAGE:")) {
                    // 遇到新页面，保存当前页面的数据
                    if (url != null) {
                        savePage(connection, insertWebpage, insertForwardIndex, url, title, contentBuilder.toString());
                        contentBuilder.setLength(0); // 清空内容
                        pageCount++;
                    }
                    // 解析新页面的 URL
                    url = line.substring(6).trim();
                    title = null; // 重置标题
                } else if (url != null && title == null) {
                    // 第一行内容为标题
                    title = line.trim();
                } else if (!line.isEmpty()) {
                    // 累积网页内容
                    contentBuilder.append(line).append(" ");
                }
            }

            // 保存最后一页数据
            if (url != null) {
                savePage(connection, insertWebpage, insertForwardIndex, url, title, contentBuilder.toString());
                pageCount++;
            }

            System.out.println("Total lines processed: " + lineCount);
            System.out.println("Total pages processed: " + pageCount);
        }
    }

    private static void savePage(Connection connection, String insertWebpage, String insertForwardIndex,
            String url, String title, String content) throws Exception {
        // 插入 Webpages 表
        try (PreparedStatement webpageStmt = connection.prepareStatement(insertWebpage,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            webpageStmt.setString(1, url);
            webpageStmt.setString(2, title);
            webpageStmt.setString(3, content);
            webpageStmt.executeUpdate();

            // 获取生成的页面 ID
            var generatedKeys = webpageStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int pageId = generatedKeys.getInt(1);

                // 解析内容并生成词频
                Map<String, Integer> wordFrequency = countWords(content);

                // 插入 ForwardIndex 和 InvertedIndex 表
                String insertInvertedIndex = "INSERT INTO InvertedIndex (word, page_id) VALUES (?, ?)";
                try (PreparedStatement forwardStmt = connection.prepareStatement(insertForwardIndex);
                        PreparedStatement invertedStmt = connection.prepareStatement(insertInvertedIndex)) {

                    for (var entry : wordFrequency.entrySet()) {
                        // 插入 ForwardIndex
                        forwardStmt.setInt(1, pageId);
                        forwardStmt.setString(2, entry.getKey());
                        forwardStmt.setInt(3, entry.getValue());
                        forwardStmt.addBatch();

                        // 插入 InvertedIndex
                        invertedStmt.setString(1, entry.getKey());
                        invertedStmt.setInt(2, pageId);
                        invertedStmt.addBatch();
                    }

                    // 执行批量插入
                    forwardStmt.executeBatch();
                    invertedStmt.executeBatch();

                    System.out.println("Saved page: " + title);
                    System.out.println("ForwardIndex and InvertedIndex updated for page: " + title);
                }
            }
        }
    }

    private static Map<String, Integer> countWords(String content) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        String[] words = content.split("\\s+");
        for (String word : words) {
            word = word.toLowerCase().replaceAll("[^a-zA-Z0-9]", ""); // 清理标点符号
            if (!word.isEmpty()) {
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
        return wordFrequency;
    }

    private static void clearDatabase(Connection connection) throws Exception {
        try (PreparedStatement stmt1 = connection.prepareStatement("DELETE FROM Webpages");
                PreparedStatement stmt2 = connection.prepareStatement("DELETE FROM ForwardIndex")) {
            stmt1.executeUpdate();
            stmt2.executeUpdate();
        }
    }
}
