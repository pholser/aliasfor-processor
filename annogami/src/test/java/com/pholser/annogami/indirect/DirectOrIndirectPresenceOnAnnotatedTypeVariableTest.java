package com.pholser.annogami.indirect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.Field;
import java.util.List;

import static com.pholser.annogami.Presences.DIRECT_OR_INDIRECT;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectOrIndirectPresenceOnAnnotatedTypeVariableTest {
  @Retention(RUNTIME) @Target(TYPE_USE) @interface A {
    int value();
  }

  static class TypeUseHaver<T> {
    @A(1) T field;
    T plain;
  }

  @Test void findsOnAnnotatedTypeVariableUse() throws Exception {
    Field f = TypeUseHaver.class.getDeclaredField("field");
    AnnotatedType annotatedType = f.getAnnotatedType();
    AnnotatedTypeVariable typeVar = (AnnotatedTypeVariable) annotatedType;

    List<A> as = DIRECT_OR_INDIRECT.find(A.class, typeVar);
    assertThat(as).hasSize(1);

    A a = as.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(a.value()).isEqualTo(1);
  }

  @Test void missesWhenTypeVariableUseNotAnnotated() throws Exception {
    List<A> as =
      DIRECT_OR_INDIRECT.find(
        A.class,
        TypeUseHaver.class.getDeclaredField("plain").getAnnotatedType());

    assertThat(as).isEmpty();
  }
}
