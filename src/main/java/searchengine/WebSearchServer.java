package searchengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;

public class WebSearchServer extends WebServer {

  private final List<Webpage> pages;
  private SearchIndex searchIndex;
  private ScoringQuery scoringQuery;

  public WebSearchServer(final int port, final String filename) throws IOException {
    super(port);

    pages = WebpageReader.fetchWebpages(filename);

    searchIndex = new ForwardIndex(pages);
    searchIndex = new InvertedIndex(pages);
    scoringQuery = new ScoringQuery();
    searchIndex.setScoringQuery(scoringQuery);

    initializeServer(port);
    startServer(port);
  }

  /**
   * initializes the web server and adds handling of the respective contexts.
   * 
   * @param port
   * @throws IOException
   */
  private void initializeServer(final int port) throws IOException {
    server.createContext("/", io -> respond(io, 200, "text/html", getFile("web/index.html")));
    server.createContext("/search", io -> {
      try {
        search(io); // 调用现有的 search 方法来处理搜索请求
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Error handling search request: " + e.getMessage());
        respond(io, 500, "text/plain", "Internal Server Error".getBytes(CHARSET));
      }
    });
    server.createContext("/favicon.ico", io -> respond(io, 200, "image/x-icon", getFile("web/favicon.ico")));
    server.createContext("/code.js", io -> respond(io, 200, "application/javascript", getFile("web/code.js")));
    server.createContext("/style.css", io -> respond(io, 200, "text/css", getFile("web/style.css")));

    server.createContext("/forwardIndexData", io -> getSelectedLines(io, 1, 4));
    server.createContext("/invertedIndexData", io -> getSelectedLines(io, 6, 9));
  }

  /**
   * Helper method to call the search index to search for the search term. Builds
   * a JSON object containing a list of the webpages (url, title). Responds with a
   * JSON object as a byte array.
   * 
   * @param io
   */
  private void search(final HttpExchange io) {
    // 获取查询词和索引类型（例如 localhost:8080/search?q=<searchTerm>&index=forward）
   try{
    String[] queryParams = io.getRequestURI().getQuery().split("&");
    String searchTerms = queryParams[0].split("=")[1];
    String indexType = queryParams[1].split("=")[1];

    System.out.println("Search Terms: " + searchTerms);
    System.out.println("Index Type: " + indexType);

    // 根据 indexType 切换索引类型
    if (indexType.equals("forward")) {
      System.out.println("Using Forward Index");
      searchIndex = new ForwardIndex(pages); // 使用 ForwardIndex
    } else {
      System.out.println("Using Inverted Index");
      searchIndex = new InvertedIndex(pages); // 使用 InvertedIndex（默认）
    }

    // 确保 scoringQuery 被正确设置
    scoringQuery = new ScoringQuery();
    searchIndex.setScoringQuery(scoringQuery);

    // 调用 searchIndex.search 获取排序后的网页结果
    List<Webpage> sortedResults = searchIndex.search(searchTerms);
    System.out.println("Found " + sortedResults.size() + " matching webpages");

    // 构建 JSON 响应
    var response = new ArrayList<String>();
    for (Webpage page : sortedResults) {
      // 将每个页面的信息格式化为 JSON
      response.add(String.format("{\"url\": \"%s\", \"title\": \"%s\", \"score\": %.2f}",
          page.getUrl(), page.getTitle(), page.getScore()));
    }

    // 将响应内容转换为字节数组
    var bytes = response.toString().getBytes(CHARSET);

    // 返回响应
    respond(io, 200, "application/json", bytes);
  } catch (Exception e) {
    e.printStackTrace();
    System.out.println("Error handling search request: " + e.getMessage());
    respond(io, 500, "text/plain", "Internal Server Error".getBytes(CHARSET));
}
  }

  private void getSelectedLines(HttpExchange io, int startLine, int endLine) throws IOException {
    // 读取文件中的所有行
    List<String> lines = Files.readAllLines(Paths.get("src/jmh/java/searchengine/jmhdata.txt"));

    // 构建指定行的内容
    StringBuilder responseContent = new StringBuilder();
    int maxLines = Math.min(endLine, lines.size());  // 确保不超过文件的总行数
    for (int i = startLine - 1; i < maxLines; i++) {
        responseContent.append(lines.get(i)).append("\n");
    }

    byte[] response = responseContent.toString().getBytes(CHARSET);
    io.getResponseHeaders().set("Content-Type", "text/plain");
    io.sendResponseHeaders(200, response.length);
    io.getResponseBody().write(response);
    io.getResponseBody().close();
}
  public SearchIndex getSearchIndex() {
    return searchIndex;
  }
  
}

