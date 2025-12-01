package com.pholser.annogami.indirect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.util.List;

import static com.pholser.annogami.Presences.DIRECT_OR_INDIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectOrIndirectPresenceOnFieldTest {
  @Retention(RUNTIME) @interface A {
    int value();
  }

  @Retention(RUNTIME) @interface Bs {
    B[] value();
  }

  @Retention(RUNTIME) @Repeatable(Bs.class) @interface B {
    int value();
  }

  static class FieldHaver {
    @A(1) int a;
    @B(2) @B(3) int manyBs;
    int none;
  }

  @Test void findsSingleNonRepeatable() throws Exception {
    List<A> as =
      DIRECT_OR_INDIRECT.find(
        A.class,
        FieldHaver.class.getDeclaredField("a"));
    assertThat(as).hasSize(1);

    A a = as.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(a.value()).isEqualTo(1);
  }

  @Test void missesNonDeclared() throws Exception {
    List<A> as =
      DIRECT_OR_INDIRECT.find(
        A.class,
        FieldHaver.class.getDeclaredField("none"));

    assertThat(as).isEmpty();
  }

  @Test void findsRepeatable() throws Exception {
    List<B> bs =
      DIRECT_OR_INDIRECT.find(
        B.class,
        FieldHaver.class.getDeclaredField("manyBs"));

    assertThat(bs)
      .extracting(B::value)
      .containsExactlyInAnyOrder(2, 3);
  }

  @Test void findsContainerAnnotationOfRepeatable() throws Exception {
    List<Bs> containers =
      DIRECT_OR_INDIRECT.find(
        Bs.class,
        FieldHaver.class.getDeclaredField("manyBs"));

    Bs bs = containers.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(containers).hasSize(1);
    assertThat(bs.value())
      .extracting(B::value)
      .containsExactlyInAnyOrder(2, 3);
  }
}
