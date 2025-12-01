package com.pholser.annogami;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.fail;

public final class AnnotationAssertions {
  private AnnotationAssertions() {
    throw new AssertionError();
  }

  public static void falseFind(Annotation a) {
    fail("Should not have found " + a);
  }
}