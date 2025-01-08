package searchengine;
import java.util.*;


public class QueryHandler {
    public static final String KEYWORD_OR = "\\s+(?i)or\\s+"; // 匹配 "or" (不区分大小写)
    public static final String KEYWORD_SPACE = " "; // 空格匹配 AND

    /**
     * Handles both OR and AND logic with pagination.
     *
     * @param query       The query string containing AND (space) or OR keywords.
     * @param searchIndex The search index for searching webpages.
     * @param limit       Maximum number of results per page.
     * @param offset      Offset for the current page.
     * @return A list of webpages matching the query.
     */
    public static Map<String, Object> getMatchingWebsitesWithPagination(
        String query, SearchIndex searchIndex, int limit, int offset) {

    // 拆分 OR 子组
    String[] searchTermGroups = query.split(KEYWORD_OR);
    Set<Webpage> combinedResults = new HashSet<>();

    // 处理每个 OR 子组
    for (String searchTerms : searchTermGroups) {
        Set<Webpage> result = processAndLogic(searchTerms.trim(), searchIndex);
        combinedResults.addAll(result); // 合并 OR 的结果
    }

    // 转换为列表并排序
    List<Webpage> allResults = new ArrayList<>(combinedResults);
    allResults.sort((page1, page2) -> Double.compare(page2.getScore(), page1.getScore())); // 按分数降序排序

    // 分页
    int totalResults = allResults.size(); // 总结果数
    int fromIndex = Math.min(offset, totalResults);
    int toIndex = Math.min(offset + limit, totalResults);
    List<Webpage> paginatedResults = allResults.subList(fromIndex, toIndex);

    // 返回分页结果和总数
    Map<String, Object> response = new HashMap<>();
    response.put("totalResults", totalResults); // 总数
    response.put("paginatedResults", paginatedResults); // 当前页的结果

    return response;
}

    /**
     * Processes AND logic: all terms must match.
     *
     * @param searchTerms The terms with AND logic (space-separated).
     * @param searchIndex The search index for searching webpages.
     * @return A set of webpages that match all terms.
     */
    private static Set<Webpage> processAndLogic(String searchTerms, SearchIndex searchIndex) {
        String[] listOfSearchTerm = searchTerms.split(KEYWORD_SPACE);

        Set<Webpage> webpagesContainingSearchTerms = new HashSet<>();

        for (int i = 0; i < listOfSearchTerm.length; i++) {
            String searchTerm = listOfSearchTerm[i];
            Set<Webpage> currentTermResults = searchIndex.searchDocuments(searchTerm, Integer.MAX_VALUE, 0);
            System.out.println("Search term: " + searchTerm + ", Results: " + currentTermResults.size());

            if (i == 0) {
                webpagesContainingSearchTerms.addAll(currentTermResults); // Initialize with first term
            } else {
                webpagesContainingSearchTerms.retainAll(currentTermResults); // Intersect results
            }

            // If intersection is empty, no need to proceed further
            if (webpagesContainingSearchTerms.isEmpty()) {
                break;
            }
        }

        return webpagesContainingSearchTerms;
    }
}