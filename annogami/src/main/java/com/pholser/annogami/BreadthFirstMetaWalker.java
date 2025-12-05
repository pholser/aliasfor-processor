package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

final class BreadthFirstMetaWalker implements MetaWalker {
  private final MetaWalkConfig config;

  BreadthFirstMetaWalker(MetaWalkConfig config) {
    this.config = Objects.requireNonNull(config);
  }

  @Override
  public Stream<MetaVisit> walk(AnnotatedElement start) {
    Objects.requireNonNull(start);

    List<MetaVisit> out = new ArrayList<>();
    Deque<TypeVisit> queue = new ArrayDeque<>();

    if (config.includeStartElement()) {
      out.add(new StartVisit(start));
    }

    if (config.maxDepth() == 0) {
      return out.stream();
    }

    for (Annotation each : config.startSource().all(start)) {
      Class<? extends Annotation> t = each.annotationType();
      if (config.shouldDescendInto().test(t)) {
        queue.addLast(new TypeVisit(t, 1, List.of(t), each));
      }
    }

    Set<Class<? extends Annotation>> scannedTypes = new HashSet<>();

    while (!queue.isEmpty()) {
      TypeVisit visit = queue.removeFirst();
      Class<? extends Annotation> type = visit.annoType();

      if (config.shouldIncludeInResults().test(type)) {
        out.add(visit);
      }

      if (visit.depth() >= config.maxDepth()) {
        continue;
      }
      if (!scannedTypes.add(type)) {
        continue;
      }

      AnnotatedElement metaElement = type;
      for (Annotation meta : config.metaSource().all(metaElement)) {
        Class<? extends Annotation> metaType = meta.annotationType();
        if (!config.shouldDescendInto().test(metaType)) {
          continue;
        }

        queue.addLast(
          new TypeVisit(
            metaType,
            visit.depth() + 1,
            append(visit.path(), metaType),
            meta
          )
        );
      }
    }

    return out.stream();
  }

  private static List<Class<? extends Annotation>> append(
    List<Class<? extends Annotation>> path,
    Class<? extends Annotation> next) {

    List<Class<? extends Annotation>> p = new ArrayList<>(path);
    p.add(next);
    return List.copyOf(p);
  }
}
