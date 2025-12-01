package io.quarkus.reactive.repository.examples.entities;

import io.quarkus.reactive.repository.types.Identifiable;
import jakarta.persistence.Entity;
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
