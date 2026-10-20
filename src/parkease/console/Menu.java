package parkease.console;

import java.text.SimpleDateFormat;
import java.util.List;
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
  private final Scanner reader;
  private final boolean interactive = System.console() != null;

  public Menu(ParkingClient parking, Scanner reader) {
    this.parking = parking;
    this.reader = reader;
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
    System.out.println(
        "    Vehicle  " + vehicle.getPlate() + " (" + type.getDisplayName() + ")");
    System.out.println("    Owner    " + vehicle.getOwner());
    System.out.println("    Slots    " + ticket.slotText());
    System.out.println("    Entry    " + formatter.format(ticket.getEntry()) + " IST");
    System.out.println(
        "    Rate     Rs. " + type.getRate()
            + "/hour (daily cap: " + Chargeable.dailyCapHours + " hours)");
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
        System.out.println("    No active ticket T-" + number + ". Please check the number.");
      }
    }
    int number = ticket.getNo();
    Vehicle vehicle = ticket.getVehicle();

    System.out.println();
    System.out.println(
        "    Vehicle  " + vehicle.getPlate() + " (" + vehicle.getType().getDisplayName() + ")");
    System.out.println("    Slots    " + ticket.slotText());
    System.out.println("    Entry    " + formatter.format(ticket.getEntry()) + " IST");
    System.out.println();

    System.out.println("    Enter hours parked. Partial hours are rounded up.");
    Double hours = readHours("    Hours parked (example 2.5): ");
    if (hours == null) {
      return;
    }

    Ticket closed = parking.exit(number, hours);

    int bill = closed.getHours();
    int paid = Math.min(bill, Chargeable.dailyCapHours);
    int rate = closed.getVehicle().getType().getRate();

    System.out.println();
    System.out.println("  Charge summary");
    System.out.println();
    if (bill != hours) {
      System.out.println("    Partial hours rounded up: " + hoursText(hours)
          + " -> " + bill + " hour(s).");
    }
    if (bill > paid) {
      System.out.println("    Daily cap applied: " + bill + " hour(s) billed as " + paid + ".");
    }
    System.out.println("    Rate     Rs. " + rate + "/hour");
    System.out.println("    Charge   Rs. " + closed.getCharge());

    int[] slots = closed.getSlots();
    String label = slots.length == 1 ? "Slot " : "Slots ";
    String verb = slots.length == 1 ? " is free." : " are free.";
    System.out.println();
    System.out.println("    " + label + closed.slotText() + verb);
  }

  private void slotMap() throws Exception {
    System.out.println("  Slot map");
    System.out.println();
    System.out.println("    Occupied slots show the plate and vehicle code.");
    System.out.println("    Free slots show FREE.");
    System.out.println();

    SlotMap map = parking.slots();
    List<SlotView> slots = map.slots();
    String border = "  +--------------+--------------+--------------+--------------+--------------+";
    int columns = 5;
    for (int start = 0; start < slots.size(); start += columns) {
      System.out.println(border);
      System.out.print("  ");
      for (int offset = 0; offset < columns; offset++) {
        int index = start + offset;
        String label = index < slots.size() ? "Slot " + slots.get(index).number() : "";
        System.out.print("| " + padCell(label));
      }
      System.out.println("|");

      System.out.print("  ");
      for (int offset = 0; offset < columns; offset++) {
        int index = start + offset;
        String code = index < slots.size() ? slots.get(index).display() : "";
        System.out.print("| " + padCell(code));
      }
      System.out.println("|");
    }
    System.out.println(border);
    System.out.println();

    System.out.println("    " + map.occupied() + " occupied, " + map.free() + " free");

    StringBuilder legend = new StringBuilder();
    for (VehicleType type : VehicleType.values()) {
      if (legend.length() > 0) {
        legend.append(" | ");
      }
      legend.append(type.getCode().charAt(0)).append(" ").append(type.getDisplayName());
    }
    legend.append(" | FREE available");
    System.out.println("    " + legend);
  }

  private String padCell(String text) {
    return String.format("%-13s", text);
  }

  private void summary() throws Exception {
    ParkingSummary summary = parking.summary();
    System.out.println("  Parking summary");
    System.out.println();
    System.out.println("    Slots       " + summary.capacity() + " total, " + summary.free()
        + " free, " + summary.occupied() + " occupied");
    System.out.println("    Vehicles    " + summary.parked() + " parked");

    StringBuilder byType = new StringBuilder();
    for (TypeCount type : summary.byType()) {
      if (byType.length() > 0) {
        byType.append(", ");
      }
      byType.append(type.name()).append(" ").append(type.count());
    }
    System.out.println("    By type     " + byType);
    System.out.println("    Tickets     " + summary.ticketCount() + " issued");
    System.out.println("    Collection  Rs. " + summary.total() + " (all time)");
    System.out.println("    Today       Rs. " + summary.today());
  }

  private String owner() {
    while (true) {
      String name = read("    Enter owner name: ");
      if (name == null) {
        return null;
      }
      name = name.trim();
      if (name.isEmpty()) {
        System.out.println("    Please enter an owner name.");
      } else if (name.length() > 60) {
        System.out.println("    Owner name must be 60 characters or fewer. Please try again.");
      } else {
        return name;
      }
    }
  }

  private Integer readInt(String text) {
    while (true) {
      String value = read(text);
      if (value == null) {
        return null;
      }
      try {
        return Integer.parseInt(value.trim());
      } catch (Exception error) {
        System.out.println("    Please enter a whole number.");
      }
    }
  }

  private Double readHours(String text) {
    while (true) {
      String value = read(text);
      if (value == null) {
        return null;
      }
      try {
        double hours = Double.parseDouble(value.trim());
        if (hours <= 0 || Double.isNaN(hours) || Double.isInfinite(hours)) {
          System.out.println("    Please enter hours greater than 0.");
        } else {
          return hours;
        }
      } catch (Exception error) {
        System.out.println("    Please enter hours as a number, for example 2.5.");
      }
    }
  }

  private String read(String text) {
    System.out.print(text);
    System.out.flush();
    if (!reader.hasNextLine()) {
      return null;
    }
    String line = reader.nextLine();
    if (!interactive) {
      System.out.println();
    }
    return line;
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
    } catch (Exception error) {
      return null;
    }
  }

  private String hoursText(double hours) {
    if (hours == Math.rint(hours)) {
      return String.valueOf((long) hours);
    }
    return String.valueOf(hours);
  }
}
