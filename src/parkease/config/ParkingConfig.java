package parkease.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import parkease.exception.StorageException;
import parkease.service.ParkingLot;
import parkease.storage.Storage;

@Configuration
public class ParkingConfig {

  @Bean
  public ParkingLot parkingLot() throws StorageException {
    return new ParkingLot(new Storage());
  }
}
