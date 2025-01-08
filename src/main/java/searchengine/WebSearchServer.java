package searchengine;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

public class WebSearchServer extends WebServer {

    private SearchIndex searchIndex;
    private ScoringQuery scoringQuery;
    private final Connection connection;

    public WebSearchServer(final int port) throws IOException {
        super(port);

        // 获取数据库连接
        this.connection = SQLiteConnection.getConnection();

        // 默认使用 InvertedIndex
        searchIndex = new InvertedIndex(connection);
        scoringQuery = new ScoringQuery();
        searchIndex.setScoringQuery(scoringQuery);

        initializeServer(port);
        startServer(port);
    }

    private void initializeServer(final int port) throws IOException {
        server.createContext("/", io -> respond(io, 200, "text/html", getFile("web/index.html")));
        server.createContext("/search", io -> {
            try {
                search(io); // 调用 search 方法
            } catch (Exception e) {
                e.printStackTrace();
                respond(io, 500, "text/plain", "Internal Server Error".getBytes(CHARSET));
            }
        });
        server.createContext("/favicon.ico", io -> respond(io, 200, "image/x-icon", getFile("web/favicon.ico")));
        server.createContext("/code.js", io -> respond(io, 200, "application/javascript", getFile("web/code.js")));
        server.createContext("/style.css", io -> respond(io, 200, "text/css", getFile("web/style.css")));

        server.createContext("/forwardIndexData", io -> {
            try {
                byte[] data = Files.readAllBytes(Paths.get("src/jmh/java/searchengine/jmhdata.txt"));
                String content = new String(data, StandardCharsets.UTF_8);
                String forwardIndexData = content.split("===SECTION===")[0];
                respond(io, 200, "text/plain", forwardIndexData.getBytes(CHARSET));
            } catch (Exception e) {
                e.printStackTrace();
                respond(io, 500, "text/plain", "Failed to load Forward Index data".getBytes(CHARSET));
            }
        });

        server.createContext("/invertedIndexData", io -> {
            try {
                byte[] data = Files.readAllBytes(Paths.get("src/jmh/java/searchengine/jmhdata.txt"));
                String content = new String(data, StandardCharsets.UTF_8);
                String invertedIndexData = content.split("===SECTION===")[1];
                respond(io, 200, "text/plain", invertedIndexData.getBytes(CHARSET));
            } catch (Exception e) {
                e.printStackTrace();
                respond(io, 500, "text/plain", "Failed to load Inverted Index data".getBytes(CHARSET));
            }
        });
    }

    private void search(HttpExchange io) {
        try {
            // 解析查询参数
            String[] queryParams = io.getRequestURI().getQuery().split("&");
            String searchTerms = queryParams[0].split("=")[1];
            String indexType = queryParams[1].split("=")[1];
            int limit = Integer.parseInt(queryParams[2].split("=")[1]);
            int offset = Integer.parseInt(queryParams[3].split("=")[1]);

            System.out.println("Search Terms: " + searchTerms);
            System.out.println("Index Type: " + indexType);

            // 根据 indexType 切换索引类型
            if (indexType.equals("forward")) {
                System.out.println("Using Forward Index");
                searchIndex = new ForwardIndex(connection);
            } else {
                System.out.println("Using Inverted Index");
                searchIndex = new InvertedIndex(connection);
            }

            // 获取总结果数
            int totalResults = searchIndex.getTotalResults(searchTerms);

            // 获取当前页的结果
            List<Webpage> sortedResults = searchIndex.search(searchTerms, limit, offset);

            // 构建 JSON 响应
            var response = new StringBuilder("{");
            response.append("\"totalResults\":").append(totalResults).append(",");
            response.append("\"currentPage\":").append(offset / limit + 1).append(",");
            response.append("\"results\":[");
           
            for (Webpage page : sortedResults) {
                // 获取 URL 并确保其完整性
                String url = page.getUrl();
                if (!url.startsWith("http")) {
                    // 假设链接是 Wikipedia 的相对路径
                    url = "https://en.wikipedia.org/wiki/" + url.trim().replace(" ", "_");
                }
            
                try {
                    // 验证并标准化 URL
                    URI uri = new URI(url);
                    url = uri.toASCIIString(); // 转换为标准 ASCII 格式
                } catch (Exception e) {
                    e.printStackTrace();
                    continue; // 跳过无效 URL
                }
            
                // 构建响应
                response.append(String.format(
                        "{\"url\":\"%s\",\"title\":\"%s\",\"score\":%.2f},",
                        url,
                        page.getTitle().replace("\"", "\\\""), // 转义标题中的双引号
                        page.getScore()
                ));
            }

            if (!sortedResults.isEmpty()) {
                response.deleteCharAt(response.length() - 1);
            }
            response.append("]}");

            respond(io, 200, "application/json", response.toString().getBytes(CHARSET));
        } catch (Exception e) {
            e.printStackTrace();
            respond(io, 500, "text/plain", "Internal Server Error".getBytes(CHARSET));
        }
    }

    public SearchIndex getSearchIndex() {
        return searchIndex;
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("[WebSearchServer] Server stopped.");
        }
    }
}