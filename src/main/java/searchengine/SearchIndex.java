package searchengine;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

public abstract class SearchIndex {
    protected ScoringQuery scoringQuery;

    public SearchIndex() {
        this.scoringQuery = new ScoringQuery(); // 初始化 ScoringQuery
    }
    // 设置评分查询对象
    public void setScoringQuery(ScoringQuery scoringQuery) {
        this.scoringQuery = scoringQuery;
    }

    // 子类必须实现的搜索逻辑
    protected abstract Set<Webpage> searchDocuments(String searchTerm);

    // 通用搜索逻辑：处理评分和排序
    public List<Webpage> search(String query) {

        // 通过 QueryHandler 来获取匹配的网页
        List<Webpage> result = QueryHandler.getMatchingWebsites(query, this);  

        // 如果没有结果，返回空列表
        if (result.isEmpty()) {
            return new ArrayList<>();
        }

        // 将 Set 转换为 List 以便排序
        List<Webpage> sortedResults = new ArrayList<>(result);

        // 计算每个网页的得分
        for (Webpage page : sortedResults) {
            // 计算每个页面的搜索词频
            String[] searchTerms = query.split("\\s+");
            double totalScore = 0;
            for (String searchTerm : searchTerms) {
                Integer wordCount = page.getSearchTermCount().get(searchTerm);
                if (wordCount == null) {
                    wordCount = 0;  // 处理未找到的情况
                }

                // 获取文档长度
                int documentLength = page.getDocumentLength();
    
                // 获取文档总数
                int documentCount = sortedResults.size(); // 文档总数

                // 获取包含搜索词的文档数
                int numberOfDocumentsContainingTheTerm = getDocumentCountContainingTerm(searchTerm);

                // 计算 TF 和 IDF 分数
                double scoreTF = scoringQuery.calculateScoreTF(wordCount, documentLength);
                double scoreIDF = scoringQuery.calculateScoreIDF(documentCount, numberOfDocumentsContainingTheTerm);
                double score = scoringQuery.calculateScoreTFIDF(wordCount, documentLength, documentCount, numberOfDocumentsContainingTheTerm);
                totalScore += score;
            }

            // 设置网页的总得分
            page.setScore(totalScore);
        }
        
        // 按照 score 降序排序
        Collections.sort(sortedResults, (page1, page2) -> Double.compare(page2.getScore(), page1.getScore()));
        return sortedResults;
    }

    // 获取包含查询词的文档数量
    public int getDocumentCountContainingTerm(String searchTerm) {
        Set<Webpage> webpagesContainingTerm = searchDocuments(searchTerm);
        return webpagesContainingTerm.size();
    }
}