package parkease.model;

import parkease.exception.InvalidOwnerException;
import parkease.exception.InvalidPlateException;
import parkease.exception.InvalidVehicleException;

class TruckVehicle extends Vehicle {

  TruckVehicle(String plate, String owner)
      throws InvalidVehicleException, InvalidPlateException, InvalidOwnerException {
    super(VehicleType.truckType, plate, owner);
  }
}
