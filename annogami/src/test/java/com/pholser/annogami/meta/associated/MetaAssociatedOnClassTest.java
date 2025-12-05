package com.pholser.annogami.meta.associated;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.util.List;

import static com.pholser.annogami.Presences.META_ASSOCIATED;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class MetaAssociatedPresenceOnClassTest {
  @Retention(RUNTIME) @interface Bs {
    B[] value();
  }

  @Retention(RUNTIME) @Repeatable(Bs.class) @interface B {
    int value();
  }

  @B(4) @B(5) @Inherited @Retention(RUNTIME) @interface HasBs {}

  @HasBs static class Base {}
  static class Derived extends Base {}

  @Test void findsRepeatableElementAnnotations() {
    List<B> bs = META_ASSOCIATED.find(B.class, Derived.class);

    assertThat(bs)
      .extracting(B::value)
      .containsExactlyInAnyOrder(4, 5);
  }

  @Test void findsContainerAnnotationWhenRequested() {
    List<Bs> containers = META_ASSOCIATED.find(Bs.class, Derived.class);

    assertThat(containers).hasSize(1);
    assertThat(containers.get(0).value())
      .extracting(B::value)
      .containsExactlyInAnyOrder(4, 5);
  }
}
