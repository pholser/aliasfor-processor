package com.pholser.annogami;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class BreadthFirstMetaWalkerEmissionVsScanTest {
  @Retention(RUNTIME) @interface A { int value(); }

  @A(10) @Retention(RUNTIME) @interface HasA10 {}
  @A(20) @Retention(RUNTIME) @interface HasA20 {}

  @HasA10 @HasA20 static class Target {}

  @Test void manyTypeVisitsEmittedButSingleScan() {
    CountingSource counter = new CountingSource(Sources.DECLARED);
    MetaWalkConfig config =
      new MetaWalkConfig(
        Sources.DECLARED,
        counter,
        MetaWalkFilters::defaultDescend,
        MetaWalkFilters::defaultInclude,
        10,
        true);
    MetaWalker walker = new BreadthFirstMetaWalker(config);

    List<MetaVisit> visits = walker.walk(Target.class).toList();

    long visitCountA =
      visits.stream()
        .filter(v -> v.element().equals(A.class))
        .count();
    assertThat(visitCountA).isEqualTo(2);
    assertThat(counter.callsFor(A.class)).isEqualTo(1);
  }

  static final class CountingSource implements AnnotationSource {
    private final AnnotationSource delegate;
    private final Map<String, Integer> calls = new ConcurrentHashMap<>();

    CountingSource(AnnotationSource delegate) {
      this.delegate = delegate;
    }

    @Override public Annotation[] all(AnnotatedElement element) {
      if (element instanceof Class<?> c && c.isAnnotation()) {
        calls.merge(c.getName(), 1, Integer::sum);
      }
      return delegate.all(element);
    }

    @Override public <T extends Annotation> T one(
      Class<T> type,
      AnnotatedElement element) {

      return delegate.one(type, element);
    }

    @Override public <T extends Annotation> T[] byType(
      Class<T> type,
      AnnotatedElement element) {

      return delegate.byType(type, element);
    }

    int callsFor(Class<? extends Annotation> annoType) {
      return calls.getOrDefault(annoType.getName(), 0);
    }
  }
}
