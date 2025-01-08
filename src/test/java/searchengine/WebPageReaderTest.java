package searchengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebpageReaderTest {

    private Connection connection;

    @BeforeAll
    void setUp() throws Exception {
        // 初始化数据库连接
        connection = SQLiteConnection.getConnection();
        prepareTestData(connection); // 插入测试数据
    }

    @AfterAll
    void tearDown() throws Exception {
        cleanTestData(connection); // 清理测试数据
        connection.close();
    }

    @Test
    void testFetchWebpages() {
        try {
            // 从数据库加载网页数据
            List<Webpage> pages = WebpageReader.fetchWebpages(connection);

            // 验证返回的网页列表
            assertEquals(2, pages.size(), "Expected 2 webpages in the database");

            // 验证第一条网页数据
            Webpage page1 = pages.get(0);
            assertEquals("Test Page 1", page1.getTitle(), "Page 1 title mismatch");
            assertEquals("http://test1.com", page1.getUrl(), "Page 1 URL mismatch");
            assertNotNull(page1.getWords(), "Page 1 words should not be null");

            // 验证第二条网页数据
            Webpage page2 = pages.get(1);
            assertEquals("Test Page 2", page2.getTitle(), "Page 2 title mismatch");
            assertEquals("http://test2.com", page2.getUrl(), "Page 2 URL mismatch");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to prepare test data
    private void prepareTestData(Connection connection) throws Exception {
        String createWebpagesTable = """
            CREATE TABLE IF NOT EXISTS Webpages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                url TEXT NOT NULL,
                content TEXT
            );
        """;

        String insertWebpages = """
            INSERT INTO Webpages (id, title, url, content) VALUES
            (1, 'Test Page 1', 'http://test1.com', 'word1 word2 word3'),
            (2, 'Test Page 2', 'http://test2.com', 'word4 word5 word6');
        """;

        try (PreparedStatement stmt1 = connection.prepareStatement(createWebpagesTable);
             PreparedStatement stmt2 = connection.prepareStatement(insertWebpages)) {
            stmt1.executeUpdate();
            stmt2.executeUpdate();
        }
    }

    // Helper method to clean up test data
    private void cleanTestData(Connection connection) throws Exception {
        String dropWebpagesTable = "DROP TABLE IF EXISTS Webpages;";
        try (PreparedStatement stmt = connection.prepareStatement(dropWebpagesTable)) {
            stmt.executeUpdate();
        }
    }
}