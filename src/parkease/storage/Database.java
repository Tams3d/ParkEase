package parkease.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import parkease.config.Environment;
import parkease.exception.StorageException;

class Database {

  private final String defaultUrl =
      "jdbc:mysql://localhost:3306/parkease"
          + "?createDatabaseIfNotExist=true"
          + "&useSSL=false"
          + "&allowPublicKeyRetrieval=true"
          + "&serverTimezone=Asia/Kolkata"
          + "&characterEncoding=UTF-8";

  Connection connect() throws StorageException {
    String jdbcUrl = Environment.value("PARKEASE_DB_URL", defaultUrl);
    String user = Environment.value("PARKEASE_DB_USER", "root");
    String password = Environment.value("PARKEASE_DB_PASSWORD", "");

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      return DriverManager.getConnection(jdbcUrl, user, password);
    } catch (Exception error) {
      throw new StorageException(
          "Could not connect to MySQL: " + error.getMessage()
              + "\nCheck .env, environment variables and MySQL.",
          error);
    }
  }
}
