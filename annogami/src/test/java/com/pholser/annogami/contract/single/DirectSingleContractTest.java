package com.pholser.annogami.contract.single;

import com.pholser.annogami.Single;

import static com.pholser.annogami.Presences.DIRECT;

final class DirectSingleContractTest extends SingleContractTest {
  @Override protected Single subject() {
    return DIRECT;
  }

  @Override protected boolean supportsMeta() {
    return false;
  }

  @Override protected boolean honorsInherited() {
    return false;
  }
}
