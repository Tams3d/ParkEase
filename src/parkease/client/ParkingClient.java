package parkease.client;

import parkease.exception.ApiException;
import parkease.model.Ticket;
import parkease.model.Vehicle;

public interface ParkingClient {

  int capacity() throws ApiException;

  Ticket park(Vehicle vehicle) throws ApiException;

  Ticket exit(int number, double hours) throws ApiException;

  Ticket find(int number) throws ApiException;

  SlotMap slots() throws ApiException;

  ParkingSummary summary() throws ApiException;
}
