package parkease.model;

import parkease.exception.InvalidHoursException;

public abstract class Vehicle implements Chargeable {

  private final String plate;
  private final String owner;
  private final VehicleType type;

  protected Vehicle(VehicleType type, String plate, String owner) {
    this.type = type;
    this.plate = plate.trim();
    this.owner = owner.trim();
  }

  @Override
  public int calculateCharge(int hours) throws InvalidHoursException {
    if (hours <= 0) {
      throw new InvalidHoursException("Hours must be greater than zero.");
    }
    int paidHours = Math.min(hours, dailyCapHours);
    return paidHours * type.getRate();
  }

  public VehicleType getType() {
    return type;
  }

  public String getPlate() {
    return plate;
  }

  public String getOwner() {
    return owner;
  }
}
