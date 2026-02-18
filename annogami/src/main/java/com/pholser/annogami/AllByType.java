package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Objects;

public sealed interface AllByType
  permits DirectOrIndirect, Associated, MetaAllByType {

  <A extends Annotation>
  List<A> find(Class<A> annoType, AnnotatedElement target);

  default <A extends Annotation> List<A> find(
    Class<A> annoType,
    AnnotatedElement target,
    Aliasing aliasing) {

    Objects.requireNonNull(aliasing, "aliasing");

    return find(annoType, target);
  }
}
