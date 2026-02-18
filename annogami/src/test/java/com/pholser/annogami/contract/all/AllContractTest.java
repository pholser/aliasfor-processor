package com.pholser.annogami.contract.all;

import com.pholser.annogami.All;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

abstract class AllContractTest {
  protected abstract All subject();

  protected abstract boolean supportsMeta();

  protected abstract boolean honorsInherited();

  @Retention(RUNTIME) @interface A {
    int value() default 0;
  }

  @Retention(RUNTIME) @interface Base {
    String value() default "";
  }

  @Retention(RUNTIME) @Base @interface Composed {
    String path() default "";
  }

  @Retention(RUNTIME) @interface Bs {
    B[] value();
  }

  @Retention(RUNTIME) @Repeatable(Bs.class) @interface B {
    int value() default 0;
  }

  @Retention(RUNTIME) @Inherited @interface Inh {
    int value() default 0;
  }

  @A(3) static class HasA {}

  static class HasNothing {}

  @B(4) @B(5) static class HasRepeatableB {}

  @Composed static class HasComposed {}

  @Inh(6) static class InhBase {}

  static class InhDerived extends InhBase {}

  @Test final void allIncludesDirectlyPresentAnnotation() {
    assertThat(subject().all(HasA.class))
      .extracting(a -> a.annotationType().getName())
      .contains(A.class.getName());
  }

  @Test final void allIsEmptyForUnannotatedElement() {
    assertThat(subject().all(HasNothing.class)).isEmpty();
  }

  @Test final void allReturnsRepeatableContainerNotElements() {
    assertThat(subject().all(HasRepeatableB.class))
      .extracting(a -> a.annotationType().getName())
      .contains(Bs.class.getName())
      .doesNotContain(B.class.getName());
  }

  @Test final void metaAnnotationsFoundOnlyByMetaImplementations() {
    boolean hasBase = subject().all(HasComposed.class).stream()
      .anyMatch(a -> a.annotationType() == Base.class);
    assertThat(hasBase).isEqualTo(supportsMeta());
  }

  @Test final void inheritedAnnotationsFoundOnlyByPresentImplementations() {
    boolean hasInh = subject().all(InhDerived.class).stream()
      .anyMatch(a -> a.annotationType() == Inh.class);
    assertThat(hasInh).isEqualTo(honorsInherited());
  }
}
