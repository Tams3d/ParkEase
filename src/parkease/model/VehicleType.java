package parkease.model;

import parkease.exception.InvalidOwnerException;
import parkease.exception.InvalidPlateException;
import parkease.exception.InvalidVehicleException;

public enum VehicleType {

  bikeType("Bike", 10, 1, "BIKE"),
  carType("Car", 30, 1, "CAR"),
  truckType("Truck", 60, 2, "TRUCK");

  private final String displayName;
  private final int rate;
  private final int slotCount;
  private final String code;

  VehicleType(String displayName, int rate, int slotCount, String code) {
    this.displayName = displayName;
    this.rate = rate;
    this.slotCount = slotCount;
    this.code = code;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getRate() {
    return rate;
  }

  public int getSlotCount() {
    return slotCount;
  }

  public String getCode() {
    return code;
  }

  public static VehicleType fromCode(String code) throws InvalidVehicleException {
    if (code == null) {
      throw new InvalidVehicleException("Vehicle type is required.");
    }
    for (VehicleType type : values()) {
      if (type.code.equalsIgnoreCase(code.trim())) {
        return type;
      }
    }
    throw new InvalidVehicleException("Unknown vehicle type: " + code);
  }

  public Vehicle createVehicle(String plate, String owner)
      throws InvalidVehicleException, InvalidPlateException, InvalidOwnerException {
    switch (this) {
      case bikeType:
        return new BikeVehicle(plate, owner);
      case carType:
        return new CarVehicle(plate, owner);
      case truckType:
        return new TruckVehicle(plate, owner);
      default:
        throw new InvalidVehicleException("Unknown vehicle type.");
    }
  }
}
