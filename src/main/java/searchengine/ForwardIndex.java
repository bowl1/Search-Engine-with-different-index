package searchengine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ForwardIndex extends SearchIndex {

    private Connection connection;

    public ForwardIndex(Connection connection) {
        super(connection); // 调用父类构造函数
        this.connection = connection; // 初始化子类的 connection
    }

    @Override
    protected Set<Webpage> searchDocuments(String searchTerm, int limit, int offset) {
        Set<Webpage> result = new HashSet<>();
        String query = """
            SELECT w.id, w.url, w.title, w.content
            FROM ForwardIndex f
            JOIN Webpages w ON f.page_id = w.id
            WHERE f.word = ?
            LIMIT ? OFFSET ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, searchTerm);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Webpage page = new Webpage(
                    rs.getInt("id"),
                    rs.getString("url"),
                    rs.getString("title"),
                    rs.getString("content")
                );
                result.add(page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

   @Override
public int getTotalResults(String searchTerms) {
    // 调用 getMatchingWebsitesWithPagination，获取所有匹配的结果
    Map<String, Object> response = QueryHandler.getMatchingWebsitesWithPagination(searchTerms, this, Integer.MAX_VALUE, 0);

    // 从返回的结果中提取总数
    return (int) response.get("totalResults");
}
    
}