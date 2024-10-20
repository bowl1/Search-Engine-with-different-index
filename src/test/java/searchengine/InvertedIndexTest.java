
package searchengine;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@TestInstance(Lifecycle.PER_CLASS)
class InvertedIndexTest {
    InvertedIndex index = null;
    ScoringQuery scoringQuery = null;

    @BeforeAll
    void setUp() {
        List<Webpage> pages = new ArrayList<>();
        try {
            pages = WebpageReader.fetchWebpages("data/test-file.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        scoringQuery = new ScoringQuery();
        index = new InvertedIndex(pages);
        index.setScoringQuery(scoringQuery);
    }

    @AfterAll
    void tearDown() {
        index = null;
    }

    @Test
    void testSearchEmpty() {
        // Test data
        List<Webpage> emptyList = new ArrayList<>();

        // Actual search
        final List<Webpage> wordResult = index.search("word");

        // Test
        assertEquals(emptyList, wordResult);
    }

    @Test
    void testSearch() {
        // Test data
        List<Webpage> word1ExpectingList = new ArrayList<>();
        Webpage w1 = new Webpage("title1", "http://page1.com");

        word1ExpectingList.add(w1);

        // Actual search
        final List<Webpage> word1Result = index.search("word5");

        // Test
        assertNotEquals(word1ExpectingList, word1Result);
    }
}