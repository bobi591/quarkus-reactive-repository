package io.quarkus.reactive.repository;

import static java.util.Objects.isNull;

import io.quarkus.reactive.repository.types.Identifiable;
import io.quarkus.reactive.repository.types.Param;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.metamodel.EntityType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.hibernate.reactive.mutiny.Mutiny;

public class BaseReactiveRepository<Entity extends Identifiable<?>>
    implements ReactiveRepository<Entity> {
  private final Mutiny.SessionFactory sf;
  private final Class<Entity> entityType;

  public BaseReactiveRepository() {
    this.sf = null;
    this.entityType = null;
  }

  public BaseReactiveRepository(final Mutiny.SessionFactory sf, final Class<Entity> entityType) {
    this.sf = sf;
    this.entityType = entityType;
  }

  @Override
  public Uni<Entity> findById(final Object id) {
    return sf.withSession(session -> session.find(entityType, id));
  }

  @Override
  public Uni<Entity> persistWithTransaction(final Entity entity) {
    final boolean isNew = isNull(entity.getId());
    if (isNew) {
      return sf.withTransaction(session -> session.persist(entity).replaceWith(entity));
    } else {
      return withTransaction(session -> session.merge(entity));
    }
  }

  @Override
  public Uni<List<Entity>> persistAllWithTransaction(List<Entity> entities) {
    return withTransaction(
        session ->
            Multi.createFrom()
                .iterable(entities)
                .onItem()
                .transformToUniAndMerge(this::persistWithTransaction)
                .collect()
                .asList());
  }

  @Override
  @SafeVarargs
  public final Uni<List<Entity>> persistAllWithTransaction(final Entity... entities) {
    return withTransaction(
        session ->
            Multi.createFrom()
                .items(entities)
                .onItem()
                .transformToUniAndMerge(this::persistWithTransaction)
                .collect()
                .asList());
  }

  @Override
  public Uni<Void> deleteWithTransaction(Entity entity) {
    return withTransaction(
        session ->
            findById(entity.getId())
                .onItem()
                .ifNotNull()
                .transformToUni(
                    existingEntity ->
                        withTransaction(deleteSession -> deleteSession.remove(existingEntity)))
                .replaceWithVoid());
  }

  @Override
  public Uni<Void> deleteAllWithTransaction(List<Entity> entities) {
    return withTransaction(
        session ->
            Multi.createFrom()
                .iterable(entities)
                .onItem()
                .transformToUniAndMerge(e -> findById(e.getId()))
                .filter(entity -> !isNull(entity))
                .collect()
                .asList()
                .chain(
                    existingEntities -> {
                      if (existingEntities.isEmpty()) {
                        return Uni.createFrom().voidItem();
                      }
                      return withTransaction(
                              deleteSession -> deleteSession.removeAll(existingEntities))
                          .replaceWithVoid();
                    }));
  }

  @Override
  public Uni<Void> deleteAllWithTransaction(Entity... entities) {
    return withTransaction(
        session ->
            Multi.createFrom()
                .items(entities)
                .onItem()
                .transformToUniAndMerge(e -> findById(e.getId()))
                .filter(entity -> !isNull(entity))
                .collect()
                .asList()
                .chain(
                    existingEntities -> {
                      if (existingEntities.isEmpty()) {
                        return Uni.createFrom().voidItem();
                      }
                      return withTransaction(
                              deleteSession -> deleteSession.removeAll(existingEntities))
                          .replaceWithVoid();
                    }));
  }

  @Override
  public <R> Uni<R> withTransaction(final Function<Mutiny.Session, Uni<R>> consumer) {
    return sf.withTransaction(consumer);
  }

  @Override
  public <R> Uni<R> withSession(Function<Mutiny.Session, Uni<R>> consumer) {
    return sf.withSession(consumer);
  }

  @Override
  public <R> Uni<R> withSelectionQuery(
      final String query, final Function<Mutiny.SelectionQuery<Entity>, Uni<R>> queryConsumer) {
    return sf.withSession(session -> queryConsumer.apply(session.createQuery(query, entityType)));
  }

  @Override
  public String currentEntityName() {
    return resolveEntityName(sf, entityType);
  }

  @Override
  public Uni<List<Entity>> selectMultiple(String query, Param... params) {
    return withSelectionQuery(
        query,
        entitySelectionQuery -> {
          Arrays.stream(params)
              .forEach(param -> entitySelectionQuery.setParameter(param.name(), param.value()));
          return entitySelectionQuery.getResultList();
        });
  }

  @Override
  public Uni<Entity> select(String query, Param... params) {
    return withSelectionQuery(
        query,
        entitySelectionQuery -> {
          Arrays.stream(params)
              .forEach(param -> entitySelectionQuery.setParameter(param.name(), param.value()));
          return entitySelectionQuery.getSingleResult();
        });
  }

  @Override
  public Uni<Boolean> exists(Entity entity) {
    return findById(entity.getId()).map(Objects::nonNull);
  }

  @Override
  public Uni<Boolean> exists(Object id) {
    return findById(id).map(Objects::nonNull);
  }

  private <T> String resolveEntityName(final Mutiny.SessionFactory sf, final Class<T> entityClass) {
    return sf.getMetamodel().getEntities().stream()
        .filter(e -> e.getJavaType().equals(entityClass))
        .map(EntityType::getName)
        .findFirst()
        .orElse(entityClass.getSimpleName());
  }
}
