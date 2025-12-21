package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class SpringAliasing implements Aliasing {
  private static final String ALIAS_FOR_FQCN =
    "org.springframework.core.annotation.AliasFor";

  @Override
  public <A extends Annotation> Optional<A> synthesize(
    Class<A> annoType,
    List<Annotation> metaContext) {

    Class<? extends Annotation> aliasForType =
      loadAliasForType(annoType.getClassLoader());
    if (aliasForType == null) {
      return Optional.empty();
    }

    var overrides = new LinkedHashMap<String, Object>();

    for (Annotation meta : metaContext) {
      Class<? extends Annotation> metaType = meta.annotationType();
      for (Method attr : metaType.getDeclaredMethods()) {
        Annotation aliasFor = attr.getAnnotation(aliasForType);
        if (aliasFor != null) {
          Class<?> targetAnnoType = targetAnnoTypeOf(aliasFor);
          if (targetAnnoType != null
            && targetAnnoType != Annotation.class
            && targetAnnoType == annoType) {

            String targetAttr = targetAttributeOf(aliasFor);
            Object aliasedValue = invoke(meta, attr);

            overrides.merge(
              targetAttr,
              aliasedValue,
              (a, b) -> {
                if (!Objects.deepEquals(a, b)) {
                  throw new IllegalStateException(
                    "Conflicting values for aliased attribute '" + targetAttr
                      + "' of @" + annoType.getName() + ": "
                      + a + " vs " + b);
                }
                return a;
              });
          }
        }
      }
    }

    return overrides.isEmpty()
      ? Optional.empty()
      : Optional.of(SynthesizedAnnotations.of(annoType, overrides));
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends Annotation> loadAliasForType(
    ClassLoader loader) {

    try {
      Class<?> k = Class.forName(ALIAS_FOR_FQCN, false, loader);
      return k.isAnnotation() ? (Class<? extends Annotation>) k : null;
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private static Class<?> targetAnnoTypeOf(Annotation aliasFor) {
    return findMethod(aliasFor.annotationType(), "annotation")
      .map(m -> (Class<?>) invoke(aliasFor, m))
      .orElse(null);
  }

  private static String targetAttributeOf(Annotation aliasFor) {
    String attribute = readString(aliasFor, "attribute");
    String value = readString(aliasFor, "value");

    boolean attrSpecified = attribute != null && !attribute.isEmpty();
    boolean valueSpecified = value != null && !value.isEmpty();
    if (attrSpecified && valueSpecified) {
      throw new IllegalStateException("@AliasFor declares both attribute and value");
    }

    if (attrSpecified) {
      return attribute;
    }
    if (valueSpecified) {
      return value;
    }
    return "value";
  }

  private static String readString(Annotation a, String methodName) {
    return findMethod(a.annotationType(), methodName)
      .map(m -> (String) invoke(a, m))
      .orElse(null);
  }

  private static Object invoke(Annotation a, Method m) {
    try {
      if (!m.canAccess(a)) {
        m.trySetAccessible();
      }

      return m.invoke(a);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  private static Optional<Method> findMethod(Class<?> k, String name) {
    try {
      return Optional.of(k.getMethod(name));
    } catch (NoSuchMethodException e) {
      return Optional.empty();
    }
  }
}
