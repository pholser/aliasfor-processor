package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class SpringAliasing implements Aliasing {
  private static final String ALIAS_FOR_FQCN =
    "org.springframework.core.annotation.AliasFor";

  private final
  ConcurrentMap<Class<? extends Annotation>, IntraAliasModel> intraCache =
    new ConcurrentHashMap<>();

  private volatile Class<? extends Annotation> aliasForTypeCache;

  private record Node(Class<? extends Annotation> annoType, String attrName) {
  }

  private record OverrideKey(
    Class<? extends Annotation> annoType,
    String attrName) {
  }

  @Override
  public <A extends Annotation> Optional<A> synthesize(
    Class<A> annoType,
    List<Annotation> metaContext) {

    Class<? extends Annotation> aliasForType =
      aliasForType(annoType.getClassLoader());
    if (aliasForType == null) {
      return Optional.empty();
    }

    Annotation directInstance = null;
    for (Annotation meta : metaContext) {
      if (meta.annotationType() == annoType) {
        directInstance = meta;
        break;
      }
    }

    Map<Node, Node> edges = buildAliasEdges(metaContext, aliasForType);

    Map<String, Object> overridesIntoTarget = new LinkedHashMap<>();

    for (Annotation meta : metaContext) {
      Class<? extends Annotation> metaType = meta.annotationType();

      if (directInstance == null || metaType != annoType) {
        for (Method attr : metaType.getDeclaredMethods()) {
          Object actual = invoke(meta, attr);
          Object def = attr.getDefaultValue();

          if (!Objects.deepEquals(actual, def)) {
            Node start = new Node(metaType, attr.getName());
            Node terminal = followToTerminal(edges, start);

            if (terminal.annoType() == annoType) {
              mergeFirstWins(
                overridesIntoTarget,
                terminal.attrName(),
                actual,
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
      Map<String, Object> overridesIntra =
        computeIntraAliasedOverrides(annoType, directInstance, aliasForType);

      if (!overridesIntra.isEmpty()) {
        return Optional.of(
          SynthesizedAnnotations.of(annoType, overridesIntra));
      }
    }

    return Optional.empty();
  }

  private static Map<Node, Node> buildAliasEdges(
    List<Annotation> metaContext,
    Class<? extends Annotation> aliasForType) {

    Map<Node, Node> edges = new HashMap<>();

    for (Annotation a : metaContext) {
      Class<? extends Annotation> declaring = a.annotationType();

      for (Method m : declaring.getDeclaredMethods()) {
        Annotation aliasFor = m.getAnnotation(aliasForType);
        if (aliasFor != null) {
          String targetAttr = targetAttributeOf(aliasFor);
          Class<?> targetAnnoRaw = targetAnnoTypeOf(aliasFor);

          Class<? extends Annotation> targetAnno =
            resolveTargetAnno(
              declaring,
              targetAttr,
              targetAnnoRaw,
              metaContext);

          Node from = new Node(declaring, m.getName());
          Node to = new Node(targetAnno, targetAttr);

          edges.put(from, to);
        }
      }
    }

    return edges;
  }

  private static Class<? extends Annotation> resolveTargetAnno(
    Class<? extends Annotation> declaring,
    String targetAttr,
    Class<?> targetAnnoRaw,
    List<Annotation> metaContext) {

    if (targetAnnoRaw != null && targetAnnoRaw != Annotation.class) {
      @SuppressWarnings("unchecked")
      Class<? extends Annotation> cast =
        (Class<? extends Annotation>) targetAnnoRaw;
      return cast;
    }

    if (hasMemberNamed(declaring, targetAttr)) {
      return declaring;
    }

    List<Class<? extends Annotation>> matches =
      implicitMetaTargets(declaring, targetAttr, metaContext);

    if (matches.isEmpty()) {
      throw new IllegalStateException(
        "Could not resolve implicit @AliasFor target for @"
          + declaring.getName() + "." + targetAttr + "()");
    }

    if (matches.size() > 1) {
      throw new IllegalStateException(
        "Ambiguous implicit @AliasFor target for @"
          + declaring.getName() + "." + targetAttr + "(): "
          + matches);
    }

    return matches.get(0);
  }

  private static List<Class<? extends Annotation>> implicitMetaTargets(
    Class<? extends Annotation> declaring,
    String targetAttr,
    List<Annotation> metaContext) {

    List<Class<? extends Annotation>> direct =
      directMetaAnnotationsWithMember(declaring, targetAttr);

    if (direct.size() <= 1) {
      return direct;
    }

    if (metaContext == null || metaContext.isEmpty()) {
      return direct;
    }

    Set<Class<? extends Annotation>> ctx = new HashSet<>();
    for (Annotation a : metaContext) {
      ctx.add(a.annotationType());
    }

    List<Class<? extends Annotation>> narrowed = new ArrayList<>();
    for (Class<? extends Annotation> c : direct) {
      if (ctx.contains(c)) {
        narrowed.add(c);
      }
    }

    return narrowed.isEmpty() ? direct : narrowed;
  }

  private static List<Class<? extends Annotation>>
  directMetaAnnotationsWithMember(
    Class<? extends Annotation> declaring,
    String memberName) {

    List<Class<? extends Annotation>> matches = new ArrayList<>();

    for (Annotation meta : declaring.getAnnotations()) {
      Class<? extends Annotation> mt = meta.annotationType();

      if (mt.getName().startsWith("java.lang.annotation.")) {
        continue;
      }

      if (hasMemberNamed(mt, memberName)) {
        matches.add(mt);
      }
    }

    return matches;
  }

  private static boolean hasMemberNamed(
    Class<? extends Annotation> anno,
    String memberName) {

    try {
      Method m = anno.getDeclaredMethod(memberName);
      return m.getParameterCount() == 0 && m.getReturnType() != void.class;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  private static Node followToTerminal(Map<Node, Node> edges, Node start) {
    Node current = start;
    Set<Node> seen = new HashSet<>();

    while (true) {
      if (!seen.add(current)) {
        throw new IllegalStateException(
          "Detected alias cycle starting at " + start);
      }

      Node next = edges.get(current);
      if (next == null) {
        return current;
      }

      current = next;
    }
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

    for (Method m : annoType.getDeclaredMethods()) {
      if (m.getParameterCount() != 0 || m.getReturnType() == void.class) {
        throw new IllegalArgumentException("Not an annotation member: " + m);
      }
    }

    UnionFind u = new UnionFind();
    Map<String, Method> members = new HashMap<>();

    Map<OverrideKey, String> firstByOverride = new HashMap<>();
    Map<String, String> explicitIntraEdges = new HashMap<>();

    for (Method m : annoType.getDeclaredMethods()) {
      String name = m.getName();
      members.put(name, m);

      Annotation aliasFor = m.getAnnotation(aliasForType);
      if (aliasFor == null) {
        continue;
      }

      Class<?> targetAnnoRaw = targetAnnoTypeOf(aliasFor);
      String targetAttr = targetAttributeOf(aliasFor);

      if (targetAnnoRaw == null
        || targetAnnoRaw == Annotation.class
        || targetAnnoRaw == annoType) {

        explicitIntraEdges.put(name, targetAttr);
        u.union(name, targetAttr);
        continue;
      }

      if (targetAnnoRaw.isAnnotation()) {
        @SuppressWarnings("unchecked")
        Class<? extends Annotation> targetAnno =
          (Class<? extends Annotation>) targetAnnoRaw;

        OverrideKey key = new OverrideKey(targetAnno, targetAttr);
        String first = firstByOverride.putIfAbsent(key, name);
        if (first != null) {
          u.union(first, name);
        }
      }
    }

    Set<String> validatedPairs = new HashSet<>();

    for (Map.Entry<String, String> e : explicitIntraEdges.entrySet()) {
      String from = e.getKey();
      String to = e.getValue();

      Method fromM = members.get(from);
      Method toM = members.get(to);

      if (toM == null) {
        throw new IllegalStateException(
          "Invalid @AliasFor on @" + annoType.getName() + "." + from
            + "(): target member '" + to + "' does not exist");
      }

      String back = explicitIntraEdges.get(to);
      if (!from.equals(back)) {
        throw new IllegalStateException(
          "Non-mirrored @AliasFor (mirror required) on @"
            + annoType.getName() + ": '" + from + "' aliases '"
            + to + "' but '" + to + "' does not alias '" + from
            + "'");
      }

      String pairKey =
        from.compareTo(to) <= 0
          ? (from + "<->" + to)
          : (to + "<->" + from);

      if (!validatedPairs.add(pairKey)) {
        continue;
      }

      if (!fromM.getReturnType().equals(toM.getReturnType())) {
        throw new IllegalStateException(
          "Incompatible @AliasFor pair on @" + annoType.getName()
            + ": '" + from + "' and '" + to
            + "' must declare the same return type");
      }

      Object d1 = fromM.getDefaultValue();
      Object d2 = toM.getDefaultValue();
      if (!Objects.deepEquals(d1, d2)) {
        throw new IllegalStateException(
          "Incompatible @AliasFor pair on @" + annoType.getName()
            + ": '" + from + "' and '" + to
            + "' must declare the same default value");
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
                + annoType.getName() + ": '" + chosenFrom + "' vs '"
                + attrName + "'");
          }
        }
      }

      if (chosenFrom != null) {
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
      throw new IllegalStateException(
        "@AliasFor declares both attribute and value");
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
