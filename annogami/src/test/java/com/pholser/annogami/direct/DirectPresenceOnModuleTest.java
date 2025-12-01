package com.pholser.annogami.direct;

import com.pholser.annogami.AnnotationAssertions;
import com.pholser.annogami.ModuleMarker;
import org.junit.jupiter.api.Test;

import static com.pholser.annogami.Presences.DIRECT;

class DirectPresenceOnModuleTest {
  // When we add in JPMS modularity for annogami,
  // add a test that finds module markers successfully

  @Test void missesNotDeclared() {
    DIRECT.find(ModuleMarker.class, String.class.getModule())
      .ifPresent(AnnotationAssertions::falseFind);
  }
}
