package parkease.model;

class BikeVehicle extends Vehicle {

  BikeVehicle(String plate, String owner) {
    super(VehicleType.bikeType, plate, owner);
  }
}
