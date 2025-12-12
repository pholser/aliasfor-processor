package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

public final class Associated implements AllByType {
  Associated() {
  }

  @Override
  public <A extends Annotation> List<A> find(
    Class<A> annoType,
    AnnotatedElement target) {

    return List.of(Sources.PRESENT.byType(annoType, target));
  }
}
