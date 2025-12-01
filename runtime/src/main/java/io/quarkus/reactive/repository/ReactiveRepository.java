package io.quarkus.reactive.repository;

import io.quarkus.reactive.repository.types.Identifiable;
import io.quarkus.reactive.repository.types.Param;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.function.Function;
import org.hibernate.reactive.mutiny.Mutiny;

/**
 * A generic interface for a reactive repository that provides methods for performing CRUD
 * operations and transactional operations on entities.
 *
 * @param <Entity> The type of the entity, which must extend {@link Identifiable}.
 */
public interface ReactiveRepository<Entity extends Identifiable<?>> {

  /**
   * Finds an entity by its identifier.
   *
   * @param id The identifier of the entity.
   * @return A {@link Uni} containing the entity if found, or null if not found.
   */
  public Uni<Entity> findById(final Object id);

  /**
   * Persists an entity within a transaction.
   *
   * @param entity The entity to persist.
   * @return A {@link Uni} containing the persisted entity.
   */
  public Uni<Entity> persistWithTransaction(final Entity entity);

  /**
   * Persists a list of entities within a transaction.
   *
   * @param entities The list of entities to persist.
   * @return A {@link Uni} containing the list of persisted entities.
   */
  public Uni<List<Entity>> persistAllWithTransaction(List<Entity> entities);

  /**
   * Persists multiple entities within a transaction.
   *
   * @param entities The entities to persist.
   * @return A {@link Uni} containing the list of persisted entities.
   */
  public Uni<List<Entity>> persistAllWithTransaction(Entity... entities);

  /**
   * Deletes an entity within a transaction.
   *
   * @param entity The entity to delete.
   * @return A {@link Uni} representing the completion of the operation.
   */
  public Uni<Void> deleteWithTransaction(final Entity entity);

  /**
   * Deletes a list of entities within a transaction.
   *
   * @param entities The list of entities to delete.
   * @return A {@link Uni} representing the completion of the operation.
   */
  public Uni<Void> deleteAllWithTransaction(List<Entity> entities);

  /**
   * Deletes multiple entities within a transaction.
   *
   * @param entities The entities to delete.
   * @return A {@link Uni} representing the completion of the operation.
   */
  public Uni<Void> deleteAllWithTransaction(Entity... entities);

  /**
   * Executes a function within a transaction.
   *
   * @param <R> The type of the result returned by the function.
   * @param consumer The function to execute, which takes a {@link Mutiny.Session}.
   * @return A {@link Uni} containing the result of the function.
   */
  public <R> Uni<R> withTransaction(final Function<Mutiny.Session, Uni<R>> consumer);

  /**
   * Executes a function with a session.
   *
   * @param <R> The type of the result returned by the function.
   * @param consumer The function to execute, which takes a {@link Mutiny.Session}.
   * @return A {@link Uni} containing the result of the function.
   */
  public <R> Uni<R> withSession(final Function<Mutiny.Session, Uni<R>> consumer);

  /**
   * Executes a selection query with a consumer function.
   *
   * @param <R> The type of the result returned by the consumer function.
   * @param query The selection query string.
   * @param selectionQueryConsumer The function to process the {@link Mutiny.SelectionQuery}.
   * @return A {@link Uni} containing the result of the consumer function.
   */
  public <R> Uni<R> withSelectionQuery(
      final String query,
      final Function<Mutiny.SelectionQuery<Entity>, Uni<R>> selectionQueryConsumer);

  /**
   * Retrieves the name of the current entity.
   *
   * @return The name of the entity as a {@link String}.
   */
  public String currentEntityName();

  /**
   * Executes a query and selects multiple entities.
   *
   * @param query The query string.
   * @param params The parameters for the query.
   * @return A {@link Uni} containing a list of selected entities.
   */
  public Uni<List<Entity>> selectMultiple(String query, Param... params);

  /**
   * Executes a query and selects a single entity.
   *
   * @param query The query string.
   * @param params The parameters for the query.
   * @return A {@link Uni} containing the selected entity.
   */
  public Uni<Entity> select(String query, Param... params);

  /**
   * Checks if the given entity exists in the repository.
   *
   * @param entity The entity to check for existence.
   * @return A {@link Uni} containing a {@link Boolean} value indicating whether the entity exists.
   */
  public Uni<Boolean> exists(final Entity entity);

  /**
   * Checks if the given entity exists in the repository by id.
   *
   * @param id The id of the entity to check for existence.
   * @return A {@link Uni} containing a {@link Boolean} value indicating whether the entity exists.
   */
  public Uni<Boolean> exists(final Object id);
}
