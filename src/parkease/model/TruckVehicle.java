package parkease.model;

class TruckVehicle extends Vehicle {

  TruckVehicle(String plate, String owner) {
    super(VehicleType.truckType, plate, owner);
  }
}
