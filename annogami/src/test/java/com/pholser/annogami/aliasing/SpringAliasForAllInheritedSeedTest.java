package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static com.pholser.annogami.Presences.META_PRESENT;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class SpringAliasForAllInheritedSeedTest {
  @Retention(RUNTIME) @Target(TYPE) @interface Base {
    String value() default "";
  }

  @Retention(RUNTIME) @Target(TYPE) @Inherited @Base @interface Composed {
    @AliasFor(annotation = Base.class, attribute = "value") String name() default "";
  }

  @Composed(name = "hello") static class InhBase {}

  static class InhDerived extends InhBase {}

  @Test void allWithAliasingUpgradesMetaAnnotationFromInheritedSeed() {
    List<Annotation> all = META_PRESENT.all(InhDerived.class, Aliasing.spring());

    Base base =
      all.stream()
        .filter(a -> a.annotationType() == Base.class)
        .map(Base.class::cast)
        .findFirst()
        .orElseGet(Assertions::fail);

    assertThat(base.value()).isEqualTo("hello");
  }
}
