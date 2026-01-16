package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import com.pholser.annogami.Presences;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class SpringAliasForImplicitMetaTargetTest {
  @Retention(RUNTIME) @interface Base {
    String value() default "";
  }

  @Retention(RUNTIME) @Base @interface Composed {
    @AliasFor("value") String path() default "";
  }

  @Composed(path = "p") static class Target {}

  @Test void aliasWithoutAnnotationDefaultsToMetaAnnotation() {
    Base b =
      DIRECT.find(Base.class, Target.class, Aliasing.spring())
        .orElseGet(Assertions::fail);

    assertThat(b.value()).isEqualTo("p");
  }
}
