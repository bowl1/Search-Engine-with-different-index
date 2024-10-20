package searchengine;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class InvertedIndex extends SearchIndex {
    private Map<String, Set<Webpage>> invertedIndex;

    public InvertedIndex(List<Webpage> pages) {
        this.invertedIndex = new HashMap<>();
        buildIndex(pages);
    }

    // 构建倒排索引
    private void buildIndex(List<Webpage> pages) {
        for (Webpage page : pages) {
            for (String word : page.getWords()) {
                invertedIndex.computeIfAbsent(word, k -> new HashSet<>()).add(page);
            }
        }
    }

     // 实现从倒排索引中获取结果的逻辑
     protected Set<Webpage> searchDocuments(String searchTerm) {
        return invertedIndex.getOrDefault(searchTerm, new HashSet<>());
    }
    }
