package parkease.api;

import java.util.List;

public final class ApiDtos {

  private ApiDtos() {
  }

  public record ParkRequest(String vehicleType, String plate, String owner) {
  }

  public record ExitRequest(double hours) {
  }

  public record TicketResponse(
      int ticketNo,
      String number,
      String plate,
      String owner,
      String vehicleType,
      String vehicleTypeName,
      int[] slots,
      long entryMillis,
      int hours,
      int charge,
      boolean active) {
  }

  public record SlotResponse(int number, boolean free, String display) {
  }

  public record SlotMapResponse(
      int capacity,
      int free,
      int occupied,
      List<SlotResponse> slots) {
  }

  public record TypeCount(String code, String name, int count) {
  }

  public record SummaryResponse(
      int capacity,
      int free,
      int occupied,
      int parked,
      List<TypeCount> byType,
      int ticketCount,
      int total,
      int today) {
  }

  public record VehicleTypeResponse(String code, String name, int rate, int slots) {
  }

  public record ApiError(String message) {
  }
}
