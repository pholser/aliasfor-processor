package com.pholser.annogami;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.fail;

final class AnnotationAssertions {
  private AnnotationAssertions() {
    throw new AssertionError();
  }

  static void falseFind(Annotation a) {
    fail("Should not have found " + a);
  }
}