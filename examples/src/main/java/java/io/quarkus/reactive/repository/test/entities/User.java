package java.io.quarkus.reactive.repository.test.entities;

import jakarta.persistence.Entity;
import java.io.quarkus.reactive.repository.types.Identifiable;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
public class User implements Identifiable<Long> {
  private Long id;

  private String firstName;
  private String lastName;
}
