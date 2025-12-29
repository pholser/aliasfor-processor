package com.pholser.annogami;

import java.util.HashMap;
import java.util.Map;

final class UnionFind {
  private final Map<String, String> parent = new HashMap<>();

  String find(String s) {
    parent.putIfAbsent(s, s);
    String p = parent.get(s);
    if (p.equals(s)) {
      return s;
    }

    String r = find(p);
    parent.put(s, r);
    return r;
  }

  void union(String a, String b) {
    String groupA = find(a);
    String groupB = find(b);
    if (!groupA.equals(groupB)) {
      parent.put(groupA, groupB);
    }
  }
}
