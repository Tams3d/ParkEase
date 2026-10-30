package parkease.client;

import parkease.exception.DuplicateVehicleException;
import parkease.exception.InvalidHoursException;
import parkease.exception.InvalidSlotException;
import parkease.exception.InvalidTicketException;
import parkease.exception.InvalidVehicleException;
import parkease.exception.ParkingFullException;
import parkease.exception.StorageException;
import parkease.model.Ticket;
import parkease.model.Vehicle;

public interface ParkingClient {

  int capacity();

  Ticket park(Vehicle vehicle) throws ParkingFullException, DuplicateVehicleException,
      InvalidVehicleException, InvalidTicketException, InvalidSlotException, StorageException;

  Ticket exit(int number, double hours)
      throws InvalidTicketException, InvalidHoursException, InvalidSlotException, StorageException;

  Ticket find(int number);

  SlotMap slots();

  ParkingSummary summary() throws StorageException;
}
