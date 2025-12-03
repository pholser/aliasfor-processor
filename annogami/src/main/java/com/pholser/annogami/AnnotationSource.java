package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

interface AnnotationSource {
  Annotation[] all(AnnotatedElement target);

  <A extends Annotation> A one(Class<A> annoType, AnnotatedElement target);

  <A extends Annotation> A[] byType(
    Class<A> annoType,
    AnnotatedElement target);
}
