package com.pholser.annogami.meta.indirect;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.util.List;

import static com.pholser.annogami.Presences.META_DIRECT_OR_INDIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class MetaDirectOrIndirectPresenceOnClassTest {
  @Retention(RUNTIME) @interface Bs {
    B[] value();
  }

  @Retention(RUNTIME) @Repeatable(Bs.class) @interface B {
    int value();
  }

  @B(1) @B(2) @Retention(RUNTIME) @interface HasBs {}

  @HasBs static class Target {}

  @Test void findsRepeatableElementAnnotationsViaMetaWalk() {
    List<B> bs = META_DIRECT_OR_INDIRECT.find(B.class, Target.class);

    assertThat(bs)
      .extracting(B::value)
      .containsExactlyInAnyOrder(1, 2);
  }

  @Test void findsContainerAnnotationViaMetaWalkWhenRequested() {
    List<Bs> containers = META_DIRECT_OR_INDIRECT.find(Bs.class, Target.class);

    assertThat(containers).hasSize(1);
    assertThat(containers.get(0).value())
      .extracting(B::value)
      .containsExactlyInAnyOrder(1, 2);
  }
}
