package io.quarkus.reactive.repository.examples.entities;

import io.quarkus.reactive.repository.types.Identifiable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "examples_users")
public class User implements Identifiable<Long> {
  private @Id @GeneratedValue Long id;

  private String firstName;
  private String lastName;
}
