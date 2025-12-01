package com.pholser.annogami;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.util.List;

import static com.pholser.annogami.Presences.DIRECT_OR_INDIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectOrIndirectPresenceOnConstructorTest {
  @Retention(RUNTIME) @interface A {
    int value();
  }

  @Retention(RUNTIME) @interface Bs {
    B[] value();
  }

  @Retention(RUNTIME) @Repeatable(Bs.class) @interface B {
    int value();
  }

  static class CtorHaver {
    @A(100) CtorHaver() {}
    @B(200) @B(300) CtorHaver(int x) {}
    CtorHaver(String s) {}
  }

  @Test void findsSingleNonRepeatable() throws Exception {
    List<A> as =
      DIRECT_OR_INDIRECT.find(
        A.class,
        CtorHaver.class.getDeclaredConstructor());
    assertThat(as).hasSize(1);

    A a = as.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(a.value()).isEqualTo(100);
  }

  @Test void missesNonDeclared() throws Exception {
    List<A> as =
      DIRECT_OR_INDIRECT.find(
        A.class,
        CtorHaver.class.getDeclaredConstructor(String.class));

    assertThat(as).isEmpty();
  }

  @Test void findsRepeatableAnnotations() throws Exception {
    List<B> bs =
      DIRECT_OR_INDIRECT.find(
        B.class,
        CtorHaver.class.getDeclaredConstructor(int.class));

    assertThat(bs)
      .extracting(B::value)
      .containsExactlyInAnyOrder(200, 300);
  }

  @Test void findsContainerAnnotation() throws Exception {
    List<Bs> containers =
      DIRECT_OR_INDIRECT.find(
        Bs.class,
        CtorHaver.class.getDeclaredConstructor(int.class));
    assertThat(containers).hasSize(1);

    Bs bs = containers.stream().findFirst().orElseGet(Assertions::fail);

    assertThat(bs.value())
      .extracting(B::value)
      .containsExactlyInAnyOrder(200, 300);
  }
}
