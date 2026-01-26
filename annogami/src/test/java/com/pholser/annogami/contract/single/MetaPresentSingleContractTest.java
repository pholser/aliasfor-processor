package com.pholser.annogami.contract.single;

import com.pholser.annogami.Single;

import static com.pholser.annogami.Presences.META_PRESENT;

final class MetaPresentSingleContractTest extends SingleContractTest {
  @Override protected Single subject() {
    return META_PRESENT;
  }

  @Override protected boolean supportsMeta() {
    return true;
  }

  @Override protected boolean honorsInherited() {
    return true;
  }
}
