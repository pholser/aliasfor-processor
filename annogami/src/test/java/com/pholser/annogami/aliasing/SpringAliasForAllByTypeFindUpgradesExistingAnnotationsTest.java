package com.pholser.annogami.aliasing;

import com.pholser.annogami.Aliasing;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static com.pholser.annogami.Presences.META_DIRECT_OR_INDIRECT;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class SpringAliasForAllByTypeFindUpgradesExistingAnnotationsTest {
  @Retention(RUNTIME) @Target(TYPE) @interface Base {
    String value() default "";
  }

  @Retention(RUNTIME) @Target(TYPE) @Base @interface Composed {
    @AliasFor("value") String path() default "";
  }

  @Composed(path = "p") static class Subject {}

  @Test void findWithAliasingUpgradesReturnedAnnotationInstance() {
    List<Base> found =
      META_DIRECT_OR_INDIRECT.find(Base.class, Subject.class, Aliasing.spring());

    assertThat(found).hasSize(1);
    assertThat(found.get(0).value()).isEqualTo("p");
  }
}
