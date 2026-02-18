package com.pholser.annogami.contract.allbytype;

import com.pholser.annogami.AllByType;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

abstract class AllByTypeContractTest {
  protected abstract AllByType subject();

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

  @Test final void findsDirectlyPresentAnnotationByType() {
    List<A> found = subject().find(A.class, HasA.class);
    assertThat(found).extracting(A::value).containsExactly(3);
  }

  @Test final void returnsEmptyListForAbsentType() {
    assertThat(subject().find(A.class, HasNothing.class)).isEmpty();
  }

  @Test final void expandsRepeatableIntoIndividualElements() {
    List<B> found = subject().find(B.class, HasRepeatableB.class);
    assertThat(found).extracting(B::value).containsExactlyInAnyOrder(4, 5);
  }

  @Test final void metaAnnotationsFoundOnlyByMetaImplementations() {
    List<Base> found = subject().find(Base.class, HasComposed.class);
    assertThat(found.isEmpty()).isEqualTo(!supportsMeta());
  }

  @Test final void inheritedAnnotationsFoundOnlyByAssociatedImplementations() {
    List<Inh> found = subject().find(Inh.class, InhDerived.class);
    assertThat(found.isEmpty()).isEqualTo(!honorsInherited());
  }
}
