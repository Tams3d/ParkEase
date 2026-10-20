package parkease.model;

import parkease.exception.InvalidOwnerException;
import parkease.exception.InvalidPlateException;
import parkease.exception.InvalidVehicleException;

class BikeVehicle extends Vehicle {

  BikeVehicle(String plate, String owner)
      throws InvalidVehicleException, InvalidPlateException, InvalidOwnerException {
    super(VehicleType.bikeType, plate, owner);
  }
}
