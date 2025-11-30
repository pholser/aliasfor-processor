package com.pholser.annogami;

public final class Presences {
  public static final Direct DIRECT = new Direct();
  public static final DirectOrIndirect DIRECT_OR_INDIRECT =
    new DirectOrIndirect();

  private Presences() {
    throw new AssertionError();
  }
}
