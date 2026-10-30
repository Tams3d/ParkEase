package parkease.client;

public class SlotView {

  private final int number;
  private final boolean free;
  private final String display;

  public SlotView(int number, boolean free, String display) {
    this.number = number;
    this.free = free;
    this.display = display;
  }

  public int number() {
    return number;
  }

  public boolean free() {
    return free;
  }

  public String display() {
    return display;
  }
}
