package parkease.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import parkease.exception.ApiException;
import parkease.model.Ticket;
import parkease.model.Vehicle;
import parkease.model.VehicleType;

public class RemoteParkingClient implements ParkingClient {

  private final HttpClient http = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(5))
      .build();
  private final ObjectMapper mapper = new ObjectMapper()
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  private final String baseUrl;

  public RemoteParkingClient(String baseUrl) {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalArgumentException("Backend URL is required.");
    }
    String value = baseUrl.trim();
    this.baseUrl = value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
  }

  @Override
  public int capacity() throws ApiException {
    return read("/api/slots", SlotMapResponse.class).capacity();
  }

  @Override
  public Ticket park(Vehicle vehicle) throws ApiException {
    Object body = Map.of(
        "vehicleType", vehicle.getType().getCode(),
        "plate", vehicle.getPlate(),
        "owner", vehicle.getOwner());
    ApiResponse response = send("POST", "/api/tickets", body);
    return toTicket(decode(response, TicketResponse.class));
  }

  @Override
  public Ticket exit(int number, double hours) throws ApiException {
    Object body = Map.of("hours", hours);
    ApiResponse response = send("POST", "/api/tickets/" + number + "/exit", body);
    return toTicket(decode(response, TicketResponse.class));
  }

  @Override
  public Ticket find(int number) throws ApiException {
    ApiResponse response = send("GET", "/api/tickets/" + number, null);
    if (response.status() == 404) {
      return null;
    }
    return toTicket(decode(response, TicketResponse.class));
  }

  @Override
  public SlotMap slots() throws ApiException {
    SlotMapResponse response = read("/api/slots", SlotMapResponse.class);
    List<SlotView> views = new ArrayList<>();
    for (SlotResponse slot : response.slots()) {
      views.add(new SlotView(slot.number(), slot.free(), slot.display()));
    }
    return new SlotMap(response.capacity(), response.free(), response.occupied(), views);
  }

  @Override
  public ParkingSummary summary() throws ApiException {
    SummaryResponse response = read("/api/summary", SummaryResponse.class);
    return new ParkingSummary(
        response.capacity(),
        response.free(),
        response.occupied(),
        response.parked(),
        response.byType(),
        response.ticketCount(),
        response.total(),
        response.today());
  }

  private <T> T read(String path, Class<T> type) throws ApiException {
    return decode(send("GET", path, null), type);
  }

  private <T> T decode(ApiResponse response, Class<T> type) throws ApiException {
    if (response.status() < 200 || response.status() >= 300) {
      throw error(response);
    }
    try {
      return mapper.readValue(response.body(), type);
    } catch (Exception error) {
      throw new ApiException("Could not read the backend response.", error);
    }
  }

  private ApiResponse send(String method, String path, Object body) throws ApiException {
    try {
      HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + path))
          .timeout(Duration.ofSeconds(15))
          .header("Accept", "application/json");
      if (body == null) {
        request.method(method, HttpRequest.BodyPublishers.noBody());
      } else {
        request.header("Content-Type", "application/json");
        request.method(
            method,
            HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
      }

      HttpResponse<String> response = http.send(
          request.build(),
          HttpResponse.BodyHandlers.ofString());
      return new ApiResponse(response.statusCode(), response.body());
    } catch (InterruptedException error) {
      Thread.currentThread().interrupt();
      throw new ApiException("The backend request was interrupted.", error);
    } catch (Exception error) {
      throw new ApiException("Could not reach the backend at " + baseUrl + ".", error);
    }
  }

  private ApiException error(ApiResponse response) {
    try {
      ApiError error = mapper.readValue(response.body(), ApiError.class);
      if (error.message() != null && !error.message().isBlank()) {
        return new ApiException(error.message());
      }
    } catch (Exception ignored) {
    }
    return new ApiException(
        "Backend request failed with HTTP " + response.status() + ".");
  }

  private Ticket toTicket(TicketResponse response) throws ApiException {
    try {
      VehicleType type = VehicleType.fromCode(response.vehicleType());
      Vehicle vehicle = type.createVehicle(response.plate(), response.owner());
      Ticket ticket = new Ticket(
          response.ticketNo(),
          vehicle,
          response.slots(),
          new Date(response.entryMillis()));
      if (!response.active()) {
        ticket.close(response.hours(), response.charge());
      }
      return ticket;
    } catch (Exception error) {
      throw new ApiException("Could not read the ticket from the backend.", error);
    }
  }

  private record ApiResponse(int status, String body) {
  }

  private record ApiError(String message) {
  }

  private record TicketResponse(
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

  private record SlotResponse(int number, boolean free, String display) {
  }

  private record SlotMapResponse(
      int capacity,
      int free,
      int occupied,
      List<SlotResponse> slots) {
  }

  private record SummaryResponse(
      int capacity,
      int free,
      int occupied,
      int parked,
      List<TypeCount> byType,
      int ticketCount,
      int total,
      int today) {
  }
}
