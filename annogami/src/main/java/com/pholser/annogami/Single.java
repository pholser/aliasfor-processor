package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

public sealed interface Single
  permits Direct, Present, MetaSingleAll {

  <A extends Annotation>
  Optional<A> find(Class<A> annoType, AnnotatedElement target);
}
