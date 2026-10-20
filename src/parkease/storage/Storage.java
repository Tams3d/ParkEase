package parkease.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

  private final String vehiclesSql =
      "CREATE TABLE IF NOT EXISTS vehicles ("
          + " vehicle_number VARCHAR(15) NOT NULL,"
          + " owner_name VARCHAR(60) NOT NULL,"
          + " vehicle_type VARCHAR(10) NOT NULL,"
          + " PRIMARY KEY (vehicle_number),"
          + " CONSTRAINT chk_vehicle_type"
          + " CHECK (vehicle_type IN ('BIKE', 'CAR', 'TRUCK'))"
          + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

  private final String ticketsSql =
      "CREATE TABLE IF NOT EXISTS tickets ("
          + " ticket_no INT NOT NULL,"
          + " vehicle_number VARCHAR(15) NOT NULL,"
          + " entry_time DATETIME NOT NULL,"
          + " exit_time DATETIME NULL,"
          + " hours_parked INT NULL,"
          + " charge INT NULL,"
          + " status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',"
          + " PRIMARY KEY (ticket_no),"
          + " INDEX idx_tickets_vehicle_status (vehicle_number, status),"
          + " INDEX idx_tickets_status (status),"
          + " CONSTRAINT fk_tickets_vehicle"
          + " FOREIGN KEY (vehicle_number) REFERENCES vehicles(vehicle_number),"
          + " CONSTRAINT chk_ticket_status"
          + " CHECK (status IN ('ACTIVE', 'CLOSED'))"
          + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

  private final String slotsSql =
      "CREATE TABLE IF NOT EXISTS slots ("
          + " slot_no INT NOT NULL,"
          + " ticket_no INT NULL,"
          + " PRIMARY KEY (slot_no),"
          + " INDEX idx_slots_ticket (ticket_no),"
          + " CONSTRAINT fk_slots_ticket FOREIGN KEY (ticket_no)"
          + " REFERENCES tickets(ticket_no) ON DELETE SET NULL"
          + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

  public void init(int capacity) throws StorageException {
    try (Connection connection = database.connect();
        Statement statement = connection.createStatement()) {
      statement.execute(vehiclesSql);
      statement.execute(ticketsSql);
      statement.execute(slotsSql);
      seed(connection, capacity);
    } catch (Exception error) {
      throw new StorageException("Could not create the tables: " + error.getMessage(), error);
    }
  }

  private void seed(Connection connection, int capacity) throws Exception {
    String query = "INSERT IGNORE INTO slots (slot_no) VALUES (?)";
    try (PreparedStatement prepared = connection.prepareStatement(query)) {
      for (int slot = 1; slot <= capacity; slot++) {
        prepared.setInt(1, slot);
        prepared.addBatch();
      }
      prepared.executeBatch();
    }
  }

  public int lastNo() throws StorageException {
    String query = "SELECT COALESCE(MAX(ticket_no), 1000) FROM tickets";
    try (Connection connection = database.connect();
        PreparedStatement prepared = connection.prepareStatement(query);
        ResultSet results = prepared.executeQuery()) {
      return results.next() ? results.getInt(1) : 1000;
    } catch (Exception error) {
      throw new StorageException("Could not read the last ticket number.", error);
    }
  }

  public List<Ticket> active() throws StorageException {
    String query =
        "SELECT ticketTable.ticket_no, ticketTable.vehicle_number,"
            + " vehicleTable.owner_name, vehicleTable.vehicle_type,"
            + " GROUP_CONCAT(slotTable.slot_no ORDER BY slotTable.slot_no) AS slot_list,"
            + " ticketTable.entry_time"
            + " FROM tickets ticketTable"
            + " JOIN vehicles vehicleTable"
            + " ON vehicleTable.vehicle_number = ticketTable.vehicle_number"
            + " JOIN slots slotTable ON slotTable.ticket_no = ticketTable.ticket_no"
            + " WHERE ticketTable.status = 'ACTIVE'"
            + " GROUP BY ticketTable.ticket_no, ticketTable.vehicle_number,"
            + " vehicleTable.owner_name, vehicleTable.vehicle_type,"
            + " ticketTable.entry_time"
            + " ORDER BY ticketTable.ticket_no";
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

        String joined = results.getString("slot_list");
        String[] parts = joined.split(",");
        int[] slots = new int[parts.length];
        for (int index = 0; index < parts.length; index++) {
          slots[index] = Integer.parseInt(parts[index]);
        }

        Date entry = parse(results.getString("entry_time"));
        list.add(new Ticket(number, vehicle, slots, entry));
      }
      return list;
    } catch (Exception error) {
      throw new StorageException("Could not load active tickets.", error);
    }
  }

  public boolean parked(String plate) throws StorageException {
    String query = "SELECT 1 FROM tickets WHERE vehicle_number = ? AND status = 'ACTIVE'";
    try (Connection connection = database.connect();
        PreparedStatement prepared = connection.prepareStatement(query)) {
      prepared.setString(1, plate);
      try (ResultSet results = prepared.executeQuery()) {
        return results.next();
      }
    } catch (Exception error) {
      throw new StorageException("Could not check the vehicle number.", error);
    }
  }

  public void save(Ticket ticket) throws StorageException {
    try (Connection connection = database.connect()) {
      connection.setAutoCommit(false);
      try {
        saveVehicle(connection, ticket.getVehicle());
        insert(connection, ticket);
        take(connection, ticket.getNo(), ticket.getSlots());
        connection.commit();
      } catch (Exception error) {
        rollback(connection, error);
        throw error;
      }
    } catch (Exception error) {
      throw new StorageException("Could not save the ticket: " + error.getMessage(), error);
    }
  }

  private void saveVehicle(Connection connection, Vehicle vehicle) throws Exception {
    String query =
        "INSERT INTO vehicles (vehicle_number, owner_name, vehicle_type)"
            + " VALUES (?, ?, ?)"
            + " ON DUPLICATE KEY UPDATE owner_name = ?, vehicle_type = ?";
    String owner = vehicle.getOwner();
    String code = vehicle.getType().getCode();
    try (PreparedStatement prepared = connection.prepareStatement(query)) {
      prepared.setString(1, vehicle.getPlate());
      prepared.setString(2, owner);
      prepared.setString(3, code);
      prepared.setString(4, owner);
      prepared.setString(5, code);
      prepared.executeUpdate();
    }
  }

  private void insert(Connection connection, Ticket ticket) throws Exception {
    String query =
        "INSERT INTO tickets (ticket_no, vehicle_number, entry_time, status)"
            + " VALUES (?, ?, ?, 'ACTIVE')";
    try (PreparedStatement prepared = connection.prepareStatement(query)) {
      prepared.setInt(1, ticket.getNo());
      prepared.setString(2, ticket.getVehicle().getPlate());
      prepared.setString(3, date(ticket.getEntry()));
      prepared.executeUpdate();
    }
  }

  private void take(Connection connection, int number, int[] slots) throws Exception {
    String query = "UPDATE slots SET ticket_no = ? WHERE slot_no = ? AND ticket_no IS NULL";
    try (PreparedStatement prepared = connection.prepareStatement(query)) {
      for (int slot : slots) {
        prepared.setInt(1, number);
        prepared.setInt(2, slot);
        if (prepared.executeUpdate() != 1) {
          throw new StorageException("Slot " + slot + " is not free in the database.");
        }
      }
    }
  }

  public void close(Ticket ticket, int hours, int charge, Date exitTime)
      throws StorageException {
    try (Connection connection = database.connect()) {
      connection.setAutoCommit(false);
      try {
        closeRow(connection, ticket.getNo(), hours, charge, exitTime);
        free(connection, ticket);
        connection.commit();
      } catch (Exception error) {
        rollback(connection, error);
        throw error;
      }
    } catch (Exception error) {
      throw new StorageException("Could not close the ticket: " + error.getMessage(), error);
    }
  }

  private void closeRow(
      Connection connection, int number, int hours, int charge, Date exitTime)
      throws Exception {
    String query =
        "UPDATE tickets SET exit_time = ?, hours_parked = ?, charge = ?, status = 'CLOSED'"
            + " WHERE ticket_no = ? AND status = 'ACTIVE'";
    try (PreparedStatement prepared = connection.prepareStatement(query)) {
      prepared.setString(1, date(exitTime));
      prepared.setInt(2, hours);
      prepared.setInt(3, charge);
      prepared.setInt(4, number);
      if (prepared.executeUpdate() != 1) {
        throw new StorageException("Ticket T-" + number + " is not active.");
      }
    }
  }

  private void free(Connection connection, Ticket ticket) throws Exception {
    String query = "UPDATE slots SET ticket_no = NULL WHERE ticket_no = ?";
    try (PreparedStatement prepared = connection.prepareStatement(query)) {
      prepared.setInt(1, ticket.getNo());
      int count = prepared.executeUpdate();
      if (count != ticket.getSlots().length) {
        throw new StorageException("Could not free all slots for this ticket.");
      }
    }
  }

  private void rollback(Connection connection, Exception first) {
    try {
      connection.rollback();
    } catch (Exception error) {
      first.addSuppressed(error);
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
            + " WHERE status = 'CLOSED' AND DATE(exit_time) = ?";
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    formatter.setTimeZone(indiaZone);
    String today = formatter.format(new Date());

    try (Connection connection = database.connect();
        PreparedStatement prepared = connection.prepareStatement(query)) {
      prepared.setString(1, today);
      try (ResultSet results = prepared.executeQuery()) {
        return results.next() ? results.getInt(1) : 0;
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
      return results.next() ? results.getInt(1) : 0;
    } catch (Exception error) {
      throw new StorageException("Could not read " + name + ".", error);
    }
  }

  private String date(Date dateValue) {
    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
    formatter.setLenient(false);
    formatter.setTimeZone(indiaZone);
    return formatter.format(dateValue);
  }

  private Date parse(String text) throws StorageException {
    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
    formatter.setLenient(false);
    formatter.setTimeZone(indiaZone);
    try {
      return formatter.parse(text);
    } catch (Exception error) {
      throw new StorageException("Could not read a ticket time.", error);
    }
  }
}
