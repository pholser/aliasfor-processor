package com.pholser.annogami.direct;

import com.pholser.annogami.AnnotationAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.List;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectPresenceOnAnnotatedWildcardTypeTest {
  @Retention(RUNTIME) @Target(TYPE_USE) @interface A {
    int value();
  }

  static class WildCarder {
    List<@A(1) ? extends Number> withWildcard;
    List<?> plain;
  }

  @Test void findsOnAnnotatedWildcardType() throws Exception {
    Field f = WildCarder.class.getDeclaredField("withWildcard");
    AnnotatedParameterizedType paramType =
      (AnnotatedParameterizedType) f.getAnnotatedType();
    AnnotatedType wildcard = paramType.getAnnotatedActualTypeArguments()[0];

    A a = DIRECT.find(A.class, wildcard).orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(1);
  }

  @Test void missesOnAnnotatedWildcardTypeNotDeclared() throws Exception {
    Field f = WildCarder.class.getDeclaredField("plain");
    AnnotatedParameterizedType paramType =
      (AnnotatedParameterizedType) f.getAnnotatedType();
    AnnotatedType arg = paramType.getAnnotatedActualTypeArguments()[0];

    DIRECT.find(A.class, arg).ifPresent(AnnotationAssertions::falseFind);
  }
}
