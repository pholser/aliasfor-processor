package com.pholser.annogami.direct;

import com.pholser.annogami.AnnotationAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectPresenceOnClassTest {
  @Retention(RUNTIME) @interface A {
    int value();
  }

  @A(3) static class AHaver {
  }

  @Retention(RUNTIME) @interface Bs {
    B[] value();
  }

  @Retention(RUNTIME) @Repeatable(Bs.class) @interface B {
    int value();
  }

  @B(4) @B(5) static class ManyBHaver {
  }

  @Retention(RUNTIME) @Inherited @interface C {
    int value();
  }

  @C(6) @D(7) @D(8)
  static class Base {}
  static class Derived extends Base {}

  @Retention(RUNTIME) @Inherited @interface Ds {
    D[] value();
  }

  @Retention(RUNTIME) @Repeatable(Ds.class) @interface D {
    int value();
  }

  @Test void findsDirectlyPresent() {
    A a =
      DIRECT.find(A.class, AHaver.class)
        .orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(3);
  }

  @Test void missesNotDeclared() {
    DIRECT.find(A.class, ManyBHaver.class)
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void missesIndirectlyPresent() {
    DIRECT.find(B.class, ManyBHaver.class)
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void findsContainerAnnotationOfIndirectlyPresent() {
    Bs bs = DIRECT.find(Bs.class, ManyBHaver.class)
      .orElseGet(Assertions::fail);

    assertThat(bs.value()).hasSize(2);
  }

  @Test void missesPresent() {
    DIRECT.find(C.class, Derived.class)
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void missesAssociated() {
    DIRECT.find(D.class, Derived.class)
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void missesContainerAnnotationOfAssociated() {
    DIRECT.find(Ds.class, Derived.class)
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void findsAll() {
    assertThat(DIRECT.all(AHaver.class))
      .hasSize(1)
      .allSatisfy(a -> assertThat(a.annotationType()).isEqualTo(A.class));
  }

  @Test void findsAllContainerButNotRepeated() {
    assertThat(DIRECT.all(ManyBHaver.class))
      .extracting(a -> a.annotationType().getName())
      .containsExactly(Bs.class.getName())
      .doesNotContain(B.class.getName());
  }

  @Test void findsAllDeclaredAnnotationsOnBaseIncludingInheritedContainer() {
    assertThat(DIRECT.all(Base.class))
      .extracting(a -> a.annotationType().getName())
      .containsExactlyInAnyOrder(C.class.getName(), Ds.class.getName())
      .doesNotContain(D.class.getName());
  }

  @Test void missesAllInheritedAnnotationsOnSubclass() {
    assertThat(DIRECT.all(Derived.class)).isEmpty();
  }
}
