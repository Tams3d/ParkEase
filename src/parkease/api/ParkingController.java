package parkease.api;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import parkease.api.ApiDtos.ExitRequest;
import parkease.api.ApiDtos.ParkRequest;
import parkease.api.ApiDtos.SlotMapResponse;
import parkease.api.ApiDtos.SlotResponse;
import parkease.api.ApiDtos.SummaryResponse;
import parkease.api.ApiDtos.TicketResponse;
import parkease.api.ApiDtos.TypeCount;
import parkease.api.ApiDtos.VehicleTypeResponse;
import parkease.exception.InvalidTicketException;
import parkease.exception.StorageException;
import parkease.model.Slot;
import parkease.model.Ticket;
import parkease.model.Vehicle;
import parkease.model.VehicleType;
import parkease.service.ParkingLot;
import parkease.validation.Plate;

@RestController
@RequestMapping("/api")
public class ParkingController {

  private final ParkingLot parking;
  private final Plate validator = new Plate();

  public ParkingController(ParkingLot parking) {
    this.parking = parking;
  }

  @GetMapping("/vehicle-types")
  public List<VehicleTypeResponse> vehicleTypes() {
    List<VehicleTypeResponse> types = new ArrayList<>();
    for (VehicleType type : VehicleType.values()) {
      types.add(new VehicleTypeResponse(
          type.getCode(),
          type.getDisplayName(),
          type.getRate(),
          type.getSlotCount()));
    }
    return types;
  }

  @GetMapping("/slots")
  public SlotMapResponse slots() {
    synchronized (parking) {
      Slot[] slots = parking.getSlots();
      List<SlotResponse> views = new ArrayList<>();
      for (Slot slot : slots) {
        views.add(new SlotResponse(slot.getNo(), slot.isFree(), slot.displayCode()));
      }
      int free = parking.freeSlots();
      int capacity = parking.getCapacity();
      return new SlotMapResponse(capacity, free, capacity - free, views);
    }
  }

  @GetMapping("/summary")
  public SummaryResponse summary() throws StorageException {
    synchronized (parking) {
      int capacity = parking.getCapacity();
      int free = parking.freeSlots();
      List<TypeCount> byType = new ArrayList<>();
      for (VehicleType type : VehicleType.values()) {
        byType.add(new TypeCount(
            type.getCode(),
            type.getDisplayName(),
            parking.count(type)));
      }
      return new SummaryResponse(
          capacity,
          free,
          capacity - free,
          parking.parked(),
          byType,
          parking.ticketCount(),
          parking.total(),
          parking.todayTotal());
    }
  }

  @PostMapping("/tickets")
  @ResponseStatus(HttpStatus.CREATED)
  public TicketResponse park(@RequestBody ParkRequest request) throws Exception {
    VehicleType type = VehicleType.fromCode(request.vehicleType());
    String plate = validator.check(request.plate());
    Vehicle vehicle = type.createVehicle(plate, request.owner());
    return response(parking.park(vehicle));
  }

  @GetMapping("/tickets/{number}")
  public TicketResponse ticket(@PathVariable int number) throws InvalidTicketException {
    synchronized (parking) {
      Ticket ticket = parking.find(number);
      if (ticket == null) {
        throw new InvalidTicketException("No active ticket T-" + number + ".");
      }
      return response(ticket);
    }
  }

  @PostMapping("/tickets/{number}/exit")
  public TicketResponse exit(
      @PathVariable int number,
      @RequestBody ExitRequest request) throws Exception {
    return response(parking.exit(number, request.hours()));
  }

  private TicketResponse response(Ticket ticket) {
    Vehicle vehicle = ticket.getVehicle();
    return new TicketResponse(
        ticket.getNo(),
        ticket.number(),
        vehicle.getPlate(),
        vehicle.getOwner(),
        vehicle.getType().getCode(),
        vehicle.getType().getDisplayName(),
        ticket.getSlots(),
        ticket.getEntry().getTime(),
        ticket.getHours(),
        ticket.getCharge(),
        ticket.isActive());
  }
}
