package com.pholser.annogami;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

    Optional<A> direct = presence.find(annoType, segment);
    if (direct.isPresent()) {
      return aliasing.synthesize(annoType, buildMetaContext(segment))
        .or(() -> direct);
    }

    List<Annotation> metaContext = buildMetaContext(segment);
    return aliasing.synthesize(annoType, metaContext);
  }

  List<Annotation> all(
    AnnotatedElement segment,
    List<Annotation> base,
    Aliasing aliasing) {

    Objects.requireNonNull(segment, "segment");
    Objects.requireNonNull(base, "base");
    Objects.requireNonNull(aliasing, "aliasing");

    List<Annotation> ctx = buildMetaContext(segment);

    List<Annotation> out = new ArrayList<>(base.size());
    for (Annotation a : base) {
      Class<? extends Annotation> t = a.annotationType();
      Optional<? extends Annotation> synth = aliasing.synthesize(t, ctx);
      out.add(synth.isPresent() ? synth.get() : a);
    }

    return List.copyOf(out);
  }

  private List<Annotation> buildMetaContext(AnnotatedElement segment) {
    List<Annotation> result = new ArrayList<>();
    Set<Class<? extends Annotation>> seen = new HashSet<>();
    List<Class<? extends Annotation>> queue = new ArrayList<>();

    for (Annotation seed : Sources.DECLARED.all(segment)) {
      result.add(seed);
      queue.add(seed.annotationType());
    }

    for (int i = 0; i < queue.size(); i++) {
      Class<? extends Annotation> t = queue.get(i);

      if (seen.add(t)) {
        for (Annotation meta : Sources.DECLARED.all(t)) {
          Class<? extends Annotation> metaType = meta.annotationType();

          if (!metaType.getName().startsWith("java.lang.annotation.")) {
            result.add(meta);
            queue.add(metaType);
          }
        }
      }
    }

    return List.copyOf(result);
  }
}
