package searchengine;

import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1)  // 预热1次
@Measurement(iterations = 2)  // 正式测量2次
@Fork(value = 1)  // 只启动一次新的JVM

public class IndexBenchmarking {
    WebSearchServer server;
    List<String> hitSearchTerms;
    List<String> missSearchTerms;
    // QUERY WORDS
    ArrayList<String> queryWords = new ArrayList<>();

    @Param({ "data/enwiki-tiny.txt", "data/enwiki-small.txt", "data/enwiki-medium.txt" })
    String filename;

    @Setup(Level.Trial)
    public void setup() {
        queryWords.add("denmark");
        queryWords.add("computer");
        queryWords.add("china");
        queryWords.add("pakistan");
        queryWords.add("italy");
        queryWords.add("poland");
    
        try {
            var rnd = new Random();
            while (server == null) {
                try {
                    server = new WebSearchServer(rnd.nextInt(60000) + 1024, filename);
                } catch (BindException e) {
                    // port in use. Try again
                }
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @Benchmark
    public void measureAvgTime() throws InterruptedException {

        for (String word : queryWords) {

            server.getSearchIndex().search(word);

        }

    }

}