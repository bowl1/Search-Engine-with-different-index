package searchengine;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/**
 * QueryHandler class handles both AND and OR logic in queries.
 */
public class QueryHandler {
    public static final String KEYWORD_OR = "\\s+(?i)or\\s+";  // 匹配 "or" (不区分大小写)
    public static final String KEYWORD_SPACE = " ";  // 空格匹配 AND

    /**
     * Processes the query to handle both OR and AND logic.
     *
     * @param query The query string which may contain AND (space) or OR keywords.
     * @param searchIndex The search index for searching webpages.
     * @return A list of webpages that match the query.
     */
    public static List<Webpage> getMatchingWebsites(String query, SearchIndex searchIndex) {
        // 拆分 OR 子组
        String[] searchTermGroups = query.split(KEYWORD_OR);
        Set<Webpage> combinedSetOfWebsites = new HashSet<>();

        // 处理每个 OR 子组
        for (String searchTerms : searchTermGroups) {
            Set<Webpage> result = getMatchingWebsitesPerSearchTerms(searchTerms.trim(), searchIndex);
            combinedSetOfWebsites.addAll(result);
        }

        return new ArrayList<>(combinedSetOfWebsites);
    }

    /**
     * Processes each AND sub-query group to find common webpages that match all terms.
     *
     * @param searchTerms The sub-query group with AND logic (terms separated by spaces).
     * @param searchIndex The search index for searching webpages.
     * @return A set of webpages that match all the terms in the sub-query.
     */
    private static Set<Webpage> getMatchingWebsitesPerSearchTerms(String searchTerms, SearchIndex searchIndex) {
        String[] listOfSearchTerm = searchTerms.split(KEYWORD_SPACE);

        Set<Webpage> webpagesContainingSearchTerms = null;
        boolean firstResult = true;

        // 处理每个 AND 逻辑的单词
        for (String searchTerm : listOfSearchTerm) {
            // 使用 SearchIndex 的 searchDocuments 方法来搜索网页
            Set<Webpage> webpageSet = searchIndex.searchDocuments(searchTerm);

            // 如果没有结果，返回空集
            if (webpageSet.isEmpty()) {
                return new HashSet<>();
            }

            // 初始化或执行交集操作
            if (firstResult) {
                webpagesContainingSearchTerms = webpageSet;
            } else {
                webpagesContainingSearchTerms.retainAll(webpageSet);
            }

            firstResult = false;
        }

        return webpagesContainingSearchTerms != null ? webpagesContainingSearchTerms : new HashSet<>();
    }
}