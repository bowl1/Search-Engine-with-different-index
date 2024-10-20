package searchengine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
	public static void main(String[] args) throws IOException {
		var filename = Files.readString(Paths.get("config.txt")).strip();
		new WebSearchServer(WebServer.PORT, filename);
	}
}
