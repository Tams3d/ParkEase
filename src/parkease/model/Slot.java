package parkease.model;

public class Slot {

  private final int number;
  private String plate;
  private VehicleType type;
  private int ticketNumber;

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
    return "T-" + ticketNumber + " " + plate + " " + type.getCode().charAt(0);
  }

  public int getTicketNumber() {
    return ticketNumber;
  }

  public void take(Ticket ticket) {
    plate = ticket.getVehicle().getPlate();
    type = ticket.getVehicle().getType();
    ticketNumber = ticket.getNo();
  }

  public void free() {
    plate = null;
    type = null;
    ticketNumber = 0;
  }
}
