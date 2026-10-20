package parkease.service;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import parkease.exception.DuplicateVehicleException;
import parkease.exception.InvalidHoursException;
import parkease.exception.InvalidSlotException;
import parkease.exception.InvalidTicketException;
import parkease.exception.InvalidVehicleException;
import parkease.exception.ParkingFullException;
import parkease.exception.StorageException;
import parkease.model.Chargeable;
import parkease.model.Slot;
import parkease.model.Ticket;
import parkease.model.Vehicle;
import parkease.model.VehicleType;
import parkease.storage.Storage;

public class ParkingLot {

  private static final int capacity = 20;
  private final Slot[] slots;
  private final LinkedHashMap<Integer, Ticket> tickets = new LinkedHashMap<>();
  private final Storage storage;

  public ParkingLot(Storage storage) throws StorageException {
    if (storage == null) {
      throw new StorageException("Storage is required.");
    }
    this.storage = storage;

    slots = new Slot[capacity];
    for (int index = 0; index < capacity; index++) {
      slots[index] = new Slot(index + 1);
    }

    storage.init(capacity);
    Ticket.continueFrom(storage.lastNo());

    for (Ticket ticket : storage.active()) {
      try {
        load(ticket);
      } catch (InvalidSlotException error) {
        throw new StorageException("Could not load active tickets.", error);
      }
      tickets.put(ticket.getNo(), ticket);
    }
  }

  public int getCapacity() {
    return capacity;
  }

  public Ticket park(Vehicle vehicle)
      throws ParkingFullException, DuplicateVehicleException, InvalidVehicleException,
          InvalidTicketException, InvalidSlotException, StorageException {
    if (vehicle == null) {
      throw new InvalidVehicleException("Vehicle is required.");
    }

    Ticket existing = findPlate(vehicle.getPlate());
    if (existing != null) {
      throw new DuplicateVehicleException(
          "Vehicle " + vehicle.getPlate() + " is already parked ("
              + existing.number() + ").");
    }
    if (storage.parked(vehicle.getPlate())) {
      throw new DuplicateVehicleException(
          "Vehicle " + vehicle.getPlate() + " is already parked.");
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
    if (hours <= 0 || Double.isNaN(hours) || Double.isInfinite(hours)) {
      throw new InvalidHoursException("Hours parked must be greater than 0.");
    }

    int bill = (int) Math.ceil(hours);
    Chargeable chargeable = ticket.getVehicle();
    int amount = chargeable.calculateCharge(bill);
    Date exitTime = new Date();

    storage.close(ticket, bill, amount, exitTime);

    ticket.close(bill, amount);
    for (int slotNumber : ticket.getSlots()) {
      slots[slotNumber - 1].free(number);
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

  private void load(Ticket ticket) throws InvalidSlotException {
    for (int slotNumber : ticket.getSlots()) {
      if (slotNumber < 1 || slotNumber > capacity) {
        throw new InvalidSlotException("Ticket uses an invalid slot: " + slotNumber);
      }
      slots[slotNumber - 1].take(ticket);
    }
  }

  private Ticket findPlate(String plate) {
    return tickets.values().stream()
        .filter(ticket -> ticket.getVehicle().getPlate().equalsIgnoreCase(plate))
        .findFirst()
        .orElse(null);
  }

  public Ticket find(int number) {
    return tickets.get(number);
  }

  public Slot[] getSlots() {
    return Arrays.copyOf(slots, slots.length);
  }

  public int freeSlots() {
    return (int) Arrays.stream(slots).filter(Slot::isFree).count();
  }

  public int parked() {
    return tickets.size();
  }

  public int count(VehicleType type) {
    return (int) tickets.values().stream()
        .filter(ticket -> ticket.getVehicle().getType() == type)
        .count();
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
