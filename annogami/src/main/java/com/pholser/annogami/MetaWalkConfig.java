package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.function.Predicate;

record MetaWalkConfig(
  AnnotationSource startSource,
  AnnotationSource metaSource,
  Predicate<Class<? extends Annotation>> shouldDescendInto,
  Predicate<Class<? extends Annotation>> shouldIncludeInResults,
  int maxDepth,
  boolean includeStartElement) {

  MetaWalkConfig {
    Objects.requireNonNull(startSource);
    Objects.requireNonNull(metaSource);
    Objects.requireNonNull(shouldDescendInto);
    Objects.requireNonNull(shouldIncludeInResults);
    if (maxDepth < 0) {
      throw new IllegalArgumentException(
        "maxDepth must be >= 0, was " + maxDepth);
    }
  }

  static MetaWalkConfig defaultsDeclared() {
    return new MetaWalkConfig(
      Sources.DECLARED,
      Sources.DECLARED,
      MetaWalkFilters::defaultDescend,
      MetaWalkFilters::defaultInclude,
      256,
      true);
  }

  public static MetaWalkConfig defaultsPresentStart() {
    return new MetaWalkConfig(
      Sources.PRESENT,
      Sources.DECLARED,
      MetaWalkFilters::defaultDescend,
      MetaWalkFilters::defaultInclude,
      256,
      true);
  }
}
