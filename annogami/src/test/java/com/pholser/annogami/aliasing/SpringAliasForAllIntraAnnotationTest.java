package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.util.List;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class SpringAliasForAllIntraAnnotationTest {
  @Retention(RUNTIME) @interface Intra {
    @AliasFor("name") String value() default "";
    @AliasFor("value") String name() default "";
  }

  @Intra(name = "hello") static class Target {}

  @Test void allWithAliasingPropagatesIntraAliasedValue() {
    List<Annotation> all = DIRECT.all(Target.class, Aliasing.spring());

    Intra intra =
      all.stream()
        .filter(a -> a.annotationType() == Intra.class)
        .map(Intra.class::cast)
        .findFirst()
        .orElseGet(Assertions::fail);

    assertThat(intra.name()).isEqualTo("hello");
    assertThat(intra.value()).isEqualTo("hello");
  }
}
