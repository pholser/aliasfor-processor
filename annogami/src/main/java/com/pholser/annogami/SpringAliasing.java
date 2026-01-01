package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class SpringAliasing implements Aliasing {
  private static final String ALIAS_FOR_FQCN =
    "org.springframework.core.annotation.AliasFor";

  private final
  ConcurrentMap<Class<? extends Annotation>, IntraAliasModel> intraCache =
    new ConcurrentHashMap<>();

  private volatile Class<? extends Annotation> aliasForTypeCache;

  @Override
  public <A extends Annotation> Optional<A> synthesize(
    Class<A> annoType,
    List<Annotation> metaContext) {

    Class<? extends Annotation> aliasForType =
      aliasForType(annoType.getClassLoader());
    if (aliasForType == null) {
      return Optional.empty();
    }

    Map<String, Object> overridesIntoTarget = new LinkedHashMap<>();
    Annotation directInstance = null;

    for (Annotation meta : metaContext) {
      if (meta.annotationType() == annoType) {
        directInstance = meta;
      }

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

            Object defaultVal = attr.getDefaultValue();
            if (!Objects.deepEquals(aliasedValue, defaultVal)) {
              mergeFirstWins(
                overridesIntoTarget,
                targetAttr,
                aliasedValue,
                annoType);
            }
          }
        }
      }
    }

    if (!overridesIntoTarget.isEmpty()) {
      return Optional.of(
        SynthesizedAnnotations.of(annoType, overridesIntoTarget));
    }

    if (directInstance != null) {
      var overridesIntra =
        computeIntraAliasedOverrides(annoType, directInstance, aliasForType);
      if (!overridesIntra.isEmpty()) {
        return Optional.of(
          SynthesizedAnnotations.of(annoType, overridesIntra));
      }
    }

    return Optional.empty();
  }

  private Class<? extends Annotation> aliasForType(ClassLoader loader) {
    Class<? extends Annotation> cached = aliasForTypeCache;
    if (cached != null) {
      return cached;
    }

    Class<? extends Annotation> loaded = loadAliasForType(loader);
    aliasForTypeCache = loaded;
    return loaded;
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

  private IntraAliasModel intraModelFor(
    Class<? extends Annotation> annoType,
    Class<? extends Annotation> aliasForType) {

    return intraCache.computeIfAbsent(
      annoType,
      t -> buildIntraAliasModel(t, aliasForType));
  }

  private static IntraAliasModel buildIntraAliasModel(
    Class<? extends Annotation> annoType,
    Class<? extends Annotation> aliasForType) {

    // Ensure only member methods exist (mirrors what your handler enforces)
    for (Method m : annoType.getDeclaredMethods()) {
      if (m.getParameterCount() != 0 || m.getReturnType() == void.class) {
        throw new IllegalArgumentException("Not an annotation member: " + m);
      }
    }

    UnionFind u = new UnionFind();
    Map<String, Method> members = new HashMap<>();
    Map<AttrOverrideKey, String> firstByOverrideKey = new HashMap<>();

    for (Method m : annoType.getDeclaredMethods()) {
      String name = m.getName();
      members.put(name, m);

      Annotation aliasFor = m.getAnnotation(aliasForType);
      if (aliasFor != null) {
        Class<?> targetAnno = targetAnnoTypeOf(aliasFor);

        if (targetAnno == null
          || targetAnno == Annotation.class
          || targetAnno == annoType) {

          String targetAttr = targetAttributeOf(aliasFor);
          u.union(name, targetAttr);
        } else if (targetAnno.isAnnotation()) {
          String targetAttr = targetAttributeOf(aliasFor);
          AttrOverrideKey key =
            new AttrOverrideKey(
              targetAnno.asSubclass(Annotation.class),
              targetAttr);

          String first = firstByOverrideKey.putIfAbsent(key, name);
          if (first != null) {
            u.union(first, name);
          }
        }
      }
    }

    Map<String, List<String>> groups = new LinkedHashMap<>();
    List<String> names = new ArrayList<>(members.keySet());
    names.sort(Comparator.naturalOrder());
    for (String name : names) {
      String root = u.find(name);
      groups.computeIfAbsent(root, r -> new ArrayList<>()).add(name);
    }

    List<List<String>> aliasGroups =
      groups.values().stream()
        .filter(g -> g.size() > 1)
        .map(List::copyOf)
        .toList();

    return new IntraAliasModel(members, aliasGroups);
  }

  private Map<String, Object> computeIntraAliasedOverrides(
    Class<? extends Annotation> annoType,
    Annotation instance,
    Class<? extends Annotation> aliasForType) {

    Map<String, Object> overrides = new LinkedHashMap<>();

    IntraAliasModel model = intraModelFor(annoType, aliasForType);
    for (List<String> group : model.aliasGroups()) {
      Object chosen = null;
      String chosenFrom = null;

      for (String attrName : group) {
        Method m = model.membersByName().get(attrName);

        Object actual = invoke(instance, m);
        Object def = m.getDefaultValue();

        if (!Objects.deepEquals(actual, def)) {
          if (chosenFrom == null) {
            chosenFrom = attrName;
            chosen = actual;
          } else if (!Objects.deepEquals(chosen, actual)) {
            throw new IllegalStateException(
              "Conflicting explicit values for aliased attributes on @"
                + annoType.getName() + ": '" + chosenFrom + "' vs '" + attrName + "'");
          }
        }
      }

      if (chosenFrom != null) {
        // Apply chosen explicit value as override for ALL members in the group
        for (String attrName : group) {
          overrides.put(attrName, chosen);
        }
      }
    }

    return Map.copyOf(overrides);
  }

  private static void mergeFirstWins(
    Map<String, Object> into,
    String key,
    Object value,
    Class<? extends Annotation> annoType) {

    if (into.containsKey(key)) {
      Object existing = into.get(key);

      if (!Objects.deepEquals(existing, value)) {
        throw new IllegalStateException(
          "Conflicting values for aliased attribute '" + key + "' of @"
            + annoType.getName() + ": " + existing + " vs " + value);
      }
    } else {
      into.put(key, value);
    }
  }

  private static Class<?> targetAnnoTypeOf(Annotation aliasFor) {
    return findAttrMethod(aliasFor.annotationType(), "annotation")
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
    return findAttrMethod(a.annotationType(), methodName)
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

  private static Optional<Method> findAttrMethod(Class<?> k, String name) {
    try {
      return Optional.of(k.getMethod(name));
    } catch (NoSuchMethodException e) {
      return Optional.empty();
    }
  }

  private record AttrOverrideKey(
    Class<? extends Annotation> annoType,
    String attrName) {
  }

  private static final class IntraAliasModel {
    private final Map<String, Method> membersByName;
    private final List<List<String>> aliasGroups;

    IntraAliasModel(
      Map<String, Method> membersByName,
      List<List<String>> aliasGroups) {

      this.membersByName = Map.copyOf(membersByName);
      this.aliasGroups = List.copyOf(aliasGroups);
    }

    Map<String, Method> membersByName() {
      return membersByName;
    }

    List<List<String>> aliasGroups() {
      return aliasGroups;
    }
  }
}
