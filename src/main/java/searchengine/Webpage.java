package searchengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Webpage {

    private String title;
    private String url;
    private List<String> words;
    private Map<String, Integer> mapOfSearchCount;
    private double score;

    public Webpage(String title, String url) {
        this.title = title;
        this.url = url;
        words = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public Map<String, Integer> getSearchTermCount() {
        return this.mapOfSearchCount;
    }

    public void setSearchTermCount(Map<String, Integer> mapOfSearchCount) {
        this.mapOfSearchCount = mapOfSearchCount;
    }

    public int getDocumentLength() {
        return words.size();
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

}