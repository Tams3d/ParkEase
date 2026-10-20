package parkease;

import java.util.Scanner;
import parkease.client.RemoteParkingClient;
import parkease.config.Environment;
import parkease.console.Menu;

public class Main {

  public static void main(String[] args) {
    try {
      String apiUrl = apiUrl(args);
      Scanner input = new Scanner(System.in);
      try {
        new Menu(new RemoteParkingClient(apiUrl), input).start();
      } finally {
        input.close();
      }
    } catch (Exception error) {
      System.out.println();
      System.out.println("Could not start ParkEase: " + error.getMessage());
      System.out.println("Start the backend and check PARKEASE_API_URL.");
      System.exit(1);
    }
  }

  private static String apiUrl(String[] args) throws Exception {
    String value = Environment.value("PARKEASE_API_URL", null);
    for (String arg : args) {
      if (arg != null && arg.startsWith("--api=")) {
        value = arg.substring("--api=".length());
      }
    }
    if (value == null || value.trim().isEmpty()) {
      return "http://localhost:8080";
    }
    return value.trim();
  }
}
