package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

public sealed interface All
  permits Direct, Present, MetaSingleAll {

  List<Annotation> all(AnnotatedElement target);
}
