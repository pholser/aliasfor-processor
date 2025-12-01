package com.pholser.annogami.direct;

import com.pholser.annogami.AnnotationAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectPresenceOnFieldTest {
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

  @Test void findsDirectlyPresent() throws Exception {
    A a =
      DIRECT.find(A.class, FieldHaver.class.getDeclaredField("a"))
        .orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(1);
  }

  @Test void missesNotDeclaredOnField() throws Exception {
    DIRECT.find(A.class, FieldHaver.class.getDeclaredField("none"))
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void missesIndirectlyPresent() throws Exception {
    DIRECT.find(B.class, FieldHaver.class.getDeclaredField("manyBs"))
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void findsContainerAnnotationOfIndirectlyPresent() throws Exception {
    Bs bs =
      DIRECT.find(Bs.class, FieldHaver.class.getDeclaredField("manyBs"))
        .orElseGet(Assertions::fail);

    assertThat(bs.value()).hasSize(2);
  }
}
