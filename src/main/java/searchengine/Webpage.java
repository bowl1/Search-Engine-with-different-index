package searchengine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Webpage {

    private int id; // 数据库中的主键
    private String title;
    private String url;
    private String content; // 网页的完整内容
    private Map<String, Integer> mapOfSearchCount; // 关键词频率
    private double score;

    // 构造函数：用于从数据库结果初始化 Webpage 对象
    public Webpage(int id, String title, String url, String content) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.content = content;
        this.mapOfSearchCount = new HashMap<>();
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Integer> getSearchTermCount() {
        return this.mapOfSearchCount;
    }

    public void setSearchTermCount(Map<String, Integer> mapOfSearchCount) {
        this.mapOfSearchCount = mapOfSearchCount;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    // 获取文档长度（以单词计数）
    public int getDocumentLength() {
        return content != null ? content.split("\\s+").length : 0;
    }

    // 解析内容并生成单词列表
    public List<String> getWords() {
        return List.of(content.split("\\s+"));
    }

    //用于处理and逻辑
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true; // 同一个对象
        if (o == null || getClass() != o.getClass())
            return false; // 类不同则返回 false
        Webpage webpage = (Webpage) o;
        return id == webpage.id; // 根据 id 判断是否相同
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id); // 基于 id 生成哈希值
    }
}