package java.io.quarkus.reactive.repository.test.repositories;

import io.smallrye.mutiny.Uni;
import java.io.quarkus.reactive.repository.ReactiveRepository;
import java.io.quarkus.reactive.repository.annotations.bean.ReactiveRepositoryBean;
import java.io.quarkus.reactive.repository.annotations.query.Query;
import java.io.quarkus.reactive.repository.annotations.query.QueryParam;
import java.io.quarkus.reactive.repository.test.entities.User;
import java.io.quarkus.reactive.repository.types.Param;
import java.util.List;

@ReactiveRepositoryBean
public interface UserRepository extends ReactiveRepository<User> {
  // Example with method operations
  default Uni<List<User>> getUsersWhereName(final String firstName) {
    return selectMultiple(
        "from User u where u.firstName = :firstName", Param.of("firstName", firstName));
  }

  // Example with query annotation
  @Query("from User u where u.lastName = :lastName")
  public Uni<List<User>> getUsersWhereLastName(final @QueryParam("lastName") String lastName);
}
