package searchengine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class WebServer {
  static final int PORT = 8080;
  static final int BACKLOG = 0;
  static final Charset CHARSET = StandardCharsets.UTF_8;

  protected HttpServer server;
  

  WebServer(int port) throws IOException {
    server = HttpServer.create(new InetSocketAddress(port), BACKLOG);
  }

  /**
   * Starts the server, and prints server that the server has started, along with
   * descripton of the server URL.gradlew 
   * 
   * @param port
   */
  protected void startServer(int port) {
    server.start();
    String msg = " WebServer running on http://localhost:" + port + " ";
    System.out.println("╭" + "─".repeat(msg.length()) + "╮");
    System.out.println("│" + msg + "│");
    System.out.println("╰" + "─".repeat(msg.length()) + "╯");

  }

  /**
   * Gets the filename as an argument and returns the content as a byte of array.
   * @param filename
   * @return
   */
  protected byte[] getFile(String filename) {
    try {
      return Files.readAllBytes(Paths.get(filename));
    } catch (IOException e) {
      e.printStackTrace();
      return new byte[0];
    }
    
  }

  /**
   * Builds the HTTP response, by adding content type, HTTP status code and the
   * actual content as byte array.
   * @param io
   * @param code
   * @param mime
   * @param response
   */
  protected void respond(HttpExchange io, int code, String mime, byte[] response) {
    try {
      io.getResponseHeaders().set("Content-Type", String.format("%s; charset=%s", mime, CHARSET.name()));
      io.sendResponseHeaders(code, response.length);
      io.getResponseBody().write(response);
    } catch (Exception e) {
    } finally {
      io.close();
    }
  }
}