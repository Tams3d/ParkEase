package parkease.model;

import parkease.exception.InvalidHoursException;

public interface Chargeable {

  int dailyCapHours = 8;

  int calculateCharge(int hours) throws InvalidHoursException;
}
