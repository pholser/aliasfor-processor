package com.pholser.annogami;

import java.lang.reflect.AnnotatedElement;
import java.util.stream.Stream;

interface MetaWalker {
  Stream<MetaVisit> walk(AnnotatedElement start);
}
