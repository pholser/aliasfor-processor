package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

final class Sources {
  private Sources() {
    throw new AssertionError();
  }

  static final AnnotationSource DECLARED = new AnnotationSource() {
    @Override public Annotation[] all(AnnotatedElement target) {
      return target.getDeclaredAnnotations();
    }

    @Override public <A extends Annotation> A one(
      Class<A> annoType,
      AnnotatedElement target) {

      return target.getDeclaredAnnotation(annoType);
    }

    @Override public <A extends Annotation> A[] byType(
      Class<A> annoType,
      AnnotatedElement target) {

      return target.getDeclaredAnnotationsByType(annoType);
    }
  };

  static final AnnotationSource PRESENT = new AnnotationSource() {
    @Override public Annotation[] all(AnnotatedElement target) {
      return target.getAnnotations();
    }

    @Override public <A extends Annotation> A one(
      Class<A> annoType,
      AnnotatedElement target) {

      return target.getAnnotation(annoType);
    }

    @Override public <A extends Annotation> A[] byType(
      Class<A> annoType,
      AnnotatedElement target) {

      return target.getAnnotationsByType(annoType);
    }
  };
}
