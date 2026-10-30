package parkease.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import parkease.config.Environment;
import parkease.exception.StorageException;

class Database {

  Connection connect() throws StorageException {
    String host = Environment.value("PARKEASE_DB_HOST", "localhost");
    String port = Environment.value("PARKEASE_DB_PORT", "3306");
    String name = Environment.value("PARKEASE_DB_NAME", "parkease");
    String user = Environment.value("PARKEASE_DB_USER", "root");
    String password = Environment.value("PARKEASE_DB_PASSWORD", "");

    String url =
        "jdbc:mysql://" + host + ":" + port + "/" + name
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia/Kolkata&characterEncoding=UTF-8";

    try {
      return DriverManager.getConnection(url, user, password);
    } catch (Exception error) {
      throw new StorageException(
          "Could not connect to MySQL at " + host + ":" + port + "/" + name
              + ". Make sure MySQL is running, the database exists and the"
              + " credentials in .env are correct. To create it run:"
              + " CREATE DATABASE " + name + ";",
          error);
    }
  }
}
