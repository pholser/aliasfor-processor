package com.pholser.annogami.meta.direct;

import com.pholser.annogami.AnnotationAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.util.List;

import static com.pholser.annogami.Presences.META_DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class MetaDirectPresenceOnClassTest {
  @Retention(RUNTIME) @interface A {
    int value();
  }

  @A(3) @Retention(RUNTIME) @interface HasA {}

  @HasA static class AHaverViaMeta {}

  @Retention(RUNTIME) @interface Bs {
    B[] value();
  }

  @Retention(RUNTIME) @Repeatable(Bs.class) @interface B {
    int value();
  }

  @B(1) @B(2) @Retention(RUNTIME) @interface HasBs {}

  @HasBs static class BHaverViaMeta {}

  @Retention(RUNTIME) @Inherited @interface C {
    int value();
  }

  @C(6) @Retention(RUNTIME) @interface HasC {}

  @HasC static class CBase {}
  static class CDerived extends CBase {}

  @A(10)
  @Retention(RUNTIME) @interface HasA10 {}

  @A(20)
  @Retention(RUNTIME) @interface HasA20 {}

  @HasA10 @HasA20
  static class TwoPathsToA {}

  @Test void allEmitsDuplicateMetaReachedViaDifferentPaths() {
    List<Annotation> all = META_DIRECT.all(TwoPathsToA.class);

    long count =
      all.stream()
        .filter(a -> a.annotationType().equals(A.class))
        .count();

    assertThat(count).isEqualTo(2);
  }

  @Test void allEmitsDistinctMetaReachedViaDifferentPaths() {
    List<Annotation> all = META_DIRECT.all(TwoPathsToA.class);

    List<Integer> aValues =
      all.stream()
        .filter(a -> a.annotationType().equals(A.class))
        .map(a -> ((A) a).value())
        .toList();

    assertThat(aValues).containsExactlyInAnyOrder(10, 20);
  }

  @Test void allIncludesDeclaredSeedAnnotationOnTarget() {
    List<String> types =
      META_DIRECT.all(AHaverViaMeta.class).stream()
        .map(a -> a.annotationType().getName())
        .toList();

    assertThat(types).contains(HasA.class.getName());
  }

  @Test void allIncludesMetaAnnotationTypesFromSeedAnnotationType() {
    List<String> types =
      META_DIRECT.all(AHaverViaMeta.class).stream()
        .map(a -> a.annotationType().getName())
        .toList();

    assertThat(types).contains(A.class.getName());
  }

  @Test void allShowsRepeatableAsContainerNotElements() {
    List<String> types =
      META_DIRECT.all(BHaverViaMeta.class).stream()
        .map(a -> a.annotationType().getName())
        .toList();

    assertThat(types)
      .contains(Bs.class.getName())
      .doesNotContain(B.class.getName());
  }

  @Test void allDoesNotSeedFromInheritedAnnotationsOnSubclass() {
    assertThat(META_DIRECT.all(CDerived.class)).isEmpty();
  }

  @Test void findsMetaPresentOnClass() {
    A a =
      META_DIRECT.findFirst(A.class, AHaverViaMeta.class)
        .orElseGet(Assertions::fail);

    assertThat(a.value()).isEqualTo(3);
  }

  @Test void missesInheritedSeedBecauseDeclaredStartDoesNotSeeIt() {
    META_DIRECT.findFirst(C.class, CDerived.class)
      .ifPresent(AnnotationAssertions::falseFind);
  }
}
