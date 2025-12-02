package com.pholser.annogami;

public final class Presences {
  public static final Direct DIRECT = new Direct();
  public static final DirectOrIndirect DIRECT_OR_INDIRECT =
    new DirectOrIndirect();
  public static final Present PRESENT = new Present();
  public static final Associated ASSOCIATED = new Associated();

  private Presences() {
    throw new AssertionError();
  }
}
