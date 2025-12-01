package io.quarkus.reactive.repository.examples.repositories;

import io.quarkus.reactive.repository.ReactiveRepository;
import io.quarkus.reactive.repository.annotations.bean.ReactiveRepositoryBean;
import io.quarkus.reactive.repository.annotations.query.Query;
import io.quarkus.reactive.repository.annotations.query.QueryParam;
import io.quarkus.reactive.repository.examples.entities.User;
import io.quarkus.reactive.repository.types.Param;
import io.smallrye.mutiny.Uni;
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
