package com.pholser.annogami.direct;

import com.pholser.annogami.AnnotationAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectPresenceOnMethodTest {
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

  @Test void findsDirectlyPresent() throws Exception {
    A a =
      DIRECT.find(A.class, MethodHaver.class.getDeclaredMethod("m1"))
        .orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(10);
  }

  @Test void missesNotDeclared() throws Exception {
    DIRECT.find(A.class, MethodHaver.class.getDeclaredMethod("m3"))
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void missesIndirectlyPresent() throws Exception {
    DIRECT.find(B.class, MethodHaver.class.getDeclaredMethod("m2"))
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void findsContainerAnnotationOfIndirectlyPresent() throws Exception {
    Bs bs =
      DIRECT.find(Bs.class, MethodHaver.class.getDeclaredMethod("m2"))
        .orElseGet(Assertions::fail);

    assertThat(bs.value()).hasSize(2);
  }
}
