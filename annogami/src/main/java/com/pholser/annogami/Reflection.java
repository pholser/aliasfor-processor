package com.pholser.annogami;

import java.lang.reflect.Method;
import java.util.Optional;

final class Reflection {
  private Reflection() {
    throw new AssertionError();
  }

  static Optional<Method> findMethod(
    Class<?> k,
    String name,
    Class<?>[] parameterTypes) {

    try {
      return Optional.of(k.getDeclaredMethod(name, parameterTypes));
    } catch (NoSuchMethodException ex) {
      return Optional.empty();
    }
  }
}
