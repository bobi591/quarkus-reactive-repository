package com.sidekick.extensions.reactive.repository.deployment;

import com.sidekick.extensions.reactive.repository.BaseReactiveRepository;
import com.sidekick.extensions.reactive.repository.types.Param;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.smallrye.mutiny.Uni;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;

/**
 * Factory class responsible for creating method implementations for reactive repositories. It
 * processes repository interfaces and generates methods based on annotations.
 */
public class MethodImplFactory {
  // DotName for the @Query annotation
  private static final DotName QUERY_ANNOTATION =
      DotName.createSimple(
          com.sidekick.extensions.reactive.repository.annotations.query.Query.class);

  // DotName for the @Param annotation
  private static final DotName PARAM_ANNOTATION =
      DotName.createSimple(
          com.sidekick.extensions.reactive.repository.annotations.query.Param.class);

  /** Enum representing the possible return types of a method. */
  public static enum ReturnType {
    UNI,
    UNI_LIST
  }

  /**
   * Creates method implementations for the given repository interface.
   *
   * @param repositoryInterface the repository interface to process.
   * @param implClassCreator the class creator for the implementation class.
   */
  public void createMethods(
      final ClassInfo repositoryInterface, final ClassCreator implClassCreator) {
    final List<MethodInfo> queryMethods =
        repositoryInterface.methods().stream()
            .filter(methodInfo -> methodInfo.hasAnnotation(QUERY_ANNOTATION))
            .toList();
    createQueryImplementation(queryMethods, implClassCreator);
  }

  /**
   * Determines if the return type of a method is `Uni` or `Uni<List>`.
   *
   * @param methodInfo the method to analyze.
   * @return the return type as `ReturnType.UNI` or `ReturnType.UNI_LIST`.
   * @throws IllegalStateException if the return type is invalid.
   */
  public ReturnType isUniTypeOrUniListReturnType(final MethodInfo methodInfo) {
    final Type returnType = methodInfo.returnType();
    boolean isListType = false;

    if (returnType.asParameterizedType().arguments().size() == 1
        && returnType.name().equals(DotName.createSimple(Uni.class))) {
      final Type argumentType = returnType.asParameterizedType().arguments().getFirst();
      if (argumentType.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
        if (argumentType.asParameterizedType().arguments().size() == 1
            && argumentType.asParameterizedType().name().equals(DotName.createSimple(List.class))) {
          isListType = true;
        } else {
          throw new IllegalStateException("Methods with should have return type Uni or Uni<List>.");
        }
      }
    } else {
      throw new IllegalStateException("Methods with should have return type Uni or Uni<List>.");
    }

    return isListType ? ReturnType.UNI_LIST : ReturnType.UNI;
  }

  /**
   * Creates parameter information for a method, including parameter names and types.
   *
   * @param methodInfo the method to process.
   * @param methodCreator the method creator for the implementation.
   * @return a list of parameter name and variable pairs.
   */
  private List<Pair<String, ParamVar>> createParameterInfo(
      final MethodInfo methodInfo, final InstanceMethodCreator methodCreator) {
    final List<MethodParameterInfo> parameterInfos =
        methodInfo.parameters().stream()
            .filter(methodParameterInfo -> methodParameterInfo.hasAnnotation(PARAM_ANNOTATION))
            .toList();
    final List<Pair<String, ParamVar>> paramVars = new ArrayList<>();
    parameterInfos.forEach(
        parameterInfo ->
            paramVars.add(
                Pair.of(
                    parameterInfo.annotation(PARAM_ANNOTATION).value().asString(),
                    methodCreator.parameter(
                        parameterInfo.name(),
                        ClassDesc.of(parameterInfo.type().name().toString())))));
    return paramVars;
  }

  /**
   * Creates query method implementations for the given methods.
   *
   * @param queryMethods the methods annotated with @Query.
   * @param implClassCreator the class creator for the implementation class.
   * @throws IllegalStateException if a method is invalid.
   */
  public void createQueryImplementation(
      final List<MethodInfo> queryMethods, final ClassCreator implClassCreator) {
    for (final MethodInfo queryMethod : queryMethods) {
      if (queryMethod.isDefault()) {
        throw new IllegalStateException("Default methods cannot be annotated with @Query.");
      }

      final ReturnType returnType = isUniTypeOrUniListReturnType(queryMethod);
      final String methodNameToInvoke =
          returnType.equals(ReturnType.UNI_LIST) ? "selectMultiple" : "select";

      final AnnotationInstance queryAnnotation = queryMethod.annotation(QUERY_ANNOTATION);
      final String query = queryAnnotation.value().asString();

      implClassCreator.method(
          queryMethod.name(),
          instanceMethodCreator -> {
            instanceMethodCreator.returning(Uni.class);
            final List<Pair<String, ParamVar>> parameterInfos =
                createParameterInfo(queryMethod, instanceMethodCreator);
            instanceMethodCreator.body(
                blockCreator -> {
                  final List<Expr> methodExpressions = new ArrayList<>();
                  methodExpressions.add(Const.of(query));
                  final List<Expr> createParams =
                      parameterInfos.stream()
                          .map(
                              paramVar ->
                                  blockCreator.invokeStatic(
                                      MethodDesc.of(
                                          Param.class,
                                          "of",
                                          Param.class,
                                          String.class,
                                          Object.class),
                                      Const.of(paramVar.getKey()),
                                      blockCreator.get(paramVar.getValue())))
                          .toList();

                  methodExpressions.add(blockCreator.newArray(Param.class, createParams));

                  blockCreator.return_(
                      blockCreator.invokeVirtual(
                          MethodDesc.of(
                              BaseReactiveRepository.class,
                              methodNameToInvoke,
                              Uni.class,
                              String.class,
                              Param[].class),
                          instanceMethodCreator.this_(),
                          methodExpressions));
                });
          });
    }
  }
}
