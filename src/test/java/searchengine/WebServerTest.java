
package searchengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.net.BindException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Random;



import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;


@TestInstance(Lifecycle.PER_CLASS)
class WebServerTest {
    WebServer server = null;

    @BeforeAll
    void setUp() {
        try {
            var rnd = new Random();
            while (server == null) {
                try {
                    server = new WebSearchServer(rnd.nextInt(60000) + 1024, "data/test-file.txt");
                } catch (BindException e) {
                    // port in use. Try again
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    void tearDown() {
        if (server != null && server.server != null) {
            server.server.stop(0);
        }
        server = null;
    }

    @Test
    void lookupWebServer() {
        String baseURL = String.format("http://localhost:%d/search?q=", server.server.getAddress().getPort());
    
        // 对于 word1 的期望输出
        String expectedJsonString = "[{\"url\": \"http://page1.com\", \"title\": \"title1\", \"score\": 5.00}, {\"url\": \"http://page2.com\", \"title\": \"title2\", \"score\": 5.00}]";
        String actualJsonString = httpGet(baseURL + "word1&index=forward");
    
        // 去除空格和换行符后比较
        assertEquals(expectedJsonString.replaceAll("\\s", ""), actualJsonString.replaceAll("\\s", ""));
    
        // 对于 word2 的期望输出
        assertEquals("[{\"url\": \"http://page1.com\", \"title\": \"title1\", \"score\": 5.00}]", 
            httpGet(baseURL + "word2&index=forward"));  
    
        // 对于 word3 的期望输出
        assertEquals("[{\"url\": \"http://page2.com\", \"title\": \"title2\", \"score\": 5.00}]", 
            httpGet(baseURL + "word3&index=forward"));
    
        // 对于 word4 的期望输出
        assertEquals("[]", 
            httpGet(baseURL + "word4&index=forward"));
    }

    @Test
    void lookupWebServer2() {
        String baseURL = String.format("http://localhost:%d/search?q=", server.server.getAddress().getPort());
        assertEquals("[]", 
            httpGet(baseURL + "bowen&index=forward"));
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


}
