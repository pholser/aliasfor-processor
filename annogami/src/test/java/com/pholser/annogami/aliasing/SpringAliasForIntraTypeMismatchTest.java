package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringAliasForIntraTypeMismatchTest {
  @Retention(RUNTIME) @interface BrokenTypes {
    @AliasFor("name") String value() default "";
    @AliasFor("value") int name() default 0;
  }

  @BrokenTypes(value = "x") static class Target {}

  @Test void mirroredAliasesMustHaveSameReturnType() {
    assertThatThrownBy(() ->
      DIRECT.find(BrokenTypes.class, Target.class, Aliasing.spring())
        .orElseGet(Assertions::fail)
        .value()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("return type");
  }
}
