#!/usr/bin/env python3
"""Smoke fonctionnel BookHub — couvre les routes API principales."""

import json
import sys
import time
import urllib.error
import urllib.request

API = "http://localhost:8080/api"
PWD = "BookHub!2026a"


def record(results, name, ok, detail=""):
    results.append((name, ok, detail))
    sym = "OK" if ok else "FAIL"
    print(f"[{sym}] {name}" + (f" — {detail}" if detail else ""))


def req(results, method, path, token=None, body=None, expected=(200,)):
    url = API + path
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    data = json.dumps(body).encode() if body is not None else None
    r = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(r, timeout=30) as resp:
            code = resp.getcode()
            raw = resp.read().decode() or "{}"
            ok = code in expected
            record(results, f"{method} {path}", ok, f"HTTP {code}" if not ok else "")
            if raw.strip().startswith(("{", "[")):
                return code, json.loads(raw)
            return code, raw
    except urllib.error.HTTPError as e:
        code = e.code
        ok = code in expected
        detail = f"HTTP {code}"
        try:
            detail += " " + e.read().decode()[:120]
        except OSError:
            pass
        record(results, f"{method} {path}", ok, detail if not ok else f"HTTP {code} (attendu)")
        return code, None


def login(results, email):
    _, data = req(results, "POST", "/auth/login", body={"email": email, "password": PWD})
    return data["token"] if data else None


def main() -> int:
    results: list[tuple[str, bool, str]] = []

    req(results, "GET", "/books")
    req(results, "GET", "/books/1")
    req(results, "GET", "/books/search?q=1984")
    req(results, "GET", "/categories")
    req(results, "GET", "/books/999999", expected=(404,))

    email = f"smoke.{int(time.time())}@example.fr"
    req(
        results,
        "POST",
        "/auth/register",
        body={
            "firstName": "Smoke",
            "lastName": "Test",
            "email": email,
            "password": PWD,
            "consent": True,
        },
        expected=(201,),
    )
    record(results, "login nouveau compte", bool(login(results, email)))
    req(results, "POST", "/auth/login", body={"email": email, "password": "wrong"}, expected=(401,))

    tok_marie = login(results, "marie.lectrice@example.fr")
    tok_ahmed = login(results, "ahmed.biblio@quartier-solidaire.fr")
    tok_sophie = login(results, "sophie.admin@quartier-solidaire.fr")
    record(results, "comptes seed", all([tok_marie, tok_ahmed, tok_sophie]))

    req(results, "GET", "/users/me", token=tok_marie)
    req(results, "GET", "/loans/my", token=tok_marie)
    req(results, "GET", "/reservations/my", token=tok_marie)
    req(results, "GET", "/dashboard/reader", token=tok_marie)
    req(results, "GET", "/ratings/books/2/context", token=tok_marie)
    req(results, "POST", "/loans", token=tok_marie, body={"bookId": 4}, expected=(409,))

    req(results, "GET", "/librarian/access-check", token=tok_marie, expected=(403,))
    req(results, "GET", "/admin/dashboard", token=tok_marie, expected=(403,))

    req(results, "GET", "/librarian/access-check", token=tok_ahmed)
    req(results, "GET", "/librarian/dashboard", token=tok_ahmed)
    req(results, "GET", "/librarian/loans/outstanding", token=tok_ahmed)
    req(results, "GET", "/librarian/loans/overdue", token=tok_ahmed)
    req(results, "GET", "/librarian/reservations/ready", token=tok_ahmed)
    req(results, "POST", "/librarian/reservations/expire", token=tok_ahmed, body=None)
    req(results, "GET", "/librarian/reviews", token=tok_ahmed)
    req(results, "GET", "/librarian/books", token=tok_ahmed)

    req(results, "GET", "/admin/dashboard", token=tok_sophie)
    req(results, "GET", "/admin/users", token=tok_sophie)

    isbn = f"978-2-99-{int(time.time()) % 1000000}-7"
    _, created = req(
        results,
        "POST",
        "/books",
        token=tok_ahmed,
        body={
            "isbn": isbn,
            "title": "Smoke test livre",
            "author": "QA",
            "description": "temp",
            "publicationYear": 2024,
            "totalCopies": 1,
            "categoryId": 1,
        },
        expected=(201,),
    )
    if created and created.get("id"):
        bid = created["id"]
        req(results, "GET", f"/books/{bid}")
        req(
            results,
            "PUT",
            f"/books/{bid}",
            token=tok_ahmed,
            body={
                "isbn": isbn,
                "title": "Smoke test livre v2",
                "author": "QA",
                "description": "temp2",
                "publicationYear": 2024,
                "totalCopies": 1,
                "categoryId": 1,
            },
        )
        req(results, "DELETE", f"/books/{bid}", token=tok_sophie, expected=(204,))

    try:
        urllib.request.urlopen("http://localhost:8080/v3/api-docs", timeout=10)
        record(results, "GET /v3/api-docs", True)
    except OSError as e:
        record(results, "GET /v3/api-docs", False, str(e))

    failed = [r for r in results if not r[1]]
    print("\n=== BILAN API ===")
    print(f"Total: {len(results)} | OK: {len(results) - len(failed)} | Échecs: {len(failed)}")
    for name, _, detail in failed:
        print(f"  - {name}: {detail}")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
