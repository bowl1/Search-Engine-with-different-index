package searchengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class WebPageReaderTest {
    WebpageReader reader = null;

    @BeforeAll
    void setUp() {
        reader = new WebpageReader();
    }

    @AfterAll
    void tearDown() {
        reader = null;
    }

    @Test
    void testFetchWebpages() {
        try {
            var pages = WebpageReader.fetchWebpages("data/test-file.txt");
            assertEquals(2,pages.size());
            assertEquals("title1", pages.get(0).getTitle());
            assertEquals("word1", pages.get(0).getWords().get(0));


            WebpageReader.fetchWebpages("data/test-file-errors.txt");
            assertNotEquals(5,pages.size());
            

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}