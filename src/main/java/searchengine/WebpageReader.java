package searchengine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to read webpages from filename.
 */
public class WebpageReader {
    public static final int MIN_NUMBER_OF_LINES_PER_WEBPAGE = 3;

    /**
     * Reads all the lines from the file passed as an argument and adds them to the
     * webpages. Webpage that is not containing any url, title and at least one word
     * is omitted.
     * 
     * @param filename
     * @return
     */
    public static List<Webpage> fetchWebpages(String filename) throws IOException {
        
        List<Webpage> pages = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            var lastIndex = lines.size();
            for (var i = lines.size() - 1; i >= 0; --i) {
                if (lines.get(i).startsWith("*PAGE")) {
                    if (lastIndex - i >= MIN_NUMBER_OF_LINES_PER_WEBPAGE) {
                        Webpage webpage = extractWebpage(lines, lastIndex, i);
                        pages.add(webpage);
                    }
                    lastIndex = i;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Collections.reverse(pages);

        return pages;
    }

    /**
     * Extract the URL, title and words from the file, and create Webpage Object.
     * 
     * @param pages
     * @param lines
     * @param lastIndex
     * @param index
     */
    private static Webpage extractWebpage(List<String> lines, int lastIndex, int index) {
        List<String> pageLinesSublist = lines.subList(index, lastIndex);
        String url = pageLinesSublist.get(0).substring(6);
        String title = pageLinesSublist.get(1);
        List<String> words = pageLinesSublist.subList(2, pageLinesSublist.size());

        Map<String, Integer> mapOfSearchCount = countSearchTerms(words);

        Webpage webpage = new Webpage(title, url);
        webpage.getWords().addAll(words);
        webpage.setSearchTermCount(mapOfSearchCount);
        return webpage;
    }

    /**
     * Count the words mentioned in the list.
     * @param words
     * @return
     */
    public static Map<String, Integer> countSearchTerms(List<String> words) {
        Map<String, Integer> searchtermFrequency = new HashMap<String, Integer>();

        for(String word : words){
            if (searchtermFrequency.containsKey(word)) {
                searchtermFrequency.put(word, searchtermFrequency.get(word) + 1);
            } else {
                searchtermFrequency.put(word, 1);
            }
        }
        return searchtermFrequency;
    }
    
}