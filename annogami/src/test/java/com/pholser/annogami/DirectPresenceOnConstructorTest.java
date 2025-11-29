package com.pholser.annogami;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectPresenceOnConstructorTest {
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

  @Test void findsDirectlyPresent() throws Exception {
    A a =
      DIRECT.find(A.class, CtorHaver.class.getDeclaredConstructor())
        .orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(100);
  }

  @Test void missesNotDeclared() throws Exception {
    DIRECT.find(A.class, CtorHaver.class.getDeclaredConstructor(String.class))
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void missesIndirectlyPresent() throws Exception {
    DIRECT.find(B.class, CtorHaver.class.getDeclaredConstructor(int.class))
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void findsContainerAnnotationOfIndirectlyPresent() throws Exception {
    Bs bs =
      DIRECT.find(Bs.class, CtorHaver.class.getDeclaredConstructor(int.class))
        .orElseGet(Assertions::fail);

    assertThat(bs.value()).hasSize(2);
  }
}
