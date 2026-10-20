package parkease.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
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
    HashMap<String, String> values = environment();
    String jdbcUrl = value(values, "PARKEASE_DB_URL", defaultUrl);
    String user = value(values, "PARKEASE_DB_USER", "root");
    String password = value(values, "PARKEASE_DB_PASSWORD", "");

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      return DriverManager.getConnection(jdbcUrl, user, password);
    } catch (Exception error) {
      throw new StorageException(
          "Could not connect to MySQL: " + error.getMessage()
              + "\nCheck .env, environment variables, lib/ and MySQL.",
          error);
    }
  }

  private HashMap<String, String> environment() throws StorageException {
    HashMap<String, String> values = new HashMap<>();
    File file = new File(System.getProperty("parkease.env", ".env"));
    if (!file.isFile()) {
      return values;
    }

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
      String line;
      while ((line = reader.readLine()) != null) {
        parseLine(values, line);
      }
    } catch (Exception error) {
      throw new StorageException("Could not read " + file.getAbsolutePath(), error);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception ignored) {
        }
      }
    }
    return values;
  }

  private void parseLine(HashMap<String, String> values, String line) {
    String text = line.trim();
    if (text.isEmpty() || text.startsWith("#")) {
      return;
    }
    int index = text.indexOf('=');
    if (index <= 0) {
      return;
    }
    String keyName = text.substring(0, index).trim();
    String value = text.substring(index + 1).trim();
    if (value.length() >= 2) {
      char first = value.charAt(0);
      char last = value.charAt(value.length() - 1);
      if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
        value = value.substring(1, value.length() - 1);
      }
    }
    if (!keyName.isEmpty()) {
      values.put(keyName, value);
    }
  }

  private String value(HashMap<String, String> values, String keyName, String fallback) {
    String fromEnv = System.getenv(keyName);
    if (fromEnv != null && !fromEnv.trim().isEmpty()) {
      return fromEnv.trim();
    }
    String fromFile = values.get(keyName);
    if (fromFile != null && !fromFile.trim().isEmpty()) {
      return fromFile.trim();
    }
    return fallback;
  }
}
