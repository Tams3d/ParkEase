package parkease.client;

import java.util.List;

public record ParkingSummary(
    int capacity,
    int free,
    int occupied,
    int parked,
    List<TypeCount> byType,
    int ticketCount,
    int total,
    int today) {
}
