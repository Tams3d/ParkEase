package parkease.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import parkease.exception.StorageException;
import parkease.model.Ticket;
import parkease.model.Vehicle;
import parkease.model.VehicleType;

public class Storage {

  private final String dateFormat = "yyyy-MM-dd HH:mm:ss";
  private final TimeZone indiaZone = TimeZone.getTimeZone("Asia/Kolkata");
  private final Database database = new Database();

  public void init(int capacity) throws StorageException {
    String vehicles =
        "CREATE TABLE IF NOT EXISTS vehicles ("
            + " vehicle_number VARCHAR(20) PRIMARY KEY,"
            + " owner_name VARCHAR(100) NOT NULL,"
            + " vehicle_type VARCHAR(20) NOT NULL"
            + ") DEFAULT CHARSET=utf8mb4";
    String tickets =
        "CREATE TABLE IF NOT EXISTS tickets ("
            + " ticket_no INT PRIMARY KEY,"
            + " vehicle_number VARCHAR(20) NOT NULL,"
            + " entry_time DATETIME NOT NULL,"
            + " exit_time DATETIME,"
            + " hours_parked INT,"
            + " charge INT,"
            + " status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE'"
            + ") DEFAULT CHARSET=utf8mb4";
    String slots =
        "CREATE TABLE IF NOT EXISTS slots ("
            + " slot_no INT PRIMARY KEY,"
            + " ticket_no INT"
            + ") DEFAULT CHARSET=utf8mb4";

    try (Connection connection = database.connect();
        Statement statement = connection.createStatement()) {
      statement.execute(vehicles);
      statement.execute(tickets);
      statement.execute(slots);

      String seed = "INSERT IGNORE INTO slots (slot_no) VALUES (?)";
      try (PreparedStatement prepared = connection.prepareStatement(seed)) {
        for (int slot = 1; slot <= capacity; slot++) {
          prepared.setInt(1, slot);
          prepared.executeUpdate();
        }
      }
    } catch (Exception error) {
      throw new StorageException("Could not create the tables: " + error.getMessage(), error);
    }
  }

  public int lastNo() throws StorageException {
    String query = "SELECT COALESCE(MAX(ticket_no), 1000) FROM tickets";
    try (Connection connection = database.connect();
        PreparedStatement prepared = connection.prepareStatement(query);
        ResultSet results = prepared.executeQuery()) {
      if (results.next()) {
        return results.getInt(1);
      }
      return 1000;
    } catch (Exception error) {
      throw new StorageException("Could not read the last ticket number.", error);
    }
  }

  public List<Ticket> active() throws StorageException {
    String query =
        "SELECT t.ticket_no, t.vehicle_number, v.owner_name, v.vehicle_type,"
            + " GROUP_CONCAT(s.slot_no) AS slot_list, t.entry_time"
            + " FROM tickets t"
            + " JOIN vehicles v ON v.vehicle_number = t.vehicle_number"
            + " JOIN slots s ON s.ticket_no = t.ticket_no"
            + " WHERE t.status = 'ACTIVE'"
            + " GROUP BY t.ticket_no, t.vehicle_number, v.owner_name, v.vehicle_type, t.entry_time"
            + " ORDER BY t.ticket_no";
    List<Ticket> list = new ArrayList<>();

    try (Connection connection = database.connect();
        PreparedStatement prepared = connection.prepareStatement(query);
        ResultSet results = prepared.executeQuery()) {
      while (results.next()) {
        int number = results.getInt("ticket_no");
        String plate = results.getString("vehicle_number");
        String owner = results.getString("owner_name");
        VehicleType type = VehicleType.fromCode(results.getString("vehicle_type"));
        Vehicle vehicle = type.createVehicle(plate, owner);

        String[] parts = results.getString("slot_list").split(",");
        int[] slots = new int[parts.length];
        for (int index = 0; index < parts.length; index++) {
          slots[index] = Integer.parseInt(parts[index].trim());
        }
        Arrays.sort(slots);

        Date entry = parse(results.getString("entry_time"));
        list.add(new Ticket(number, vehicle, slots, entry));
      }
      return list;
    } catch (Exception error) {
      throw new StorageException("Could not load active tickets.", error);
    }
  }

