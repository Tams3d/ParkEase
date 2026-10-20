
CREATE DATABASE IF NOT EXISTS parkease
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE parkease;

CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_number VARCHAR(15) NOT NULL,
    owner_name     VARCHAR(60) NOT NULL,
    vehicle_type   VARCHAR(10) NOT NULL,
    PRIMARY KEY (vehicle_number),
    CONSTRAINT chk_vehicle_type
        CHECK (vehicle_type IN ('BIKE', 'CAR', 'TRUCK'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS tickets (
    ticket_no     INT          NOT NULL,
    vehicle_number VARCHAR(15) NOT NULL,
    entry_time    DATETIME     NOT NULL,
    exit_time     DATETIME     NULL,
    hours_parked  INT          NULL,
    charge        INT          NULL,
    status        VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (ticket_no),
    INDEX idx_tickets_vehicle_status (vehicle_number, status),
    INDEX idx_tickets_status (status),
    CONSTRAINT fk_tickets_vehicle
        FOREIGN KEY (vehicle_number) REFERENCES vehicles (vehicle_number),
    CONSTRAINT chk_ticket_status
        CHECK (status IN ('ACTIVE', 'CLOSED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS slots (
    slot_no   INT NOT NULL,
    ticket_no INT NULL,
    PRIMARY KEY (slot_no),
    INDEX idx_slots_ticket (ticket_no),
    CONSTRAINT fk_slots_ticket
        FOREIGN KEY (ticket_no) REFERENCES tickets (ticket_no)
        ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT IGNORE INTO slots (slot_no) VALUES
    (1), (2), (3), (4), (5), (6), (7), (8), (9), (10),
    (11), (12), (13), (14), (15), (16), (17), (18), (19), (20);
