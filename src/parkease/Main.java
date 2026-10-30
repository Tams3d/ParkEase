package parkease;

import parkease.client.LocalParkingClient;
import parkease.client.ParkingClient;
import parkease.console.Menu;
import parkease.service.ParkingLot;
import parkease.storage.Storage;

public class Main {

  public static void main(String[] args) {
    try {
      Storage storage = new Storage();
      ParkingLot lot = new ParkingLot(storage);
      ParkingClient client = new LocalParkingClient(lot);
      new Menu(client).start();
    } catch (Exception error) {
      System.out.println();
      String message = error.getMessage();
      if (message == null) {
        message = "Unexpected error.";
      }
      System.out.println("Could not start ParkEase: " + message);
      System.out.println("Check the MySQL settings in .env and try again.");
      System.exit(1);
    }
  }
}
