package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Optional;

public final class Present implements SingleByType, All {
  Present() {
  }

  @Override public <A extends Annotation> Optional<A> find(
    Class<A> annoType,
    AnnotatedElement target) {

    return Optional.ofNullable(Sources.PRESENT.one(annoType, target));
  }

  @Override public List<Annotation> all(AnnotatedElement target) {
    return List.of(Sources.PRESENT.all(target));
  }
}
