package parkease.model;

import parkease.exception.InvalidOwnerException;
import parkease.exception.InvalidPlateException;
import parkease.exception.InvalidVehicleException;

class CarVehicle extends Vehicle {

  CarVehicle(String plate, String owner)
      throws InvalidVehicleException, InvalidPlateException, InvalidOwnerException {
    super(VehicleType.carType, plate, owner);
  }
}
