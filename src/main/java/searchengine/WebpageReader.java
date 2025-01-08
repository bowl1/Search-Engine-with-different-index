package searchengine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to read webpages from the database.
 */
public class WebpageReader {

    /**
     * Fetch all webpages from the database.
     * 
     * @param connection Database connection
     * @return List of Webpage objects
     * @throws Exception if an error occurs during database operations
     */
    public static List<Webpage> fetchWebpages(Connection connection) throws Exception {
        List<Webpage> pages = new ArrayList<>();

        // SQL query to fetch all webpage data
        String query = "SELECT id, url, title, content FROM Webpages";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String url = rs.getString("url");
                String title = rs.getString("title");
                String content = rs.getString("content");

                // Convert content to words and count frequencies
                List<String> words = List.of(content.split("\\s+"));
                Map<String, Integer> mapOfSearchCount = countSearchTerms(words);

                // Create Webpage object
                Webpage webpage = new Webpage(id, title, url, content);
                webpage.getWords().addAll(words);
                webpage.setSearchTermCount(mapOfSearchCount);

                pages.add(webpage);
            }
        }
        return pages;
    }

    /**
     * Count the frequency of words in the list.
     * 
     * @param words List of words
     * @return Map with word frequencies
     */
    public static Map<String, Integer> countSearchTerms(List<String> words) {
        Map<String, Integer> searchtermFrequency = new HashMap<>();

        for (String word : words) {
            searchtermFrequency.put(word, searchtermFrequency.getOrDefault(word, 0) + 1);
        }
        return searchtermFrequency;
    }
}