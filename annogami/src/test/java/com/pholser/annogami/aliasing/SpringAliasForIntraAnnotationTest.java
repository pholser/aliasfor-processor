package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringAliasForIntraAnnotationTest {
  @Retention(RUNTIME) @interface Intra {
    @AliasFor("name") String value() default "";
    @AliasFor("value") String name() default "";
  }

  @Intra(name = "hello") static class Target1 {}
  @Intra(value = "hello") static class Target2 {}
  @Intra(name = "one", value = "two") static class TargetConflict {}

  @Test void intraAliasReadsThroughEitherMember() {
    Intra i =
      DIRECT.find(Intra.class, Target1.class, Aliasing.spring())
        .orElseGet(Assertions::fail);

    assertThat(i.name()).isEqualTo("hello");
    assertThat(i.value()).isEqualTo("hello");
  }

  @Test void intraAliasReadsThroughEitherDirection() {
    Intra i =
      DIRECT.find(Intra.class, Target2.class, Aliasing.spring())
        .orElseGet(Assertions::fail);

    assertThat(i.value()).isEqualTo("hello");
    assertThat(i.name()).isEqualTo("hello");
  }

  @Test void intraAliasConflictingExplicitValuesFailFast() {
    assertThatThrownBy(() ->
      DIRECT.find(Intra.class, TargetConflict.class, Aliasing.spring())
        .orElseGet(Assertions::fail)
        .value())
      .isInstanceOf(IllegalStateException.class);
  }
}
