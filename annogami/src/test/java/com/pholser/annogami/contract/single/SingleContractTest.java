package com.pholser.annogami.contract.single;

import com.pholser.annogami.Single;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

abstract class SingleContractTest {
  protected abstract Single subject();

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

  @A(3) static class HasA {
  }

  static class HasNoA {
  }

  @Composed(path = "p") static class HasComposed {
  }

  @B(4) @B(5) static class HasRepeatableB {
  }

  @Inh(6) static class InhBase {
  }

  static class InhDerived extends InhBase {
  }

  @Test final void findsDirectlyPresentAnnotationOnClass() {
    A a = subject().find(A.class, HasA.class).orElseThrow();
    assertThat(a.value()).isEqualTo(3);
  }

  @Test final void missesAbsentAnnotationOnClass() {
    assertThat(subject().find(A.class, HasNoA.class)).isEmpty();
  }

  @Test final void repeatableMemberTypeIsNotDirectlyPresent() {
    assertThat(subject().find(B.class, HasRepeatableB.class)).isEmpty();
  }

  @Test final void repeatableContainerIsDirectlyPresent() {
    Bs bs = subject().find(Bs.class, HasRepeatableB.class).orElseThrow();
    assertThat(bs.value()).hasSize(2);
  }

  @Test final void metaAnnotationIsFoundOnlyByMetaSingles() {
    boolean found = subject().find(Base.class, HasComposed.class).isPresent();
    assertThat(found).isEqualTo(supportsMeta());
  }

  @Test final void inheritedAnnotationIsFoundOnlyByPresentSingles() {
    boolean found = subject().find(Inh.class, InhDerived.class).isPresent();
    assertThat(found).isEqualTo(honorsInherited());
  }
}
