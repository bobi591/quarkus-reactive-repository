package java.io.quarkus.reactive.repository.types;

public class Param {
  private final String name;
  private final Object value;

  public Param(String name, Object value) {
    this.name = name;
    this.value = value;
  }

  public String name() {
    return name;
  }

  public Object value() {
    return value;
  }

  public static Param of(String name, Object value) {
    return new Param(name, value);
  }
}
