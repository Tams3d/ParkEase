package parkease.client;

import java.util.List;

public class SlotMap {

  private final int capacity;
  private final int free;
  private final int occupied;
  private final List<SlotView> slots;

  public SlotMap(int capacity, int free, int occupied, List<SlotView> slots) {
    this.capacity = capacity;
    this.free = free;
    this.occupied = occupied;
    this.slots = slots;
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

  public List<SlotView> slots() {
    return slots;
  }
}
