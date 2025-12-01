package com.pholser.annogami.indirect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.util.List;

import static com.pholser.annogami.Presences.DIRECT_OR_INDIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

public class DirectOrIndirectPresenceOnMethodTest {
  @Retention(RUNTIME) @interface A {
    int value();
  }

  @Retention(RUNTIME) @interface Bs {
    B[] value();
  }

  @Retention(RUNTIME) @Repeatable(Bs.class) @interface B {
    int value();
  }

  static class MethodHaver {
    @A(10) void m1() {}
    @B(20) @B(30) void m2() {}
    void m3() {}
  }

  @Test void findsSingleNonRepeatable() throws Exception {
    List<A> as =
      DIRECT_OR_INDIRECT.find(
        A.class,
        MethodHaver.class.getDeclaredMethod("m1"));
    assertThat(as).hasSize(1);

    A a = as.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(a.value()).isEqualTo(10);
  }

  @Test void missesNonDeclared() throws Exception {
    List<A> as =
      DIRECT_OR_INDIRECT.find(
        A.class,
        MethodHaver.class.getDeclaredMethod("m3"));

    assertThat(as).isEmpty();
  }

  @Test void findsRepeatableAnnotations() throws Exception {
    List<B> bs =
      DIRECT_OR_INDIRECT.find(
        B.class,
        MethodHaver.class.getDeclaredMethod("m2"));

    assertThat(bs)
      .extracting(B::value)
      .containsExactlyInAnyOrder(20, 30);
  }

  @Test void findsContainerAnnotationOfRepeatable() throws Exception {
    List<Bs> containers =
      DIRECT_OR_INDIRECT.find(
        Bs.class,
        MethodHaver.class.getDeclaredMethod("m2"));
    assertThat(containers).hasSize(1);

    Bs bs = containers.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(bs.value())
      .extracting(B::value)
      .containsExactlyInAnyOrder(20, 30);
  }
}
