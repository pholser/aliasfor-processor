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

  @interface A { int value(); }
  @A(1) @interface B { int value(); }
  @A(2) @interface C { int value(); }
}
