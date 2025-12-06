package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

sealed interface MetaVisit permits StartVisit, TypeVisit {
  AnnotatedElement element();
}

record StartVisit(AnnotatedElement element) implements MetaVisit {
}

record TypeVisit(
  Class<? extends Annotation> type,
  int depth,
  List<Class<? extends Annotation>> path,
  Annotation via)
  implements MetaVisit {

  @Override public AnnotatedElement element() {
    return type;
  }
}
