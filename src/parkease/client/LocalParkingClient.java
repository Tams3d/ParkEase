package parkease.client;

import java.util.ArrayList;
import java.util.List;
import parkease.exception.DuplicateVehicleException;
import parkease.exception.InvalidHoursException;
import parkease.exception.InvalidTicketException;
import parkease.exception.ParkingFullException;
import parkease.exception.StorageException;
import parkease.model.Slot;
import parkease.model.Ticket;
import parkease.model.Vehicle;
import parkease.model.VehicleType;
import parkease.service.ParkingLot;

public class LocalParkingClient implements ParkingClient {

  private final ParkingLot lot;

  public LocalParkingClient(ParkingLot lot) {
    this.lot = lot;
  }

  @Override
  public int capacity() {
    return lot.getCapacity();
  }

  @Override
  public Ticket park(Vehicle vehicle) throws ParkingFullException, DuplicateVehicleException,
      StorageException {
    return lot.park(vehicle);
  }

  @Override
  public Ticket exit(int number, double hours)
      throws InvalidTicketException, InvalidHoursException, StorageException {
    return lot.exit(number, hours);
  }

  @Override
  public Ticket find(int number) {
    return lot.find(number);
  }

  @Override
  public SlotMap slots() {
    List<SlotView> views = new ArrayList<>();
    Slot[] slots = lot.getSlots();
    for (Slot slot : slots) {
      views.add(new SlotView(slot.getNo(), slot.isFree(), slot.displayCode()));
    }
    int freeCount = lot.freeSlots();
    return new SlotMap(lot.getCapacity(), freeCount, lot.getCapacity() - freeCount, views);
  }

  @Override
  public ParkingSummary summary() throws StorageException {
    List<TypeCount> byType = new ArrayList<>();
    for (VehicleType type : VehicleType.values()) {
      byType.add(new TypeCount(type.getCode(), type.getDisplayName(), lot.count(type)));
    }
    int freeCount = lot.freeSlots();
    return new ParkingSummary(
        lot.getCapacity(),
        freeCount,
        lot.getCapacity() - freeCount,
        lot.parked(),
        byType,
        lot.ticketCount(),
        lot.total(),
        lot.todayTotal());
  }
}
