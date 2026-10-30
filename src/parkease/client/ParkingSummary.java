package parkease.client;

import java.util.List;
import parkease.model.Ticket;

public class ParkingSummary {

  private final int capacity;
  private final int free;
  private final int occupied;
  private final int parked;
  private final List<TypeCount> byType;
  private final int ticketCount;
  private final int total;
  private final int today;
  private final List<Ticket> activeTickets;

  public ParkingSummary(int capacity, int free, int occupied, int parked,
      List<TypeCount> byType, int ticketCount, int total, int today,
      List<Ticket> activeTickets) {
    this.capacity = capacity;
    this.free = free;
    this.occupied = occupied;
    this.parked = parked;
    this.byType = byType;
    this.ticketCount = ticketCount;
    this.total = total;
    this.today = today;
    this.activeTickets = activeTickets;
  }

  public int capacity() {
    return capacity;
  }

  public int free() {
    return free;
  }

  public int occupied() {
    return occupied;
  }

  public int parked() {
    return parked;
  }

  public List<TypeCount> byType() {
    return byType;
  }

  public int ticketCount() {
    return ticketCount;
  }

  public int total() {
    return total;
  }

  public int today() {
    return today;
  }

  public List<Ticket> activeTickets() {
    return activeTickets;
  }
}
