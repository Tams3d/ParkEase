package parkease.model;

class CarVehicle extends Vehicle {

  CarVehicle(String plate, String owner) {
    super(VehicleType.carType, plate, owner);
  }
}
