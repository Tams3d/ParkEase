package parkease.client;

import java.util.List;

public record SlotMap(
    int capacity,
    int free,
    int occupied,
    List<SlotView> slots) {
}
