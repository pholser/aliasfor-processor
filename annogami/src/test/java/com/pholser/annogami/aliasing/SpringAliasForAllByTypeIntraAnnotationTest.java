package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;
import java.util.List;

import static com.pholser.annogami.Presences.DIRECT_OR_INDIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class SpringAliasForAllByTypeIntraAnnotationTest {
  @Retention(RUNTIME) @interface Intra {
    @AliasFor("name") String value() default "";
    @AliasFor("value") String name() default "";
  }

  @Intra(name = "hello") static class Target {}

  @Test void findWithAliasingPropagatesIntraAliasedValue() {
    List<Intra> found =
      DIRECT_OR_INDIRECT.find(Intra.class, Target.class, Aliasing.spring());

    Intra intra = found.stream()
      .findFirst()
      .orElseGet(Assertions::fail);

    assertThat(intra.name()).isEqualTo("hello");
    assertThat(intra.value()).isEqualTo("hello");
  }
}
