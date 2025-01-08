package searchengine;
public class Main {
    public static void main(String[] args) {
        WebSearchServer server = null;
        try {
            server = new WebSearchServer(WebServer.PORT);
            System.out.println("Press Ctrl+C to stop the server...");
            // 阻塞主线程，直到服务器被终止
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                server.stopServer(); // 停止服务器（需在 WebSearchServer 中实现 stopServer 方法）
            }
            System.out.println("[Main] Closing database connection...");
            SQLiteConnection.closeConnection(); // 程序退出时关闭数据库连接
        }
    }
}