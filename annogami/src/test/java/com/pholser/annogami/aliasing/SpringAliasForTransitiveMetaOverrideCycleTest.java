package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import com.pholser.annogami.Presences;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringAliasForTransitiveMetaOverrideCycleTest {
  @Retention(RUNTIME) @interface Base {
    String value() default "";
  }

  @Retention(RUNTIME) @Base @interface Level1 {
    // Level1.x is intended to alias into Base.value, but Level1 also has a cycle.
    @AliasFor(annotation = Base.class, attribute = "value")
    String x() default "";

    // Cycle: x <-> y (both within Level1)
    @AliasFor("y") String xAlias() default "";

    @AliasFor("xAlias") String y() default "";
  }

  @Retention(RUNTIME) @Level1 @interface Composed {
    // Composed.z aliases into Level1.xAlias (which participates in the cycle)
    @AliasFor(annotation = Level1.class, attribute = "xAlias")
    String z() default "";
  }

  @Composed(z = "boom") static class Target {}

  @Test void cycleInAliasGraphIsDetectedAndFailsFast() {
    assertThatThrownBy(() ->
      Presences.META_DIRECT
        .find(Base.class, Target.class, Aliasing.spring())
        .orElseGet(Assertions::fail)
        .value() // force proxy creation / member access
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("cycle");
  }
}
