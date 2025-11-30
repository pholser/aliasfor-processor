package com.pholser.annogami;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.util.List;

import static com.pholser.annogami.Presences.DIRECT_OR_INDIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class DirectOrIndirectPresenceOnClassTest {
  @Retention(RUNTIME) @interface A {
    int value();
  }

  @A(3) static class AHaver {}

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

  @C(6) @D(7) @D(8) static class Base {}
  static class Derived extends Base {}

  @Retention(RUNTIME) @Inherited @interface Ds {
    D[] value();
  }

  @Retention(RUNTIME) @Repeatable(Ds.class) @interface D {
    int value();
  }

  @Test void findsDeclared() {
    List<A> as = DIRECT_OR_INDIRECT.find(A.class, AHaver.class);
    assertThat(as).hasSize(1);

    A a = as.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(a.value()).isEqualTo(3);
  }

  @Test void missesNotDeclared() {
    List<A> as = DIRECT_OR_INDIRECT.find(A.class, ManyBHaver.class);

    assertThat(as).isEmpty();
  }

  @Test void findsRepeatableDeclared() {
    List<B> bs = DIRECT_OR_INDIRECT.find(B.class, ManyBHaver.class);

    assertThat(bs)
      .extracting(B::value)
      .containsExactlyInAnyOrder(4, 5);
  }

  @Test void findsContainerForRepeatable() {
    List<Bs> containers = DIRECT_OR_INDIRECT.find(Bs.class, ManyBHaver.class);

    Bs bs = containers.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(containers).hasSize(1);
    assertThat(bs.value()).hasSize(2);
    assertThat(bs.value())
      .extracting(B::value)
      .containsExactlyInAnyOrder(4, 5);
  }

  @Test void missesInheritedNonRepeatableOnSubclass() {
    List<C> cs = DIRECT_OR_INDIRECT.find(C.class, Derived.class);

    assertThat(cs).isEmpty();
  }

  @Test void findsInheritedNonRepeatableOnBaseClassItself() {
    List<C> cs = DIRECT_OR_INDIRECT.find(C.class, Base.class);

    C c = cs.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(cs).hasSize(1);
    assertThat(c.value()).isEqualTo(6);
  }

  @Test void missesInheritedRepeatableOnSubclass() {
    List<D> ds = DIRECT_OR_INDIRECT.find(D.class, Derived.class);

    assertThat(ds).isEmpty();
  }

  @Test void findsRepeatableDeclaredOnBaseClassItself() {
    List<D> ds = DIRECT_OR_INDIRECT.find(D.class, Base.class);

    assertThat(ds)
      .extracting(D::value)
      .containsExactlyInAnyOrder(7, 8);
  }

  @Test void missesInheritedContainerOnSubclass() {
    List<Ds> containers = DIRECT_OR_INDIRECT.find(Ds.class, Derived.class);

    assertThat(containers).isEmpty();
  }

  @Test void seesContainerDeclaredOnBaseClassItself() {
    List<Ds> containers = DIRECT_OR_INDIRECT.find(Ds.class, Base.class);

    Ds ds = containers.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(containers).hasSize(1);
    assertThat(ds.value())
      .extracting(D::value)
      .containsExactlyInAnyOrder(7, 8);
  }

  @Retention(RUNTIME) @interface Unused {
  }

  @Test void annotationTypeUnrelatedToTarget() {
    List<Unused> unused = DIRECT_OR_INDIRECT.find(Unused.class, AHaver.class);

    assertThat(unused).isEmpty();
  }
}
