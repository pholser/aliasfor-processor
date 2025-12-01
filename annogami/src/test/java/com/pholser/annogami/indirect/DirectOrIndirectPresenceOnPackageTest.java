package com.pholser.annogami.indirect;

import com.pholser.annogami.pkg.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.pholser.annogami.Presences.DIRECT_OR_INDIRECT;
import static org.assertj.core.api.Assertions.assertThat;

class DirectOrIndirectPresenceOnPackageTest {
  @Test void findsOnPackage() {
    Package pkg =
      getClass().getClassLoader().getDefinedPackage(
        "com.pholser.annogami.pkg");

    List<A> as = DIRECT_OR_INDIRECT.find(A.class, pkg);
    assertThat(as).hasSize(1);

    A a = as.stream().findFirst().orElseGet(Assertions::fail);
    assertThat(a.value()).isEqualTo(42);
  }

  @Test void missesNotDeclared() {
    List<A> as = DIRECT_OR_INDIRECT.find(A.class, getClass().getPackage());

    assertThat(as).isEmpty();
  }
}
