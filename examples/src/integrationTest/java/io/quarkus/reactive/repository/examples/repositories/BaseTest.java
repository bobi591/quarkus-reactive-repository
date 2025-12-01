package io.quarkus.reactive.repository.examples.repositories;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class BaseTest {
  public Duration awaitDuration() {
    return Duration.of(5, ChronoUnit.SECONDS);
  }
}
