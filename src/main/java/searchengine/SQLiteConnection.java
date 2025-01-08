package searchengine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:search_engine.db");
                System.out.println("Database connection established.");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to connect to the SQLite database.");
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[SQLiteConnection] Connection closed at:");
                Thread.dumpStack(); // 打印关闭调用的栈追踪
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}