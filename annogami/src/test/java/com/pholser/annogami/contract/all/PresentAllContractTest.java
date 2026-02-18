package com.pholser.annogami.contract.all;

import com.pholser.annogami.All;

import static com.pholser.annogami.Presences.PRESENT;

final class PresentAllContractTest extends AllContractTest {
  @Override protected All subject() {
    return PRESENT;
  }

  @Override protected boolean supportsMeta() {
    return false;
  }

  @Override protected boolean honorsInherited() {
    return true;
  }
}
