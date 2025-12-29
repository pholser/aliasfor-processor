package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.META_DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class SpringAliasForMetaOverrideNonValueAttributeTest {
  @Retention(RUNTIME) @interface Base {
    String name() default "default-name";
    int count() default 42;
  }

  @Retention(RUNTIME) @Base @interface Composed {
    @AliasFor(annotation = Base.class, attribute = "name")
    String myName() default "";
  }

  @Composed(myName = "hello")
  static class Target {}

  @Test void metaAliasForNonValueAttributeIsApplied() {
    Base base =
      META_DIRECT.find(Base.class, Target.class, Aliasing.spring())
        .orElseThrow();

    assertThat(base.name()).isEqualTo("hello");
    assertThat(base.count()).isEqualTo(42);
  }
}
