package com.pholser.annogami.meta.indirect;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.util.List;

import static com.pholser.annogami.Presences.META_DIRECT_OR_INDIRECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class MetaDirectOrIndirectMultiLevelRepeatableTest {
  @Retention(RUNTIME) @interface Bs { B[] value(); }
  @Retention(RUNTIME) @Repeatable(Bs.class) @interface B { int value(); }

  @B(100) @Retention(RUNTIME) @interface HasB100 {}
  @HasB100 @B(200) static class Target {}

  @Test void findsRepeatableOccurrencesOnTargetAndOnMeta() {
    List<B> bs = META_DIRECT_OR_INDIRECT.find(B.class, Target.class);

    assertThat(bs)
      .extracting(B::value)
      .contains(100, 200);
  }
}
