package parkease.model;

import java.util.Date;
import parkease.exception.InvalidTicketException;
import parkease.exception.InvalidVehicleException;

public class Ticket {

  private static int nextNumber = 1001;
  private final int number;
  private final Vehicle vehicle;
  private final int[] slots;
  private final Date entry;
  private int hours;
  private int charge;
  private boolean active = true;

  public Ticket(int number, Vehicle vehicle, int[] slots, Date entry)
      throws InvalidVehicleException, InvalidTicketException {
    if (vehicle == null) {
      throw new InvalidVehicleException("Vehicle is required.");
    }
    if (slots == null || slots.length == 0) {
      throw new InvalidTicketException("At least one slot is required.");
    }
    if (entry == null) {
      throw new InvalidTicketException("Entry time is required.");
    }
    this.number = number;
    this.vehicle = vehicle;
    this.slots = slots.clone();
    this.entry = new Date(entry.getTime());
  }

  public static int takeNextNumber() {
    int currentNumber = nextNumber;
    nextNumber = nextNumber + 1;
    return currentNumber;
  }

  public static void continueFrom(int lastNumber) {
    if (lastNumber >= nextNumber) {
      nextNumber = lastNumber + 1;
    }
  }

  public void close(int hours, int charge) throws InvalidTicketException {
    if (!active) {
      throw new InvalidTicketException("Ticket " + number() + " is already closed.");
    }
    this.hours = hours;
    this.charge = charge;
    this.active = false;
  }

  public int getNo() {
    return number;
  }

  public String number() {
    return "T-" + number;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public int[] getSlots() {
    return slots.clone();
  }

  public String slotText() {
    StringBuilder builder = new StringBuilder();
    for (int index = 0; index < slots.length; index++) {
      if (index > 0) {
        builder.append(" & ");
      }
      builder.append(slots[index]);
    }
    return builder.toString();
  }

  public Date getEntry() {
    return new Date(entry.getTime());
  }

  public int getHours() {
    return hours;
  }

  public int getCharge() {
    return charge;
  }
}
