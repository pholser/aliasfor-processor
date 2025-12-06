package com.pholser.annogami;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class BreadthFirstMetaWalkerCycleTest {
  @Retention(RUNTIME) @interface A {}

  @A @Retention(RUNTIME) @interface B {}

  @B @Retention(RUNTIME) @interface AWithB {}

  @AWithB @Retention(RUNTIME) @interface BWithA {}

  @AWithB @BWithA static class Target {}

  @Test void terminatesOnCycle() {
    MetaWalker walker =
      new BreadthFirstMetaWalker(MetaWalkConfig.defaultsDeclared());

    List<MetaVisit> visits = walker.walk(Target.class).toList();

    assertThat(visits)
      .anySatisfy(v -> assertThat(v.element()).isEqualTo(AWithB.class))
      .anySatisfy(v -> assertThat(v.element()).isEqualTo(BWithA.class));
  }
}
