package searchengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InvertedIndexTest {

    private InvertedIndex index;
    private ScoringQuery scoringQuery;
    private Connection connection;

    @BeforeAll
    void setUp() throws Exception {
        connection = SQLiteConnection.getConnection();
        prepareTestData(connection);
        scoringQuery = new ScoringQuery();
        index = new InvertedIndex(connection);
        index.setScoringQuery(scoringQuery);
    }

    @AfterAll
    void tearDown() throws Exception {
        cleanTestData(connection);
        connection.close();
        index = null;
    }

    @Test
    void testSearchEmpty() {
        List<Webpage> emptyList = new ArrayList<>();
        final List<Webpage> wordResult = index.search("nonexistent", 30, 0);
        assertEquals(emptyList, wordResult);
    }

    @Test
    void testSearch() {
        List<Webpage> expectedList = new ArrayList<>();
        expectedList.add(new Webpage(1, "Test Page 1", "http://test1.com", "content1 content2"));
        final List<Webpage> actualResult = index.search("content1", 30, 0);
        assertNotEquals(0, actualResult.size());
        assertEquals(expectedList.get(0).getTitle(), actualResult.get(0).getTitle());
        assertEquals(expectedList.get(0).getUrl(), actualResult.get(0).getUrl());
    }

    private void prepareTestData(Connection connection) throws Exception {
        String createWebpagesTable = """
            CREATE TABLE IF NOT EXISTS Webpages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                url TEXT NOT NULL,
                content TEXT
            );
        """;
        String createInvertedIndexTable = """
            CREATE TABLE IF NOT EXISTS InvertedIndex (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                word TEXT NOT NULL,
                page_id INTEGER NOT NULL,
                FOREIGN KEY (page_id) REFERENCES Webpages(id)
            );
        """;
        String insertWebpages = """
            INSERT INTO Webpages (id, title, url, content) VALUES
            (1, 'Test Page 1', 'http://test1.com', 'content1 content2'),
            (2, 'Test Page 2', 'http://test2.com', 'content3 content4');
        """;
        String insertInvertedIndex = """
            INSERT INTO InvertedIndex (word, page_id) VALUES
            ('content1', 1),
            ('content2', 1),
            ('content3', 2),
            ('content4', 2);
        """;

        try (PreparedStatement stmt1 = connection.prepareStatement(createWebpagesTable);
             PreparedStatement stmt2 = connection.prepareStatement(createInvertedIndexTable);
             PreparedStatement stmt3 = connection.prepareStatement(insertWebpages);
             PreparedStatement stmt4 = connection.prepareStatement(insertInvertedIndex)) {

            stmt1.executeUpdate();
            stmt2.executeUpdate();
            stmt3.executeUpdate();
            stmt4.executeUpdate();
        }
    }

    private void cleanTestData(Connection connection) throws Exception {
        String deleteWebpages = "DROP TABLE IF EXISTS Webpages;";
        String deleteInvertedIndex = "DROP TABLE IF EXISTS InvertedIndex;";

        try (PreparedStatement stmt1 = connection.prepareStatement(deleteWebpages);
             PreparedStatement stmt2 = connection.prepareStatement(deleteInvertedIndex)) {

            stmt1.executeUpdate();
            stmt2.executeUpdate();
        }
    }
}