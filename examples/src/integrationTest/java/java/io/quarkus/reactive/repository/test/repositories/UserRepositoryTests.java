package java.io.quarkus.reactive.repository.test.repositories;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.quarkus.reactive.repository.test.entities.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class UserRepositoryTests extends BaseTest {
  @Inject private UserRepository userRepository;

  @Test
  public void testAddOneEntity() {
    final User user = User.builder().firstName("Boris").lastName("Georgiev").build();
    userRepository
        .persistWithTransaction(user)
        .invoke(persisted -> assertNotNull(persisted.getId()))
        .call(persisted -> userRepository.deleteWithTransaction(persisted))
        .chain(deleted -> userRepository.exists(deleted))
        .invoke(Assertions::assertFalse)
        .await()
        .atMost(awaitDuration());
  }
}
