package com.pholser.annogami;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectPresenceOnReceiverTypeTest {
  @Retention(RUNTIME) @Target(TYPE_USE) @interface A {
    int value();
  }

  static class ReceiverHaver {
    void m(@A(1) ReceiverHaver this) {}
    void m2(ReceiverHaver this) {}
  }

  @Test void findsDirectlyPresent() throws Exception {
    Method m = ReceiverHaver.class.getDeclaredMethod("m");
    AnnotatedType receiver = m.getAnnotatedReceiverType();

    A a = DIRECT.find(A.class, receiver).orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(1);
  }

  @Test void missesWhenNotAnnotated() throws Exception {
    Method m2 = ReceiverHaver.class.getDeclaredMethod("m2");
    AnnotatedType receiver = m2.getAnnotatedReceiverType();

    DIRECT.find(A.class, receiver)
      .ifPresent(AnnotationAssertions::falseFind);
  }
}
