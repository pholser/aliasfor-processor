package com.pholser.annogami;

import java.lang.annotation.Annotation;

class MetaWalkFilters {
  private MetaWalkFilters() {
    throw new AssertionError();
  }

  static boolean defaultDescend(Class<? extends Annotation> annoType) {
    return !annoType.getName().startsWith("java.lang.annotation.");
  }

  public static boolean defaultInclude(Class<? extends Annotation> annoType) {
    return !annoType.getName().startsWith("java.lang.annotation.");
  }
}
