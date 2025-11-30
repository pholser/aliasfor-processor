package com.pholser.annogami;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.TypeVariable;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectPresenceOnAnnotatedTypeBoundsTest {
  @Retention(RUNTIME) @Target(TYPE_USE) @interface A {
    int value();
  }

  static class BoundsHaver<@A(1) T extends @A(2) Number> {}
  static class NoAnnotatedBounds<T extends Number> {}

  @Test void findsDirectlyPresent() {
    @SuppressWarnings("rawtypes")
    TypeVariable<Class<BoundsHaver>>[] vars =
      BoundsHaver.class.getTypeParameters();
    AnnotatedType[] bounds = vars[0].getAnnotatedBounds();

    A a =
      DIRECT.find(A.class, bounds[0])
        .orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(2);
  }

  @Test void missesWhenNotAnnotated() {
    @SuppressWarnings("rawtypes")
    TypeVariable<Class<NoAnnotatedBounds>>[] vars =
      NoAnnotatedBounds.class.getTypeParameters();
    AnnotatedType[] bounds = vars[0].getAnnotatedBounds();

    DIRECT.find(A.class, bounds[0])
      .ifPresent(AnnotationAssertions::falseFind);
  }
}
