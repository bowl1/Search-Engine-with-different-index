package searchengine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.BindException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebServerTest {

    private WebServer server;
    private Connection connection;
    private int port;

    @BeforeAll
    void setUp() throws Exception {
        // 初始化数据库连接
        connection = SQLiteConnection.getConnection();
        prepareTestData(connection);

        // 启动 Web 服务器
        var rnd = new Random();
        int maxAttempts = 10; // 限制端口尝试次数
        int attempts = 0;

        while (server == null && attempts < maxAttempts) {
            try {
                port = rnd.nextInt(60000) + 1024;
                server = new WebSearchServer(port);
            } catch (BindException e) {
                attempts++;
            }
        }

        if (server == null) {
            throw new RuntimeException("Failed to bind to a port after multiple attempts.");
        }
    }

    @AfterAll
    void tearDown() throws Exception {
        if (server != null && server.server != null) {
            server.server.stop(0);
        }
        cleanTestData(connection);
        connection.close();
    }

    @Test
    void lookupWebServer() {
        String baseURL = String.format("http://localhost:%d/search?q=", port);

        // Test for "word1"
        String expectedJsonWord1 = "[{\"url\":\"http://page1.com\",\"title\":\"title1\",\"score\":5.00},"
                + "{\"url\":\"http://page2.com\",\"title\":\"title2\",\"score\":5.00}]";
        String actualJsonWord1 = httpGet(baseURL + "word1&index=forward");
        assertEquals(expectedJsonWord1, actualJsonWord1, "Failed for query 'word1'");

        // Test for "word2"
        assertEquals("[{\"url\":\"http://page1.com\",\"title\":\"title1\",\"score\":5.00}]",
                httpGet(baseURL + "word2&index=forward"), "Failed for query 'word2'");

        // Test for "word3"
        assertEquals("[{\"url\":\"http://page2.com\",\"title\":\"title2\",\"score\":5.00}]",
                httpGet(baseURL + "word3&index=forward"), "Failed for query 'word3'");

        // Test for "word4"
        assertEquals("[]", httpGet(baseURL + "word4&index=forward"), "Failed for query 'word4'");
    }

    @Test
    void lookupWebServer2() {
        String baseURL = String.format("http://localhost:%d/search?q=", port);
        assertEquals("[]", httpGet(baseURL + "bowen&index=forward"), "Failed for query 'bowen'");
    }

    private String httpGet(String url) {
        var uri = URI.create(url);
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder().uri(uri).GET().build();
        try {
            return client.send(request, BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 插入测试数据
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
            (1, 'title1', 'http://page1.com', 'word1 word2'),
            (2, 'title2', 'http://page2.com', 'word1 word3');
        """;

        String createForwardIndexTable = """
            CREATE TABLE IF NOT EXISTS ForwardIndex (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                page_id INTEGER NOT NULL,
                word TEXT NOT NULL,
                frequency INTEGER NOT NULL,
                FOREIGN KEY (page_id) REFERENCES Webpages(id)
            );
        """;

        String insertForwardIndex = """
            INSERT INTO ForwardIndex (page_id, word, frequency) VALUES
            (1, 'word1', 1),
            (1, 'word2', 1),
            (2, 'word1', 1),
            (2, 'word3', 1);
        """;

        try (PreparedStatement stmt1 = connection.prepareStatement(createWebpagesTable);
             PreparedStatement stmt2 = connection.prepareStatement(insertWebpages);
             PreparedStatement stmt3 = connection.prepareStatement(createForwardIndexTable);
             PreparedStatement stmt4 = connection.prepareStatement(insertForwardIndex)) {

            stmt1.executeUpdate();
            stmt2.executeUpdate();
            stmt3.executeUpdate();
            stmt4.executeUpdate();
        }
    }

    // 清理测试数据
    private void cleanTestData(Connection connection) throws Exception {
        String dropWebpagesTable = "DROP TABLE IF EXISTS Webpages;";
        String dropForwardIndexTable = "DROP TABLE IF EXISTS ForwardIndex;";
        try (PreparedStatement stmt1 = connection.prepareStatement(dropWebpagesTable);
             PreparedStatement stmt2 = connection.prepareStatement(dropForwardIndexTable)) {
            stmt1.executeUpdate();
            stmt2.executeUpdate();
        }
    }
}