package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import com.pholser.annogami.Presences;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringAliasForIntraDefaultMismatchTest {
  @Retention(RUNTIME) @interface BrokenDefaults {
    @AliasFor("name") String value() default "a";
    @AliasFor("value") String name() default "b";
  }

  @BrokenDefaults(value = "x")
  static class Target {}

  @Test void mirroredAliasesMustHaveSameDefaultValue() {
    assertThatThrownBy(() ->
      DIRECT.find(BrokenDefaults.class, Target.class, Aliasing.spring())
        .orElseGet(Assertions::fail)
        .value()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("default");
  }
}
