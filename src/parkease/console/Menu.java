package parkease.console;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;
import parkease.client.ParkingClient;
import parkease.client.ParkingSummary;
import parkease.client.SlotMap;
import parkease.client.SlotView;
import parkease.client.TypeCount;
import parkease.exception.InvalidPlateException;
import parkease.model.Chargeable;
import parkease.model.Ticket;
import parkease.model.Vehicle;
import parkease.model.VehicleType;
import parkease.validation.Plate;

public class Menu {

  private final SimpleDateFormat formatter =
      new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH);
  private final Plate validator = new Plate();
  private final ParkingClient parking;
  private final Scanner sc = new Scanner(System.in);

  public Menu(ParkingClient parking) {
    this.parking = parking;
    formatter.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
  }

  public void start() throws Exception {
    System.out.println();
    System.out.println("  ParkEase");
    System.out.println("  Parking management for bikes, cars and trucks");
    System.out.println("  " + parking.capacity() + " slots available");
    System.out.println();
    System.out.println("  Welcome. Select an option from the menu.");
    System.out.println();

    boolean running = true;
    while (running) {
      menu();
      Integer choice = readInt("  Select an option: ");
      if (choice == null) {
        break;
      }

      System.out.println();
      try {
        switch (choice) {
          case 1:
            park();
            break;
          case 2:
            exit();
            break;
          case 3:
            slotMap();
            break;
          case 4:
            summary();
            break;
          case 5:
            running = false;
            break;
          default:
            System.out.println("  Please enter a number from 1 to 5.");
        }
      } catch (Exception error) {
        showError(error);
      }
      System.out.println();
    }

    System.out.println("Thank you for using ParkEase. Have a good day.");
  }

  private void showError(Exception error) {
    String message = error.getMessage();
    System.out.println("  Error: " + (message == null ? "Unexpected error." : message));
    System.out.println("  Please try again.");
  }

  private void menu() {
    System.out.println("  What would you like to do?");
    System.out.println();
    System.out.println("  1 - Park a vehicle");
    System.out.println("  2 - Exit a vehicle");
    System.out.println("  3 - View slot map");
    System.out.println("  4 - View summary");
    System.out.println("  5 - Quit");
    System.out.println();
  }

  private void park() throws Exception {
    System.out.println("  Park a vehicle");
    System.out.println();
    System.out.println("    Vehicle choices:");
    VehicleType[] types = VehicleType.values();
    for (int index = 0; index < types.length; index++) {
      VehicleType option = types[index];
      int slots = option.getSlotCount();
      String slotInfo = slots == 1 ? "1 slot" : slots + " consecutive slots";
      System.out.println(String.format(Locale.ENGLISH, "      %d - %-6s Rs. %d/hour, %s",
          index + 1, option.getDisplayName(), option.getRate(), slotInfo));
    }
    System.out.println();

    VehicleType type = null;
    while (type == null) {
      Integer choice = readInt("    Select vehicle type: ");
      if (choice == null) {
        return;
      }
      if (choice >= 1 && choice <= types.length) {
        type = types[choice - 1];
      } else {
        System.out.println("    Please choose a number from 1 to " + types.length + ".");
      }
    }

    String plate = null;
    while (plate == null) {
      String input = read("    Enter number plate (example TN37AB1234): ");
      if (input == null) {
        return;
      }
      try {
        plate = validator.check(input);
      } catch (InvalidPlateException error) {
        System.out.println("    " + error.getMessage());
        System.out.println("    Please try again.");
      }
    }

    String owner = owner();
    if (owner == null) {
      return;
    }

    Vehicle vehicle = type.createVehicle(plate, owner);
    Ticket ticket = parking.park(vehicle);

    System.out.println();
    System.out.println("  Ticket " + ticket.number() + " issued.");
    System.out.println("  Keep this ticket number for exit.");
    System.out.println();
    System.out.println("    Vehicle  " + vehicle.getPlate() + " (" + type.getDisplayName() + ")");
    System.out.println("    Owner    " + vehicle.getOwner());
    System.out.println("    Slots    " + ticket.slotText());
    System.out.println("    Entry    " + formatter.format(ticket.getEntry()) + " IST");
    System.out.println("    Rate     Rs. " + type.getRate() + "/hour (daily cap: " + Chargeable.dailyCapHours + " hours)");
  }

  private String owner() {
    System.out.print("    Owner name: ");
    System.out.flush();
    String name = sc.hasNextLine() ? sc.nextLine() : null;
    if (name != null) {
      name = name.trim();
      if (name.isEmpty()) {
        System.out.println("    Please enter an owner name.");
        return null;
      }
    }
    return name;
  }

  private void exit() throws Exception {
    System.out.println("  Exit a vehicle");
    System.out.println();
    System.out.println("    Enter the ticket number from the parking ticket.");

    Ticket ticket = null;
    while (ticket == null) {
      String input = read("    Ticket number (T-1001 or 1001): ");
      if (input == null) {
        return;
      }
      Integer number = ticketNo(input);
      if (number == null) {
        System.out.println("    Please enter a ticket number such as T-1001 or 1001.");
        continue;
      }
      ticket = parking.find(number);
      if (ticket == null) {
        System.out.println("    No active ticket T-" + number + ". Try again.");
        continue;
      }
    }

    Double hours = readDouble("    Hours parked: ");
    if (hours == null || hours <= 0) {
      System.out.println("    Hours parked must be greater than 0.");
      return;
    }

    System.out.println();
    System.out.println("    Closing ticket... (this may take a moment)");
    Ticket closedTicket = parking.exit(ticket.getNo(), hours);
    System.out.println("  Ticket " + closedTicket.number() + " exited.");
    System.out.println("  Charge: Rs. " + closedTicket.getCharge());
    String label = closedTicket.getSlots().length == 1 ? "Slot " : "Slots ";
    String verb = closedTicket.getSlots().length == 1 ? " is free again." : " are free again.";
    System.out.println("  " + label + closedTicket.slotText() + verb);
  }

  private void slotMap() throws Exception {
    SlotMap map = parking.slots();
    System.out.println("  Slot map");
    System.out.println("    Occupied slots show the plate and vehicle code.");
    System.out.println("    Free slots show FREE.");
    System.out.println();
    for (SlotView slot : map.slots()) {
      System.out.printf(Locale.ENGLISH, "    %2d: %-16s%n", slot.number(), slot.display());
    }
    System.out.println();
    System.out.println("    " + map.free() + " free, " + map.occupied() + " occupied.");
  }

  private void summary() throws Exception {
    ParkingSummary summary = parking.summary();
    System.out.println("  Parking summary");
    System.out.println("    Slots       " + summary.capacity() + " total, " + summary.free()
        + " free, " + summary.occupied() + " occupied");
    System.out.println("    Vehicles    " + summary.parked() + " parked");
    for (TypeCount type : summary.byType()) {
      System.out.println("      " + type.name() + "       " + type.count());
    }
    System.out.println("    Tickets     " + summary.ticketCount() + " issued");
    System.out.println("    Collection  Rs. " + summary.total() + " (all time)");
    System.out.println("    Today       Rs. " + summary.today());
  }

  private Integer readInt(String text) {
    System.out.print(text);
    System.out.flush();
    if (!sc.hasNextLine()) {
      return null;
    }
    String line = sc.nextLine();
    try {
      return Integer.parseInt(line.trim());
    } catch (NumberFormatException error) {
      return null;
    }
  }

  private String read(String text) {
    System.out.print(text);
    System.out.flush();
    if (!sc.hasNextLine()) {
      return null;
    }
    return sc.nextLine();
  }

  private Double readDouble(String text) {
    System.out.print(text);
    System.out.flush();
    if (!sc.hasNextLine()) {
      return null;
    }
    String line = sc.nextLine();
    try {
      return Double.parseDouble(line.trim());
    } catch (NumberFormatException error) {
      return null;
    }
  }

  private Integer ticketNo(String input) {
    String text = input.trim().toUpperCase(Locale.ENGLISH);
    if (text.startsWith("T-")) {
      text = text.substring(2);
    } else if (text.startsWith("T")) {
      text = text.substring(1);
    }
    if (text.isEmpty()) {
      return null;
    }
    try {
      int number = Integer.parseInt(text);
      return number > 0 ? number : null;
    } catch (NumberFormatException error) {
      return null;
    }
  }
}
