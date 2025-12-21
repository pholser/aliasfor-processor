package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class SegmentResolver {
  private final MetaWalker walker;

  private SegmentResolver(MetaWalker walker) {
    this.walker = walker;
  }

  static SegmentResolver defaults() {
    return new SegmentResolver(
      new BreadthFirstMetaWalker(
        MetaWalkConfig.defaultsDeclared()));
  }

  <A extends Annotation> Optional<A> findFirst(
    Class<A> annoType,
    AnnotatedElement segment,
    Single presence,
    Aliasing aliasing) {

    Objects.requireNonNull(annoType);
    Objects.requireNonNull(segment);
    Objects.requireNonNull(presence);
    Objects.requireNonNull(aliasing);

    return presence.find(annoType, segment)
      .map(a ->
        aliasing.synthesize(annoType, buildMetaContext(segment))
          .orElse(a));
  }

  private List<Annotation> buildMetaContext(AnnotatedElement segment) {
    List<Annotation> context = new ArrayList<>();

    for (Annotation seed : Sources.DECLARED.all(segment)) {
      context.add(seed);

      // walk meta-annotation types from seed.annotationType(), and include their instances
      walker.walk(seed.annotationType()).forEach(visit -> {
        AnnotatedElement el = visit.element();
        if (el instanceof Class<?> k && k.isAnnotation()) {
          @SuppressWarnings("unchecked")
          Class<? extends Annotation> annoType = (Class<? extends Annotation>) k;

          Optional.ofNullable(seed.annotationType().getAnnotation(annoType))
            .ifPresent(context::add);
        }
      });
    }

    return List.copyOf(context);
  }
}
