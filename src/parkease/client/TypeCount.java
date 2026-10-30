package parkease.client;

public class TypeCount {

  private final String code;
  private final String name;
  private final int count;

  public TypeCount(String code, String name, int count) {
    this.code = code;
    this.name = name;
    this.count = count;
  }

  public String code() {
    return code;
  }

  public String name() {
    return name;
  }

  public int count() {
    return count;
  }
}
