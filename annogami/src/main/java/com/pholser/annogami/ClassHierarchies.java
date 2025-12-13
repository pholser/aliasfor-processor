package com.pholser.annogami;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.pholser.annogami.Reflection.findMethod;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Collectors.toList;

final class ClassHierarchies {
  private ClassHierarchies() {
    throw new AssertionError();
  }

  static List<Class<?>> depthFirstHierarchyOf(Class<?> k) {
    Deque<Class<?>> visitees = new ArrayDeque<>();
    visitees.push(k);

    LinkedHashSet<Class<?>> hierarchy = new LinkedHashSet<>();
    while (!visitees.isEmpty()) {
      Class<?> next = visitees.pop();
      if (next != k) {
        hierarchy.add(next);
      }

      for (int i = next.getInterfaces().length - 1; i >= 0; --i) {
        visitees.push(next.getInterfaces()[i]);
      }

      if (next.getSuperclass() != null) {
        visitees.push(next.getSuperclass());
      }
    }

    return new ArrayList<>(hierarchy);
  }

  static List<Class<?>> breadthFirstHierarchyOf(Class<?> k) {
    Queue<Class<?>> visitees = new LinkedList<>();
    visitees.add(k);

    LinkedHashSet<Class<?>> hierarchy = new LinkedHashSet<>();
    while (!visitees.isEmpty()) {
      Class<?> next = visitees.remove();
      if (next != k) {
        hierarchy.add(next);
      }

      if (next.getSuperclass() != null) {
        visitees.add(next.getSuperclass());
      }

      visitees.addAll(Arrays.asList(next.getInterfaces()));
    }

    return new ArrayList<>(hierarchy);
  }

  static List<Method> depthFirstOverrideHierarchyOf(Method m) {
    int mods = m.getModifiers();
    if (isStatic(mods) || isPrivate(mods)) {
      return Collections.emptyList();
    }

    return methodsOverriddenBy(
      m,
      depthFirstHierarchyOf(m.getDeclaringClass()));
  }

  static List<Method> breadthFirstOverrideHierarchyOf(Method m) {
    int mods = m.getModifiers();
    if (isStatic(mods) || isPrivate(mods)) {
      return Collections.emptyList();
    }

    return methodsOverriddenBy(
      m,
      breadthFirstHierarchyOf(m.getDeclaringClass()));
  }

  private static List<Method> methodsOverriddenBy(
    Method m,
    List<Class<?>> inClasses) {

    return inClasses
      .stream()
      .flatMap(k ->
        findMethod(k, m.getName(), m.getParameterTypes()).stream())
      .filter(sigMatch ->
        sigMatch.getReturnType().isAssignableFrom(m.getReturnType()))
      .filter(sigMatch -> overrides(m, sigMatch))
      .collect(toList());
  }

  /*
   * see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.4.8.1">JLS</a>
   */
  private static boolean overrides(Method m, Method sigMatch) {
    if (m.equals(sigMatch)) {
      return false;
    }

    int sigMatchMods = sigMatch.getModifiers();
    return !(isStatic(sigMatchMods) || isPrivate(sigMatchMods));
  }
}
