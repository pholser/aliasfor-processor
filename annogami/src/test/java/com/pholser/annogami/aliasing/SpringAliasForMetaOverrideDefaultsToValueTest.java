package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.META_DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class SpringAliasForMetaOverrideDefaultsToValueTest {
  @Retention(RUNTIME) @interface Base {
    String value() default "";
  }

  @Retention(RUNTIME) @Base @interface Composed {
    @AliasFor(annotation = Base.class) String name() default "";
  }

  @Composed(name = "hello") static class Target {}

  @Test void aliasForAnnotationOnlyDefaultsToTargetValue() {
    Base base =
      META_DIRECT
        .find(Base.class, Target.class, Aliasing.spring())
        .orElseGet(Assertions::fail);

    assertThat(base.value()).isEqualTo("hello");
  }
}
