package parkease.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import parkease.exception.DuplicateVehicleException;
import parkease.exception.InvalidHoursException;
import parkease.exception.InvalidSlotException;
import parkease.exception.InvalidTicketException;
import parkease.exception.ParkingFullException;
import parkease.exception.StorageException;
import parkease.model.Slot;
import parkease.model.Ticket;
import parkease.model.Vehicle;
import parkease.model.VehicleType;
import parkease.storage.Storage;

public class ParkingLot {

  private final int capacity = 20;
  private final Slot[] slots = new Slot[capacity];
  private final Map<Integer, Ticket> tickets = new HashMap<>();
  private final Storage storage;

  public ParkingLot(Storage storage) throws StorageException {
    this.storage = storage;

    for (int index = 0; index < capacity; index++) {
      slots[index] = new Slot(index + 1);
    }

    storage.init(capacity);
    Ticket.continueFrom(storage.lastNo());

    for (Ticket ticket : storage.active()) {
      for (int slotNumber : ticket.getSlots()) {
        slots[slotNumber - 1].take(ticket);
      }
      tickets.put(ticket.getNo(), ticket);
    }
  }

  public int getCapacity() {
    return capacity;
  }

  public Ticket park(Vehicle vehicle)
      throws ParkingFullException, DuplicateVehicleException, InvalidTicketException,
          InvalidSlotException, StorageException {
    Ticket existing = findPlate(vehicle.getPlate());
    if (existing != null) {
      throw new DuplicateVehicleException(
          "Vehicle " + vehicle.getPlate() + " is already parked ("
              + existing.number() + ").");
    }

    VehicleType type = vehicle.getType();
    int needed = type.getSlotCount();
    int start = freeSlot(needed);
    if (start < 0) {
      if (type == VehicleType.truckType) {
        throw new ParkingFullException(
            "Parking full: no two consecutive slots are free for a truck.");
      }
      throw new ParkingFullException(
          "Parking full: no free slot is available for a "
              + type.getDisplayName().toLowerCase() + ".");
    }

    int[] numbers = new int[needed];
    for (int index = 0; index < needed; index++) {
      numbers[index] = slots[start + index].getNo();
    }

    Ticket ticket = new Ticket(Ticket.takeNextNumber(), vehicle, numbers, new Date());
    storage.save(ticket);

    for (int index = 0; index < needed; index++) {
      slots[start + index].take(ticket);
    }
    tickets.put(ticket.getNo(), ticket);
    return ticket;
  }

  public Ticket exit(int number, double hours)
      throws InvalidTicketException, InvalidHoursException, InvalidSlotException, StorageException {
    Ticket ticket = tickets.get(number);
    if (ticket == null) {
      throw new InvalidTicketException("No active ticket T-" + number + ".");
    }
    if (hours <= 0) {
      throw new InvalidHoursException("Hours parked must be greater than 0.");
    }

    int bill = (int) Math.ceil(hours);
    int amount = ticket.getVehicle().calculateCharge(bill);
    Date exitTime = new Date();

    storage.close(ticket, bill, amount, exitTime);

    ticket.close(bill, amount);
    for (int slotNumber : ticket.getSlots()) {
      slots[slotNumber - 1].free();
    }
    tickets.remove(number);
    return ticket;
  }

  private int freeSlot(int needed) {
    for (int start = 0; start <= slots.length - needed; start++) {
      boolean free = true;
      for (int index = 0; index < needed; index++) {
        if (!slots[start + index].isFree()) {
          free = false;
          break;
        }
      }
      if (free) {
        return start;
      }
    }
    return -1;
  }

  private Ticket findPlate(String plate) {
    for (Ticket ticket : tickets.values()) {
      if (ticket.getVehicle().getPlate().equalsIgnoreCase(plate)) {
        return ticket;
      }
    }
    return null;
  }

  public Ticket find(int number) {
    return tickets.get(number);
  }

  public Slot[] getSlots() {
    return slots;
  }

  public int freeSlots() {
    int count = 0;
    for (Slot slot : slots) {
      if (slot.isFree()) {
        count = count + 1;
      }
    }
    return count;
  }

  public int parked() {
    return tickets.size();
  }

  public int count(VehicleType type) {
    int count = 0;
    for (Ticket ticket : tickets.values()) {
      if (ticket.getVehicle().getType() == type) {
        count = count + 1;
      }
    }
    return count;
  }

  public int total() throws StorageException {
    return storage.total();
  }

  public int todayTotal() throws StorageException {
    return storage.todayTotal();
  }

  public int ticketCount() throws StorageException {
    return storage.ticketCount();
  }
}
