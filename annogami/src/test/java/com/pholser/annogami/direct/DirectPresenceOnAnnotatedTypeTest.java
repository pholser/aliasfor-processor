package com.pholser.annogami.direct;

import com.pholser.annogami.AnnotationAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Repeatable;
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

class DirectPresenceOnAnnotatedTypeTest {
  @Retention(RUNTIME) @Target(TYPE_USE) @interface A {
    int value();
  }

  @Retention(RUNTIME) @Target(TYPE_USE) @interface Bs {
    B[] value();
  }

  @Retention(RUNTIME) @Target(TYPE_USE) @Repeatable(Bs.class) @interface B {
    int value();
  }

  static class Holder {
    int @A(1) [] array;
    int @B(2) @B(3) [] repeatArray;
    @A(4) List<String> param;
    @B(5) @B(6) List<String> repeatParam;
    List<@A(7) String> annotatedArg;
    List<String> plain;
  }

  @Test void findsOnAnnotatedArrayType() throws Exception {
    A a =
      DIRECT.find(
        A.class,
        Holder.class.getDeclaredField("array").getAnnotatedType()
      ).orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(1);
  }

  @Test void missesOnAnnotatedArrayTypeNotDeclared() throws Exception {
    DIRECT.find(
      A.class,
      Holder.class.getDeclaredField("plain").getAnnotatedType()
    ).ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void missesOnAnnotatedArrayTypeIndirectlyPresent() throws Exception {
    DIRECT.find(
      B.class,
      Holder.class.getDeclaredField("repeatArray").getAnnotatedType()
    ).ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void findsContainerOnAnnotatedArrayType() throws Exception {
    Bs bs =
      DIRECT.find(
        Bs.class,
        Holder.class.getDeclaredField("repeatArray").getAnnotatedType()
      ).orElseGet(Assertions::fail);

    assertThat(bs.value()).hasSize(2);
  }

  @Test void findsOnAnnotatedParameterizedType() throws Exception {
    A a =
      DIRECT.find(
        A.class,
        Holder.class.getDeclaredField("param").getAnnotatedType()
      ).orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(4);
  }

  @Test void findsContainerOnAnnotatedParameterizedType() throws Exception {
    Bs bs =
      DIRECT.find(
        Bs.class,
        Holder.class.getDeclaredField("repeatParam").getAnnotatedType()
      ).orElseGet(Assertions::fail);

    assertThat(bs.value()).hasSize(2);
  }

  @Test void findsOnAnnotatedTypeArgument() throws Exception {
    Field f = Holder.class.getDeclaredField("annotatedArg");
    AnnotatedParameterizedType paramType =
      (AnnotatedParameterizedType) f.getAnnotatedType();
    AnnotatedType argType = paramType.getAnnotatedActualTypeArguments()[0];

    A a = DIRECT.find(A.class, argType).orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(7);
  }
}
