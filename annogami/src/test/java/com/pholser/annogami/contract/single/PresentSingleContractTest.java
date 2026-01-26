package com.pholser.annogami.contract.single;

import com.pholser.annogami.Single;

import static com.pholser.annogami.Presences.PRESENT;

final class PresentSingleContractTest extends SingleContractTest {
  @Override protected Single subject() {
    return PRESENT;
  }

  @Override protected boolean supportsMeta() {
    return false;
  }

  @Override protected boolean honorsInherited() {
    return true;
  }
}
