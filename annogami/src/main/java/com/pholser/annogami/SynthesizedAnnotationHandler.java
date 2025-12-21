package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

final class SynthesizedAnnotationHandler implements InvocationHandler {
  private final Class<? extends Annotation> annoType;
  private final Map<String, Object> overrides;
  private final Method[] members; // cache

  SynthesizedAnnotationHandler(
    Class<? extends Annotation> annoType,
    Map<String, Object> overrides) {

    this.annoType = Objects.requireNonNull(annoType);
    this.overrides = Map.copyOf(Objects.requireNonNull(overrides));

    Method[] methods = annoType.getDeclaredMethods();
    for (Method m : methods) {
      if (m.getParameterCount() != 0 || m.getReturnType() == void.class) {
        throw new IllegalArgumentException("Not an annotation member: " + m);
      }
    }

    this.members = methods;
  }

  @Override public Object invoke(Object proxy, Method m, Object[] args)
    throws Throwable {

    if (isAnnotationType(m)) {
      return annoType;
    }
    if (isObjectMethod(m)) {
      if (isEquals(m)) {
        return handleEquals(proxy, args[0]);
      }
      if (isHashCode(m)) {
        return handleHashCode();
      }
      if (isToString(m)) {
        return handleToString();
      }
    }

    return valueOf(m);
  }

  private static boolean isObjectMethod(Method m) {
    return Object.class.equals(m.getDeclaringClass());
  }

  private static boolean isAnnotationType(Method m) {
    return "annotationType".equals(m.getName()) && m.getParameterCount() == 0;
  }

  private static boolean isEquals(Method m) {
    return "equals".equals(m.getName()) && m.getParameterCount() == 1;
  }

  private static boolean isHashCode(Method m) {
    return "hashCode".equals(m.getName()) && m.getParameterCount() == 0;
  }

  private static boolean isToString(Method m) {
    return "toString".equals(m.getName()) && m.getParameterCount() == 0;
  }

  private Object valueOf(Method m) {
    String n = m.getName();
    return overrides.containsKey(n) ? overrides.get(n) : m.getDefaultValue();
  }

  private boolean handleEquals(Object proxy, Object other) throws Exception {
    if (other == proxy) {
      return true;
    }
    if (!annoType.isInstance(other)) {
      return false;
    }

    for (Method m : members) {
      Object v1 = valueOf(m);
      Object v2 = m.invoke(other);
      if (!memberEquals(v1, v2)) {
        return false;
      }
    }

    return true;
  }

  private int handleHashCode() {
    int result = 0;
    for (Method m : members) {
      if (m.getParameterCount() != 0) continue;
      String name = m.getName();
      Object v = valueOf(m);
      result += (127 * name.hashCode()) ^ memberHashCode(v);
    }
    return result;
  }

  private String handleToString() {
    return "@"
      + annoType.getName()
      + "("
      + Arrays.stream(members)
        .map(m -> m.getName() + '=' + formatMemberValue(valueOf(m)))
        .collect(joining(", "))
      + ")";
  }

  private static boolean memberEquals(Object first, Object second) {
    if (first == second) {
      return true;
    }
    if (first == null || second == null) {
      return false;
    }

    if (first.getClass().isArray() && second.getClass().isArray()) {
      if (first instanceof Object[] a && second instanceof Object[] b) {
        return Arrays.equals(a, b);
      }
      if (first instanceof boolean[] a && second instanceof boolean[] b) {
        return Arrays.equals(a, b);
      }
      if (first instanceof byte[] a && second instanceof byte[] b) {
        return Arrays.equals(a, b);
      }
      if (first instanceof char[] a && second instanceof char[] b) {
        return Arrays.equals(a, b);
      }
      if (first instanceof double[] a && second instanceof double[] b) {
        return Arrays.equals(a, b);
      }
      if (first instanceof float[] a && second instanceof float[] b) {
        return Arrays.equals(a, b);
      }
      if (first instanceof int[] a && second instanceof int[] b) {
        return Arrays.equals(a, b);
      }
      if (first instanceof long[] a && second instanceof long[] b) {
        return Arrays.equals(a, b);
      }
      if (first instanceof short[] a && second instanceof short[] b) {
        return Arrays.equals(a, b);
      }
    }

    return first.equals(second);
  }

  private static int memberHashCode(Object v) {
    if (v == null) {
      return 0;
    }

    Class<?> k = v.getClass();
    if (k.isArray()) {
      if (v instanceof Object[] a) {
        return Arrays.hashCode(a);
      }
      if (v instanceof boolean[] a) {
        return Arrays.hashCode(a);
      }
      if (v instanceof byte[] a) {
        return Arrays.hashCode(a);
      }
      if (v instanceof char[] a) {
        return Arrays.hashCode(a);
      }
      if (v instanceof double[] a) {
        return Arrays.hashCode(a);
      }
      if (v instanceof float[] a) {
        return Arrays.hashCode(a);
      }
      if (v instanceof int[] a) {
        return Arrays.hashCode(a);
      }
      if (v instanceof long[] a) {
        return Arrays.hashCode(a);
      }
      if (v instanceof short[] a) {
        return Arrays.hashCode(a);
      }
    }

    return v.hashCode();
  }

  private static String formatMemberValue(Object v) {
    if (v == null) {
      return "null";
    }

    if (v.getClass().isArray()) {
      if (v instanceof Object[] a) {
        return Arrays.toString(a);
      }
      if (v instanceof int[] a) {
        return Arrays.toString(a);
      }
      if (v instanceof boolean[] a) {
        return Arrays.toString(a);
      }
      if (v instanceof byte[] a) {
        return Arrays.toString(a);
      }
      if (v instanceof char[] a) {
        return Arrays.toString(a);
      }
      if (v instanceof double[] a) {
        return Arrays.toString(a);
      }
      if (v instanceof float[] a) {
        return Arrays.toString(a);
      }
      if (v instanceof long[] a) {
        return Arrays.toString(a);
      }
      if (v instanceof short[] a) {
        return Arrays.toString(a);
      }
    }

    return String.valueOf(v);
  }
}
