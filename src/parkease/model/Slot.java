package parkease.model;

public class Slot {

  private final int number;
  private String plate;
  private VehicleType type;
  private int ticketNo;

  public Slot(int number) {
    this.number = number;
  }

  public int getNo() {
    return number;
  }

  public boolean isFree() {
    return plate == null;
  }

  public String displayCode() {
    if (isFree()) {
      return "FREE";
    }
    return plate + " " + type.getCode().charAt(0);
  }

  public void take(Ticket ticket) {
    plate = ticket.getVehicle().getPlate();
    type = ticket.getVehicle().getType();
    ticketNo = ticket.getNo();
  }

  public void free() {
    plate = null;
    type = null;
    ticketNo = 0;
  }
}
