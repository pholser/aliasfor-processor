package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

public final class DirectOrIndirect {
  DirectOrIndirect() {
  }

  public <A extends Annotation> List<A> find(
    Class<A> annoType,
    AnnotatedElement target) {

    return List.of(target.getDeclaredAnnotationsByType(annoType));
  }
}