  public void save(Ticket ticket) throws StorageException {
    try (Connection connection = database.connect()) {
      String vehicleQuery =
          "INSERT INTO vehicles (vehicle_number, owner_name, vehicle_type) VALUES (?, ?, ?)"
              + " ON DUPLICATE KEY UPDATE"
              + " owner_name = VALUES(owner_name), vehicle_type = VALUES(vehicle_type)";
      try (PreparedStatement prepared = connection.prepareStatement(vehicleQuery)) {
        prepared.setString(1, ticket.getVehicle().getPlate());
        prepared.setString(2, ticket.getVehicle().getOwner());
        prepared.setString(3, ticket.getVehicle().getType().getCode());
        prepared.executeUpdate();
      }

      String ticketQuery =
          "INSERT INTO tickets (ticket_no, vehicle_number, entry_time, status)"
              + " VALUES (?, ?, ?, 'ACTIVE')";
      try (PreparedStatement prepared = connection.prepareStatement(ticketQuery)) {
        prepared.setInt(1, ticket.getNo());
        prepared.setString(2, ticket.getVehicle().getPlate());
        prepared.setString(3, date(ticket.getEntry()));
        prepared.executeUpdate();
      }

      String slotQuery = "UPDATE slots SET ticket_no = ? WHERE slot_no = ?";
      try (PreparedStatement prepared = connection.prepareStatement(slotQuery)) {
        for (int slot : ticket.getSlots()) {
          prepared.setInt(1, ticket.getNo());
          prepared.setInt(2, slot);
          prepared.executeUpdate();
        }
      }
    } catch (Exception error) {
      throw new StorageException("Could not save the ticket: " + error.getMessage(), error);
    }
  }

  public void close(Ticket ticket, int hours, int charge, Date exitTime)
      throws StorageException {
    try (Connection connection = database.connect()) {
      String ticketQuery =
          "UPDATE tickets SET exit_time = ?, hours_parked = ?, charge = ?, status = 'CLOSED'"
              + " WHERE ticket_no = ?";
      try (PreparedStatement prepared = connection.prepareStatement(ticketQuery)) {
        prepared.setString(1, date(exitTime));
        prepared.setInt(2, hours);
        prepared.setInt(3, charge);
        prepared.setInt(4, ticket.getNo());
        prepared.executeUpdate();
      }

      String slotQuery = "UPDATE slots SET ticket_no = NULL WHERE ticket_no = ?";
      try (PreparedStatement prepared = connection.prepareStatement(slotQuery)) {
        prepared.setInt(1, ticket.getNo());
        prepared.executeUpdate();
      }
    } catch (Exception error) {
      throw new StorageException("Could not close the ticket: " + error.getMessage(), error);
    }
  }

  public int total() throws StorageException {
    return readCount(
        "SELECT COALESCE(SUM(charge), 0) FROM tickets WHERE status = 'CLOSED'",
        "total collection");
  }

  public int todayTotal() throws StorageException {
    String query =
        "SELECT COALESCE(SUM(charge), 0) FROM tickets"
            + " WHERE status = 'CLOSED' AND substr(exit_time, 1, 10) = ?";
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    formatter.setTimeZone(indiaZone);
    String today = formatter.format(new Date());

    try (Connection connection = database.connect();
        PreparedStatement prepared = connection.prepareStatement(query)) {
      prepared.setString(1, today);
      try (ResultSet results = prepared.executeQuery()) {
        if (results.next()) {
          return results.getInt(1);
        }
        return 0;
      }
    } catch (Exception error) {
      throw new StorageException("Could not read today's collection.", error);
    }
  }

  public int ticketCount() throws StorageException {
    return readCount("SELECT COUNT(*) FROM tickets", "ticket count");
  }

  private int readCount(String query, String name) throws StorageException {
    try (Connection connection = database.connect();
        PreparedStatement prepared = connection.prepareStatement(query);
        ResultSet results = prepared.executeQuery()) {
      if (results.next()) {
        return results.getInt(1);
      }
      return 0;
    } catch (Exception error) {
      throw new StorageException("Could not read " + name + ".", error);
    }
  }

  private String date(Date value) {
    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
    formatter.setTimeZone(indiaZone);
    return formatter.format(value);
  }

  private Date parse(String text) throws StorageException {
    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
    formatter.setTimeZone(indiaZone);
    try {
      return formatter.parse(text);
    } catch (Exception error) {
      throw new StorageException("Could not read a ticket time.", error);
    }
  }
}
