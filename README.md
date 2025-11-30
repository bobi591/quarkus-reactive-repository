# Quarkus Reactive Repository
A Quarkus extension for Spring Data JPA flavoured reactive repositories with Hibernate Reactive (Mutiny).


## Information
### Entities
Your entities should implement the `Identifiable<ID>` interface which exposes the `getId()` method.
The `<ID>` can be of any type, as long as Hibernate Mutiny Session supports it.

### Repositories
Your repositories should be defined with interfaces that extend the `ReactiveRepository<Entity extends Identifiable<?>>` interface.</br>
The `<Entity>` param should point to the type of you concrete entity class.<br>We will talk about inheritance options below.</br></br>
<b>Note:</b> Repository interfaces should be annotated with `@ReactiveRepositoryBean`.

### Method based operations
The Reactive Repository extension supports the following operations:
<ul>
    <li><b>findById</b> - Finds an entity by its identifier.</li>
    <li><b>persistWithTransaction</b> - Persists an entity within a transaction.</li>
    <li><b>persistAllWithTransaction</b> - Persists a multiple entities within a transaction.</li>
    <li><b>deleteWithTransaction</b> - Persists a multiple entities within a transaction.</li>
    <li><b>deleteAllWithTransaction</b> - Deletes multiple entities within a transaction.</li>
    <li><b>withTransaction</b> - Executes a function within a transaction.</li>
    <li><b>withSession</b> - Executes a function within a session.</li>
    <li><b>withSelectionQuery</b> - Executes a selection query with a consumer function that can let you customise the Mutiny.SelectionQuery.</li>
    <li><b>currentEntityName</b> - Retrieves the name of the current entity.</li>
    <li><b>selectMultiple</b> - Executes a query and selects multiple entities.</li>
    <li><b>select</b> - Executes a query and selects a single entity.</li>
    <li><b>exists</b> - Checks if the given entity exists in the repository.</li>
    <li><b>exists</b> - Checks if the given entity exists in the repository by id.</li>
</ul>

#### Notes:
Mind that all supported operations return `Uni<?>`, where in cases like `selectMultiple` the return type is `Uni<List<?>>`.

### Annotation based operations
<ul>
    <li><b>Query</b> - Annotation that defines a query.</li>
</ul>

## Examples
### Basic repository

<code>@ReactiveRepositoryBean
public interface ReactiveUserRepository extends ReactiveRepository\<User>{}</code>

### Repository with method based operations
<code>@ReactiveRepositoryBean
public interface ReactiveUserRepository extends ReactiveRepository\<User> {
  default Uni\<User> findByEmail(final String email) {
    return select("from User u where u.email = :email", Param.of("email", email));
  }
}</code>

### Repository with annotation based operations
<code>@ReactiveRepositoryBean
public interface ReactiveFriendsConversationRepository
    extends ReactiveRepository\<FriendsConversation> {
  @Query(
      "select fc from FriendsConversation fc join fc.conversations c where c.user.email = :email")
  Uni\<List\<FriendsConversation>> getByEmail(@Param("email") final String email);
  @Query(
      "select fc from FriendsConversation fc join fc.conversations c where c.event.id = :eventId")
  Uni\<FriendsConversation> getByEventId(@Param("eventId") final Long eventId);
}</code>
