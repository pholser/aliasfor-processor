package com.pholser.annogami.indirect;

import com.pholser.annogami.ModuleMarker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.pholser.annogami.Presences.DIRECT_OR_INDIRECT;
import static org.assertj.core.api.Assertions.assertThat;

class DirectOrIndirectPresenceOnModuleTest {
  // When we add in JPMS modularity for annogami,
  // add a test that finds module markers successfully

  @Test void missesNotDeclared() {
    List<ModuleMarker> markers =
      DIRECT_OR_INDIRECT.find(ModuleMarker.class, String.class.getModule());

    assertThat(markers).isEmpty();
  }
}