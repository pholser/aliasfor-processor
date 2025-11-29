package com.pholser.annogami;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.reflect.Parameter;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectPresenceOnParameterTest {
  @Retention(RUNTIME) @interface A {
    int value();
  }

  @Retention(RUNTIME) @interface Bs {
    B[] value();
  }

  @Retention(RUNTIME) @Repeatable(Bs.class) @interface B {
    int value();
  }

  static class ParameterHolder {
    void p1(@A(1000) int x) {}
    void p2(@B(2000) @B(3000) int x) {}
    void p3(int x) {}
  }

  @Test void findsDirectlyPresent() throws Exception {
    A a =
      DIRECT.find(A.class, parameterOf("p1"))
        .orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(1000);
  }

  @Test void missesNotDeclared() throws Exception {
    DIRECT.find(A.class, parameterOf("p3"))
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void missesIndirectlyPresent() throws Exception {
    DIRECT.find(B.class, parameterOf("p2"))
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void findsContainerAnnotationOfIndirectlyPresent() throws Exception {
    Bs bs =
      DIRECT.find(Bs.class, parameterOf("p2"))
        .orElseGet(Assertions::fail);

    assertThat(bs.value()).hasSize(2);
  }

  private static Parameter parameterOf(String methodName)
    throws NoSuchMethodException {

    return ParameterHolder.class.getDeclaredMethod(methodName, int.class)
      .getParameters()[0];
  }
}
