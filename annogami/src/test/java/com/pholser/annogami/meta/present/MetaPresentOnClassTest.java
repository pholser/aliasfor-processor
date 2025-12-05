package com.pholser.annogami.meta.present;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.util.List;

import static com.pholser.annogami.Presences.META_PRESENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class MetaPresentOnClassTest {
  @Retention(RUNTIME) @interface A {
    int value();
  }

  @A(3) @Retention(RUNTIME) @interface HasA {}

  @HasA static class AHaverViaMeta {}

  @Retention(RUNTIME) @interface D {
    int value();
  }

  @D(9) @Inherited @Retention(RUNTIME) @interface HasD {}

  @HasD static class DBase {}
  static class DDerived extends DBase {}

  @Test void findsMetaPresentOnClass() {
    A a =
      META_PRESENT.find(A.class, AHaverViaMeta.class)
        .orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(3);
  }

  @Test void findsThroughInheritedSeedOnSubclass() {
    D d =
      META_PRESENT.find(D.class, DDerived.class)
        .orElseGet(Assertions::fail);

    assertThat(d.value()).isEqualTo(9);
  }

  @Test void allIncludesStartPresentAnnotationsAndMetaAnnotations() {
    List<Annotation> all = META_PRESENT.all(DDerived.class);

    List<String> types =
      all.stream()
        .map(a -> a.annotationType().getName())
        .toList();

    assertThat(types)
      .contains(HasD.class.getName()) // present on DDerived (via @Inherited)
      .contains(D.class.getName());   // meta-present on HasD
  }
}
