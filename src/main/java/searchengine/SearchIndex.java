package searchengine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public abstract class SearchIndex {
    protected ScoringQuery scoringQuery;
    protected Connection connection; // 数据库连接

    public SearchIndex(Connection connection) {
        this.connection = connection;
        this.scoringQuery = new ScoringQuery(); // 初始化 ScoringQuery
    }

    // 设置评分查询对象
    public void setScoringQuery(ScoringQuery scoringQuery) {
        this.scoringQuery = scoringQuery;
    }

    // 子类必须实现的搜索逻辑
    protected abstract Set<Webpage> searchDocuments(String searchTerm, int limit, int offset);

    // 获取总结果数
    public abstract int getTotalResults(String searchTerm);

    // 通用搜索逻辑：处理评分和分页
    public List<Webpage> search(String query, int limit, int offset) {
        Map<String, Object> resultsWithTotal = QueryHandler.getMatchingWebsitesWithPagination(query, this, limit, offset);
    
        // 获取分页后的结果
        List<Webpage> sortedResults = (List<Webpage>) resultsWithTotal.get("paginatedResults");
    
        // 如果没有结果，返回空列表
        if (sortedResults.isEmpty()) {
            return new ArrayList<>();
        }
    
        // 获取总数（可用于前端分页显示）
        int totalResults = (int) resultsWithTotal.get("totalResults");
        System.out.println("Total results for query: " + query + " = " + totalResults);
    
        // 计算每个网页的得分
        for (Webpage page : sortedResults) {
            double totalScore = 0;
    
            String[] searchTerms = query.toLowerCase().split("\\s+");
    
            for (String searchTerm : searchTerms) {
                String lowerCaseSearchTerm = searchTerm.toLowerCase();
    
                // 从数据库查询 wordCount 和 documentLength
                int wordCount = getWordCount(page.getId(), lowerCaseSearchTerm);
                int documentLength = getDocumentLength(page.getId());
    
                // 获取文档总数和包含搜索词的文档数
                int documentCount = getTotalDocumentCount();
                int numberOfDocumentsContainingTheTerm = getDocumentCountContainingTerm(lowerCaseSearchTerm);
    
                // 计算 TF 和 IDF 分数
                double score = scoringQuery.calculateScoreTFIDF(
                    wordCount, documentLength, documentCount, numberOfDocumentsContainingTheTerm
                );
                totalScore += score;
            }
    
            // 设置网页的总得分
            page.setScore(totalScore);
        }
    
        // 按分数降序排序
        sortedResults.sort((page1, page2) -> Double.compare(page2.getScore(), page1.getScore()));
    
        return sortedResults;
    }
    // 获取包含查询词的文档数量
    public int getDocumentCountContainingTerm(String searchTerm) {
        String query = "SELECT COUNT(DISTINCT page_id) FROM InvertedIndex WHERE word = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, searchTerm);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 获取文档总数
    private int getTotalDocumentCount() {
        String query = "SELECT COUNT(*) FROM Webpages";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 获取某文档的单词总数
    private int getDocumentLength(int pageId) {
        String query = "SELECT LENGTH(content) - LENGTH(REPLACE(content, ' ', '')) + 1 AS documentLength FROM Webpages WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, pageId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("documentLength");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 获取某文档中查询词的频率
    private int getWordCount(int pageId, String searchTerm) {
        String query = "SELECT frequency FROM ForwardIndex WHERE page_id = ? AND word = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, pageId);
            stmt.setString(2, searchTerm);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("frequency");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}