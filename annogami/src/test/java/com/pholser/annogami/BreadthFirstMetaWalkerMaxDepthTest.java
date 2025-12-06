package com.pholser.annogami;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class BreadthFirstMetaWalkerMaxDepthTest {
  @Retention(RUNTIME) @interface A {}

  @A @Retention(RUNTIME) @interface HasA {}

  @HasA static class Target {}

  @Test void maxDepthZeroOnlyIncludesStartVisit() {
    MetaWalkConfig config =
      new MetaWalkConfig(
        Sources.DECLARED,
        Sources.DECLARED,
        MetaWalkFilters::defaultDescend,
        MetaWalkFilters::defaultInclude,
        0,
        true);
    MetaWalker walker = new BreadthFirstMetaWalker(config);

    List<MetaVisit> visits = walker.walk(Target.class).toList();

    assertThat(visits).hasSize(1);
    assertThat(visits.get(0).element()).isEqualTo(Target.class);
  }

  @Test void maxDepthOneIncludesOnlyTypesDirectlyOnStart() {
    MetaWalkConfig config =
      new MetaWalkConfig(
        Sources.DECLARED,
        Sources.DECLARED,
        MetaWalkFilters::defaultDescend,
        MetaWalkFilters::defaultInclude,
        1,
        true);
    MetaWalker walker = new BreadthFirstMetaWalker(config);

    List<MetaVisit> types =
      walker.walk(Target.class)
      .filter(v -> v.element() instanceof Class<?> c && c.isAnnotation())
      .toList();

    assertThat(types)
      .extracting(v -> v.element().getClass())
      .isNotNull(); // no-op, just ensures the stream didn't blow up
    assertThat(types)
      .extracting(v -> ((Class<?>) v.element()).getName())
      .contains(HasA.class.getName())
      .doesNotContain(A.class.getName()); // depth 2 would be required to reach @A
  }
}
