package com.pholser.annogami;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Field;
import java.util.List;

import static com.pholser.annogami.Presences.DIRECT_OR_INDIRECT;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectOrIndirectPresenceOnAnnotatedWildcardTypeTest {
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
    AnnotatedType typeArg = paramType.getAnnotatedActualTypeArguments()[0];
    AnnotatedWildcardType wildcard = (AnnotatedWildcardType) typeArg;

    List<A> as = DIRECT_OR_INDIRECT.find(A.class, wildcard);
    assertThat(as).hasSize(1);

    A a = as.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(a.value()).isEqualTo(1);
  }

  @Test void missesOnUnannotatedWildcardType() throws Exception {
    Field f = WildCarder.class.getDeclaredField("plain");
    AnnotatedParameterizedType paramType =
      (AnnotatedParameterizedType) f.getAnnotatedType();
    AnnotatedType typeArg = paramType.getAnnotatedActualTypeArguments()[0];

    List<A> as = DIRECT_OR_INDIRECT.find(A.class, typeArg);

    assertThat(as).isEmpty();
  }
}
