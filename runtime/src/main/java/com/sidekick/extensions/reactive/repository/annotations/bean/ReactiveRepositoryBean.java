package com.sidekick.extensions.reactive.repository.annotations.bean;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a reactive repository bean. This annotation is used to indicate
 * that the annotated class is a reactive repository bean, which can be managed and processed by the
 * framework. Should annotate only interfaces extending {@link
 * com.sidekick.extensions.reactive.repository.ReactiveRepository}
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface ReactiveRepositoryBean {}
