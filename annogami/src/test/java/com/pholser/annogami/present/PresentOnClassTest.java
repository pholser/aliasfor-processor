package com.pholser.annogami.present;

import com.pholser.annogami.AnnotationAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.PRESENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class PresentOnClassTest {
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

  @C(6) static class CBase {}
  static class CDerived extends CBase {}

  @Retention(RUNTIME) @interface E {
    int value();
  }

  @E(9) static class ENonInheritedBase {}
  static class ENonInheritedDerived extends ENonInheritedBase {}

  @Retention(RUNTIME) @Inherited @interface Ds {
    D[] value();
  }

  @Retention(RUNTIME) @Repeatable(Ds.class) @interface D {
    int value();
  }

  @D(7) @D(8) static class DBase {}
  static class DDerived extends DBase {}

  @Test void findsSingleNonRepeatable() {
    A a = PRESENT.find(A.class, AHaver.class).orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(3);
  }

  @Test void missesNonDeclaredNonRepeatable() {
    PRESENT.find(A.class, ManyBHaver.class)
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void missesRepeatableWhenOnlyContainerIsPresent() {
    PRESENT.find(B.class, ManyBHaver.class)
      .ifPresent(AnnotationAssertions::falseFind);
  }

  @Test void findsContainerAnnotationForRepeatable() {
    Bs bs =
      PRESENT.find(Bs.class, ManyBHaver.class)
        .orElseGet(Assertions::fail);

    assertThat(bs.value())
      .extracting(B::value)
      .containsExactlyInAnyOrder(4, 5);
  }

  @Test void findsInheritedNonRepeatableOnSubclass() {
    C c = PRESENT.find(C.class, CDerived.class).orElseGet(Assertions::fail);

    assertThat(c.value()).isEqualTo(6);
  }

  @Test void findsDirectAnnotationOnBaseClassItself() {
    C c = PRESENT.find(C.class, CBase.class).orElseGet(Assertions::fail);

    assertThat(c.value()).isEqualTo(6);
  }

  @Test void doesNotInheritNonInheritedAnnotationToSubclass() {
    PRESENT.find(E.class, ENonInheritedDerived.class)
        .ifPresent(AnnotationAssertions::falseFind);

    E base =
      PRESENT.find(E.class, ENonInheritedBase.class)
        .orElseGet(Assertions::fail);
    assertThat(base.value()).isEqualTo(9);
  }

  @Test void inheritedContainerAnnotationIsPresentOnSubclass() {
    Ds dsOnDerived =
      PRESENT.find(Ds.class, DDerived.class)
        .orElseGet(Assertions::fail);

    assertThat(dsOnDerived.value())
      .extracting(D::value)
      .containsExactlyInAnyOrder(7, 8);
  }

  @Test void elementTypeOfRepeatableIsNotItselfInherited() {
    PRESENT.find(D.class, DDerived.class)
      .ifPresent(AnnotationAssertions::falseFind);

    PRESENT.find(D.class, DBase.class)
      .ifPresent(AnnotationAssertions::falseFind);

    Ds dsOnBase =
      PRESENT.find(Ds.class, DBase.class)
        .orElseGet(Assertions::fail);

    assertThat(dsOnBase.value())
      .extracting(D::value)
      .containsExactlyInAnyOrder(7, 8);
  }
}
