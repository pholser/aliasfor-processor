package com.pholser.annogami;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectPresenceOnRecordComponentTest {
  @Retention(RUNTIME) @Target(RECORD_COMPONENT) @interface A {
    int value();
  }

  @Retention(RUNTIME) @Target(RECORD_COMPONENT) @interface Bs {
    B[] value();
  }

  @Retention(RUNTIME) @Target(RECORD_COMPONENT) @Repeatable(Bs.class)
  @interface B {
    int value();
  }

  record R(@A(1) int a, @B(2) @B(3) int b, int c) {}

  @Test void findsDirectlyPresent() {
    A a =
      DIRECT.find(A.class, R.class.getRecordComponents()[0])
        .orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(1);
  }

  @Test void findsContainerAnnotationOfIndirectlyPresent() {
    Bs bs =
      DIRECT.find(Bs.class, R.class.getRecordComponents()[1])
        .orElseGet(Assertions::fail);

    assertThat(bs.value()).hasSize(2);
  }

  @Test void missesNotDeclared() {
    DIRECT.find(A.class, R.class.getRecordComponents()[2])
      .ifPresent(AnnotationAssertions::falseFind);
  }
}
