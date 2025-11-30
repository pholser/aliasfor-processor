package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

public final class DirectOrIndirect {
  public <A extends Annotation> List<A> find(
    Class<A> annoType,
    AnnotatedElement target) {

    return Arrays.asList(target.getDeclaredAnnotationsByType(annoType));
  }
}
