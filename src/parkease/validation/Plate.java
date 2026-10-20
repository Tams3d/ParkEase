package parkease.validation;

import java.util.Locale;
import parkease.exception.InvalidPlateException;

public class Plate {

  public String check(String rawPlate) throws InvalidPlateException {
    if (rawPlate == null) {
      throw new InvalidPlateException("Number plate is required.");
    }

    StringBuilder builder = new StringBuilder();
    for (int index = 0; index < rawPlate.length(); index++) {
      char character = rawPlate.charAt(index);
      if (character != ' ' && character != '-' && character != '\t') {
        builder.append(character);
      }
    }

    String plate = builder.toString().toUpperCase(Locale.ENGLISH);
    int length = plate.length();
    if (length < 9 || length > 10) {
      throw new InvalidPlateException(
          "Number plate must be 9 or 10 characters, e.g. TN37AB1234.");
    }

    letters(plate, 0, 2, "state code");
    digits(plate, 2, 2, "RTO code");

    int series = length - 8;
    letters(plate, 4, series, "series");
    digits(plate, 4 + series, 4, "serial number");
    return plate;
  }

  private void letters(String plate, int start, int count, String field)
      throws InvalidPlateException {
    for (int index = start; index < start + count; index++) {
      if (!Character.isLetter(plate.charAt(index))) {
        throw new InvalidPlateException("Invalid plate: " + field + " must be letters.");
      }
    }
  }

  private void digits(String plate, int start, int count, String field)
      throws InvalidPlateException {
    for (int index = start; index < start + count; index++) {
      if (!Character.isDigit(plate.charAt(index))) {
        throw new InvalidPlateException("Invalid plate: " + field + " must be digits.");
      }
    }
  }
}
