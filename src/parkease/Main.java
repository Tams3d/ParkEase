package parkease;

import java.util.Scanner;
import parkease.console.Menu;
import parkease.service.ParkingLot;
import parkease.storage.Storage;

public class Main {
  public static void main(String[] args) {
    try {
      ParkingLot parking = new ParkingLot(new Storage());
      System.out.println("MySQL connected.");

      Scanner input = new Scanner(System.in);
      try {
        new Menu(parking, input).start();
      } finally {
        input.close();
      }
    } catch (Exception error) {
      System.out.println();
      System.out.println("Could not start ParkEase: " + error.getMessage());
      System.out.println("Check .env, environment variables and README.md.");
      System.exit(1);
    }
  }
}
