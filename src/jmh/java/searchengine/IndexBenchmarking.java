package searchengine;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime) // 测试平均时间
@OutputTimeUnit(TimeUnit.MILLISECONDS) // 输出时间单位为毫秒
@Warmup(iterations = 1) // 预热1次
@Measurement(iterations = 3) // 正式测量3次
@Fork(value = 1) // 启动一个JVM实例
public class IndexBenchmarking {

    private WebSearchServer server;
    private List<String> queryWords;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        // 初始化查询词
        queryWords = List.of("denmark", "computer", "china", "pakistan", "italy", "poland");

        // 初始化 WebSearchServer（直接使用数据库连接）
        initializeServer();
    }

    private void initializeServer() throws Exception {
        var rnd = new Random();
        int maxAttempts = 10; // 限制端口尝试次数
        int attempts = 0;

        while (server == null && attempts < maxAttempts) {
            try {
                int port = rnd.nextInt(60000) + 1024;
                server = new WebSearchServer(port);
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    throw new RuntimeException("Failed to start server after multiple attempts.", e);
                }
            }
        }
    }

    @Benchmark
    public void measureSearchIndexPerformance() {
        int limit = 30; // 每页 30 个结果
        int offset = 0; // 从第一页开始

        for (String word : queryWords) {
            // 测试每个查询词的搜索性能（带分页参数）
            server.getSearchIndex().search(word, limit, offset);
        }
    }
}