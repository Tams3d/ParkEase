package parkease.client;

import java.util.List;

public class ParkingSummary {

  private final int capacity;
  private final int free;
  private final int occupied;
  private final int parked;
  private final List<TypeCount> byType;
  private final int ticketCount;
  private final int total;
  private final int today;

  public ParkingSummary(int capacity, int free, int occupied, int parked,
      List<TypeCount> byType, int ticketCount, int total, int today) {
    this.capacity = capacity;
    this.free = free;
    this.occupied = occupied;
    this.parked = parked;
    this.byType = byType;
    this.ticketCount = ticketCount;
    this.total = total;
    this.today = today;
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
}
