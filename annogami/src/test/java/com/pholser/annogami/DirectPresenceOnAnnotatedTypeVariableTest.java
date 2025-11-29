package com.pholser.annogami;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectPresenceOnAnnotatedTypeVariableTest {
  @Retention(RUNTIME) @Target(TYPE_USE) @interface A {
    int value();
  }

  static class TypeUseHaver<T> {
    @A(1) T field;
    T plain;
  }

  @Test void findsOnAnnotatedTypeVariable() throws Exception {
    A a =
      DIRECT.find(
        A.class,
        TypeUseHaver.class.getDeclaredField("field").getAnnotatedType()
      ).orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(1);
  }

  @Test void missesOnAnnotatedTypeVariableNotDeclared() throws Exception {
    DIRECT.find(
      A.class,
      TypeUseHaver.class.getDeclaredField("plain").getAnnotatedType()
    ).ifPresent(AnnotationAssertions::falseFind);
  }
}
