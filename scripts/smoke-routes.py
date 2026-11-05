#!/usr/bin/env python3
"""Vérifie que les routes Angular répondent (HTTP 200) — smoke léger SPA."""

import sys
import urllib.request

BASE = "http://localhost:4200"
ROUTES = [
    "/books",
    "/books/1",
    "/login",
    "/register",
    "/access-denied",
    "/nimporte-quoi",  # not-found
]

failed = []
for path in ROUTES:
    url = BASE + path
    try:
        with urllib.request.urlopen(url, timeout=15) as resp:
            ok = resp.getcode() == 200
    except OSError as e:
        ok = False
        err = str(e)
    else:
        err = ""
    sym = "OK" if ok else "FAIL"
    print(f"[{sym}] GET {path}" + (f" — {err}" if err else ""))
    if not ok:
        failed.append(path)

print(f"\nRoutes publiques: {len(ROUTES) - len(failed)}/{len(ROUTES)}")
sys.exit(1 if failed else 0)
