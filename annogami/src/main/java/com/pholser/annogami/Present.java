package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Optional;

public final class Present {
  Present() {
  }

  public <A extends Annotation> Optional<A> find(
    Class<A> annoType,
    AnnotatedElement target) {

    return Optional.ofNullable(Sources.PRESENT.one(annoType, target));
  }

  public List<Annotation> all(AnnotatedElement target) {
    return List.of(Sources.PRESENT.all(target));
  }
}
