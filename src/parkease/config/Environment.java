package parkease.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import parkease.exception.StorageException;

public final class Environment {

  private Environment() {
  }

  public static String value(String key, String fallback) throws StorageException {
    String fromEnv = System.getenv(key);
    if (fromEnv != null && !fromEnv.trim().isEmpty()) {
      return fromEnv.trim();
    }
    String fromFile = fileValues().get(key);
    if (fromFile != null && !fromFile.trim().isEmpty()) {
      return fromFile.trim();
    }
    return fallback;
  }

  private static HashMap<String, String> fileValues() throws StorageException {
    HashMap<String, String> values = new HashMap<>();
    File file = new File(System.getProperty("parkease.env", ".env"));
    if (!file.isFile()) {
      return values;
    }

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(
          new InputStreamReader(new FileInputStream(file), "UTF-8"));
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

  private static void parseLine(HashMap<String, String> values, String line) {
    String text = line.trim();
    if (text.isEmpty() || text.startsWith("#")) {
      return;
    }
    int index = text.indexOf('=');
    if (index <= 0) {
      return;
    }
    String key = text.substring(0, index).trim();
    String value = text.substring(index + 1).trim();
    if (value.length() >= 2) {
      char first = value.charAt(0);
      char last = value.charAt(value.length() - 1);
      if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
        value = value.substring(1, value.length() - 1);
      }
    }
    if (!key.isEmpty()) {
      values.put(key, value);
    }
  }
}
