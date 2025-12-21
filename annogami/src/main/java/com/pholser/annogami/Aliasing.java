package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public interface Aliasing {
  static Aliasing none() {
    return new NoAliasing();
  }

  static Aliasing spring() {
    return new SpringAliasing();
  }

  default String canonicalAttribute(
    Class<? extends Annotation> annoType,
    String attrName) {

    return attrName;
  }

  default <A extends Annotation> Optional<A> synthesize(
    Class<A> annoType,
    List<Annotation> metaContext) {

    return Optional.empty();
  }
}
