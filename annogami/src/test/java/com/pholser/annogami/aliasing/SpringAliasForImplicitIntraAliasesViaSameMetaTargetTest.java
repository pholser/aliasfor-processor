package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;

import static com.pholser.annogami.Presences.DIRECT;
import static com.pholser.annogami.Presences.META_DIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringAliasForImplicitIntraAliasesViaSameMetaTargetTest {
  @Retention(RUNTIME) @interface Base {
    String value() default "";
  }

  @Retention(RUNTIME) @Base @interface Composed {
    @AliasFor(annotation = Base.class, attribute = "value")
    String name() default "";

    @AliasFor(annotation = Base.class, attribute = "value")
    String value() default "";
  }

  @Composed(name = "hello") static class TargetNameOnly {}

  @Composed(value = "hello") static class TargetValueOnly {}

  @Composed(name = "one", value = "two") static class TargetConflict {}

  @Test void settingNameAlsoSetsValueBecauseTheyAreImplicitAliases() {
    Composed c =
      DIRECT.find(Composed.class, TargetNameOnly.class, Aliasing.spring())
        .orElseGet(Assertions::fail);

    assertThat(c.name()).isEqualTo("hello");
    assertThat(c.value()).isEqualTo("hello");
  }

  @Test void settingValueAlsoSetsNameBecauseTheyAreImplicitAliases() {
    Composed c =
      DIRECT.find(Composed.class, TargetValueOnly.class, Aliasing.spring())
        .orElseGet(Assertions::fail);

    assertThat(c.value()).isEqualTo("hello");
    assertThat(c.name()).isEqualTo("hello");
  }

  @Test void conflictingExplicitValuesOnImplicitAliasesFailFast() {
    assertThatThrownBy(() ->
      DIRECT.find(Composed.class, TargetConflict.class, Aliasing.spring())
        .orElseGet(Assertions::fail)
        .value()
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test void metaViewAlsoSeesTheResolvedValue() {
    Base b =
      META_DIRECT.find(Base.class, TargetNameOnly.class, Aliasing.spring())
        .orElseGet(Assertions::fail);

    assertThat(b.value()).isEqualTo("hello");
  }
}
