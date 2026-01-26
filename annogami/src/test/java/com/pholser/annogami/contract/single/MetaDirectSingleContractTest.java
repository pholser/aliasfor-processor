package com.pholser.annogami.contract.single;

import com.pholser.annogami.Single;

import static com.pholser.annogami.Presences.META_DIRECT;

final class MetaDirectSingleContractTest extends SingleContractTest {
  @Override protected Single subject() {
    return META_DIRECT;
  }

  @Override protected boolean supportsMeta() {
    return true;
  }

  @Override protected boolean honorsInherited() {
    return false;
  }
}
