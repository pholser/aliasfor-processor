package com.pholser.annogami.pkg;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME) public @interface A {
  int value();
}
