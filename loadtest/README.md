# Testy obciążeniowe — wątki wirtualne

Porównanie obsługi współbieżności przez backend przy różnych modelach wątków.
Cel testu **nie jest** taki, by aplikacja była „szybsza", lecz by sprawdzić, jak
znosi rosnącą liczbę jednoczesnych, blokujących na I/O żądań (JDBC do Postgresa).

## Wymagania

- [k6](https://k6.io/docs/get-started/installation/) (`brew install k6` / `choco install k6` / `apt`)
- Działający backend + Postgres (np. `docker compose up`)

## Warianty (profile Spring)

| Wariant | Profil | Wątki | `tomcat.threads.max` | Po co |
|---------|--------|-------|----------------------|-------|
| A | `vt-off` | platformowe | 200 (domyślne) | punkt odniesienia |
| B | `vt-pool` | platformowe | 20 | wymusza wąskie gardło puli |
| C | `vt-on` | **wirtualne** | — | model docelowy |

Wariant B jest kluczowy — bez sztucznego zacieśnienia puli różnica między A i C
przy umiarkowanym obciążeniu bywa niewidoczna.

## Uruchamianie

Dla każdego wariantu: (1) wystartuj backend z danym profilem, (2) odpal k6.

```bash
# Wariant A
SPRING_PROFILES_ACTIVE=vt-off ./mvnw -f app/pom.xml spring-boot:run
# w drugim terminalu:
k6 run loadtest/loadtest.js

# Wariant B
SPRING_PROFILES_ACTIVE=vt-pool ./mvnw -f app/pom.xml spring-boot:run
k6 run loadtest/loadtest.js

# Wariant C
SPRING_PROFILES_ACTIVE=vt-on ./mvnw -f app/pom.xml spring-boot:run
k6 run loadtest/loadtest.js
```

Parametry skryptu (zmienne `-e`):

```bash
k6 run -e BASE_URL=http://localhost:8080 -e MAX_VUS=2000 loadtest/loadtest.js
```

## Co obserwować

- **k6 (klient):** `http_req_duration` p95/p99, `http_req_failed`, throughput.
  Szukaj progu liczby VU, przy którym p99 zaczyna szybować.
- **Serwer (macie Prometheus + Grafanę w `charts/.../monitoring`):**
  - `jvm_threads_live_threads` — w wariancie A utknie ~200, w C poleci wyżej,
  - `hikaricp_connections_pending` — pokaże, że realnym gardłem jest pool DB,
  - `http_server_requests_seconds` (p99), CPU, GC.

## ⚠️ Pułapki interpretacyjne

1. **HikariCP (pool połączeń do bazy, domyślnie 10)** jest twardym limitem dla
   endpointów zależnych od DB. Wątki wirtualne go nie omijają — żeby zwiększyć
   realną przepustowość do bazy, podnieś też `spring.datasource.hikari.maximum-pool-size`
   (pilnując `max_connections` Postgresa). Mały zysk A vs C na tych endpointach to
   poprawny wynik, nie błąd.
2. **Pinning** — `synchronized` wokół blokującego wywołania przypina wątek wirtualny.
   Uruchom JVM z `-Djdk.tracePinnedThreads=short`, żeby to wykryć.
3. Testuj na puli osobnej maszyny/kontenera niż k6 — generator obciążenia nie może
   konkurować o CPU z aplikacją.
