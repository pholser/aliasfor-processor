package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.META_DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class SpringAliasForTransitiveMetaOverrideTest {
  @Retention(RUNTIME) @interface Base {
    String value() default "";
  }

  @Retention(RUNTIME) @Base @interface Level1 {
    @AliasFor(annotation = Base.class, attribute = "value")
    String x() default "";
  }

  @Retention(RUNTIME) @Level1 @interface Composed {
    @AliasFor(annotation = Level1.class, attribute = "x")
    String y() default "";
  }

  @Composed(y = "hello") static class Target {}

  @Test void transitiveAliasOverridesBaseThroughIntermediateMetaAnnotation() {
    Base b =
      META_DIRECT.find(Base.class, Target.class, Aliasing.spring())
        .orElseThrow();

    assertThat(b.value()).isEqualTo("hello");
  }
}
