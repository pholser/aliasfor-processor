package com.pholser.annogami;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.List;

import static com.pholser.annogami.Presences.DIRECT_OR_INDIRECT;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectOrIndirectPresenceOnAnnotatedTypeTest {
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
    List<A> as =
      DIRECT_OR_INDIRECT.find(
        A.class,
        Holder.class.getDeclaredField("array").getAnnotatedType());
    assertThat(as).hasSize(1);

    A a = as.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(a.value()).isEqualTo(1);
  }

  @Test void findsRepeatableOnAnnotatedArrayType() throws Exception {
    List<B> bs =
      DIRECT_OR_INDIRECT.find(
        B.class,
        Holder.class.getDeclaredField("repeatArray").getAnnotatedType());

    assertThat(bs)
      .extracting(B::value)
      .containsExactlyInAnyOrder(2, 3);
  }

  @Test void findsContainerOnAnnotatedArrayType() throws Exception {
    List<Bs> containers =
      DIRECT_OR_INDIRECT.find(
        Bs.class,
        Holder.class.getDeclaredField("repeatArray").getAnnotatedType());
    assertThat(containers).hasSize(1);

    Bs bs = containers.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(bs.value())
      .extracting(B::value)
      .containsExactlyInAnyOrder(2, 3);
  }

  @Test void findsOnAnnotatedParameterizedType() throws Exception {
    List<A> as =
      DIRECT_OR_INDIRECT.find(
        A.class,
        Holder.class.getDeclaredField("param").getAnnotatedType());
    assertThat(as).hasSize(1);

    A a = as.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(a.value()).isEqualTo(4);
  }

  @Test void findsRepeatableOnAnnotatedParameterizedType() throws Exception {
    List<B> bs =
      DIRECT_OR_INDIRECT.find(
        B.class,
        Holder.class.getDeclaredField("repeatParam").getAnnotatedType());

    assertThat(bs)
      .extracting(B::value)
      .containsExactlyInAnyOrder(5, 6);
  }

  @Test void findsContainerOnAnnotatedParameterizedType() throws Exception {
    List<Bs> containers =
      DIRECT_OR_INDIRECT.find(
        Bs.class,
        Holder.class.getDeclaredField("repeatParam").getAnnotatedType());
    assertThat(containers).hasSize(1);

    Bs bs = containers.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(bs.value())
      .extracting(B::value)
      .containsExactlyInAnyOrder(5, 6);
  }

  @Test void findsOnAnnotatedTypeArg() throws Exception {
    Field f = Holder.class.getDeclaredField("annotatedArg");
    AnnotatedParameterizedType paramType =
      (AnnotatedParameterizedType) f.getAnnotatedType();
    AnnotatedType typeArg = paramType.getAnnotatedActualTypeArguments()[0];

    List<A> as = DIRECT_OR_INDIRECT.find(A.class, typeArg);
    assertThat(as).hasSize(1);

    A a = as.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(a.value()).isEqualTo(7);
  }

  @Test void missesOnPlainAnnotatedType() throws Exception {
    List<A> as =
      DIRECT_OR_INDIRECT.find(
        A.class,
        Holder.class.getDeclaredField("plain").getAnnotatedType());

    assertThat(as).isEmpty();
  }
}
