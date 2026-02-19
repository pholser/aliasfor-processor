package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Objects;

public sealed interface All
  permits Direct, Present, MetaSingleAll {

  List<Annotation> all(AnnotatedElement target);

  default List<Annotation> all(
    AnnotatedElement target,
    Aliasing aliasing) {

    Objects.requireNonNull(aliasing, "aliasing");

    return SegmentResolver.defaults().all(target, all(target), aliasing);
  }
}
