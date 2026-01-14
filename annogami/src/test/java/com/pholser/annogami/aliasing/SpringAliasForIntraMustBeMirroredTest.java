package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringAliasForIntraMustBeMirroredTest {
  @Retention(RUNTIME) @interface Broken {
    @AliasFor("name") String value() default "";
    String name() default "";
  }

  @Broken(value = "x")
  static class Target {}

  @Test void intraAliasMustBeReciprocal() {
    assertThatThrownBy(() ->
      DIRECT.find(Broken.class, Target.class, Aliasing.spring())
        .orElseGet(Assertions::fail)
        .name() // force resolution
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mirror");
  }
}
