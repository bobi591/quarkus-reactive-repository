package io.quarkus.reactive.repository.deployment;

import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmo2Adaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.This;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import jakarta.inject.Singleton;
import io.quarkus.reactive.repository.BaseReactiveRepository;
import io.quarkus.reactive.repository.annotations.bean.ReactiveRepositoryBean;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;

/**
 * The `ReactiveRepositoryProcessor` class is responsible for processing and generating reactive
 * repository implementations at build time. It integrates with the Quarkus build system to
 * dynamically create repository classes based on annotated interfaces.
 */
public class ReactiveRepositoryProcessor {

  private static final String FEATURE = "reactive-repository";

  /**
   * Registers the reactive repository feature with the Quarkus build system.
   *
   * @return a `FeatureBuildItem` representing the reactive repository feature.
   */
  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(FEATURE);
  }

  /**
   * Resolves a `Type` to a `ClassType` if possible.
   *
   * @param type the type to resolve.
   * @return the resolved `ClassType`, or `null` if the type is not a `ClassType`.
   */
  private ClassType resolveType(final Type type) {
    if (type instanceof ClassType ct) {
      return ct;
    }
    return null;
  }

  /**
   * Generates reactive repository implementations for interfaces annotated with
   * `@ReactiveRepositoryBean`.
   *
   * @param index the combined index of the application.
   * @param beanBuildItemProducer the build producer for generated beans.
   */
  @BuildStep
  public void generateReactiveRepositories(
      final CombinedIndexBuildItem index,
      final BuildProducer<GeneratedBeanBuildItem> beanBuildItemProducer) {
    final IndexView view = index.getIndex();
    final DotName beanAnnotationName = DotName.createSimple(ReactiveRepositoryBean.class);
    final Collection<AnnotationInstance> annotatedInstances =
        view.getAnnotations(beanAnnotationName);

    final List<Pair<ClassInfo, Type>> typesForImplementation = new ArrayList<>();

    for (final AnnotationInstance annotatedInstance : annotatedInstances) {
      final ClassInfo reactiveRepositoryInterface = annotatedInstance.target().asClass();
      if (!reactiveRepositoryInterface.isInterface()) {
        throw new IllegalStateException(
            String.format(
                "@ReactiveRepositoryBean can only be applied to interfaces. %s is not an interface.",
                reactiveRepositoryInterface.name()));
      }
      for (final Type interfaceType : reactiveRepositoryInterface.interfaceTypes()) {
        if (interfaceType.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
          final ParameterizedType parameterizedType = interfaceType.asParameterizedType();
          final Type entityType = parameterizedType.arguments().get(0);
          typesForImplementation.add(Pair.of(reactiveRepositoryInterface, entityType));
        }
      }
    }

    for (final Pair<ClassInfo, Type> typeForImplementation : typesForImplementation) {
      final ClassInfo repositoryForImpl = typeForImplementation.getKey();
      final Type entityType = resolveType(typeForImplementation.getValue());

      final Class<?> entityClass = classForName(entityType.name().toString());

      final String implementationClassName =
          repositoryForImpl.name().packagePrefix() + "." + repositoryForImpl.simpleName() + "Impl";

      final Gizmo gizmo = Gizmo.create(new GeneratedBeanGizmo2Adaptor(beanBuildItemProducer));
      gizmo.class_(
          implementationClassName,
          classCreator -> {
            classCreator.extends_(BaseReactiveRepository.class);
            classCreator.addAnnotation(Singleton.class);
            classCreator.implements_(ClassDesc.of(repositoryForImpl.name().toString()));

            // Add constructor that accepts Mutiny Session Factory
            classCreator.constructor(
                constructorCreator -> {
                  final This this_ = constructorCreator.this_();
                  final ParamVar sessionFactoryParam =
                      constructorCreator.parameter("sf", Mutiny.SessionFactory.class);
                  constructorCreator.body(
                      bodyCreator -> {
                        bodyCreator.invokeSpecial(
                            ConstructorDesc.of(
                                BaseReactiveRepository.class,
                                Mutiny.SessionFactory.class,
                                Class.class),
                            this_,
                            sessionFactoryParam,
                            Const.of(entityClass));
                        bodyCreator.return_();
                      });
                });

            final MethodImplFactory methodImplFactory = new MethodImplFactory();
            methodImplFactory.createMethods(repositoryForImpl, classCreator);
          });
    }
  }

  /**
   * Loads a class by its name.
   *
   * @param name the fully qualified name of the class.
   * @return the `Class` object representing the class.
   * @throws IllegalStateException if the class cannot be found.
   */
  private Class<?> classForName(final String name) {
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }
}
