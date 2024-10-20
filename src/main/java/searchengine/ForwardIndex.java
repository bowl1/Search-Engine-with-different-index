package searchengine;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ForwardIndex extends SearchIndex {

    private List<Webpage> pages;

    public ForwardIndex(List<Webpage> pages) {
        this.pages = pages;
    }

    @Override
    protected Set<Webpage> searchDocuments(String searchTerm) {
        Set<Webpage> result = new HashSet<>();
        for (Webpage page : pages) {
            if (page.getWords().contains(searchTerm)) {
                result.add(page);
            }
        }
        return result;
    }
}