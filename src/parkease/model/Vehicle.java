package parkease.model;

import parkease.exception.InvalidHoursException;
import parkease.exception.InvalidOwnerException;
import parkease.exception.InvalidPlateException;
import parkease.exception.InvalidVehicleException;

public abstract class Vehicle implements Chargeable {

  private final String plate;
  private final String owner;
  private final VehicleType type;

  protected Vehicle(VehicleType type, String plate, String owner)
      throws InvalidVehicleException, InvalidPlateException, InvalidOwnerException {
    if (type == null) {
      throw new InvalidVehicleException("Vehicle type is required.");
    }
    if (plate == null || plate.trim().isEmpty()) {
      throw new InvalidPlateException("Number plate is required.");
    }
    if (owner == null || owner.trim().isEmpty()) {
      throw new InvalidOwnerException("Owner name is required.");
    }
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
