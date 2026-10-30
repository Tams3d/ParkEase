package parkease.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import parkease.exception.StorageException;

public final class Environment {

  private Environment() {
  }

  public static String value(String key, String fallback) throws StorageException {
    String fromEnv = System.getenv(key);
    if (fromEnv != null && !fromEnv.trim().isEmpty()) {
      return fromEnv.trim();
    }

    File file = new File(System.getProperty("parkease.env", ".env"));
    if (!file.isFile()) {
      return fallback;
    }

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        int index = line.indexOf('=');
        if (index <= 0) {
          continue;
        }
        String name = line.substring(0, index).trim();
        if (name.equals(key)) {
          return line.substring(index + 1).trim();
        }
      }
    } catch (Exception error) {
      throw new StorageException("Could not read " + file.getAbsolutePath(), error);
    }

    return fallback;
  }
}
