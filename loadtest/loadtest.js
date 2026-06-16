// Test obciążeniowy do porównania pul wątków (platformowe vs wirtualne).
//
// Scenariusz: rampa współbieżnych użytkowników uderzających w endpoint
// czytający z bazy (GET /api/projects/page). W setup() rejestrujemy testowego
// użytkownika (idempotentnie) i logujemy się po token JWT.
//
// Uruchomienie (osobno dla każdego wariantu A/B/C — patrz README.md):
//   k6 run loadtest/loadtest.js
//
// Konfiguracja przez zmienne środowiskowe (-e KLUCZ=wartosc):
//   BASE_URL   - adres backendu          (domyślnie http://localhost:8080)
//   USERNAME   - login testowy           (domyślnie loadtest)
//   PASSWORD   - hasło testowe           (domyślnie loadtest123)
//   MAX_VUS    - szczyt współbieżności   (domyślnie 1000)

import http from 'k6/http';
import { check, fail } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const USERNAME = __ENV.USERNAME || 'loadtest';
const PASSWORD = __ENV.PASSWORD || 'loadtest123';
const MAX_VUS = parseInt(__ENV.MAX_VUS || '1000', 10);

const JSON_HEADERS = { 'Content-Type': 'application/json' };

const projectsLatency = new Trend('projects_page_latency', true);

export const options = {
  scenarios: {
    ramp: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: Math.round(MAX_VUS * 0.1) },
        { duration: '1m', target: Math.round(MAX_VUS * 0.5) },
        { duration: '1m', target: MAX_VUS },
        { duration: '1m', target: MAX_VUS },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'projects_page_latency': ['p(95)<800', 'p(99)<2000'],
  },
};

// Rejestracja (jeśli user istnieje, backend zwróci błąd unikalności – ignorujemy)
// + logowanie po token. Token trafia do każdego VU przez zwrotkę setup().
export function setup() {
  http.post(
    `${BASE_URL}/api/auth/register`,
    JSON.stringify({
      username: USERNAME,
      email: `${USERNAME}@example.com`,
      firstName: 'Load',
      lastName: 'Test',
      password: PASSWORD,
    }),
    { headers: JSON_HEADERS }
  );

  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ username: USERNAME, password: PASSWORD }),
    { headers: JSON_HEADERS }
  );

  if (res.status !== 200) {
    fail(`Logowanie nie powiodło się (status ${res.status}). Czy backend działa pod ${BASE_URL}?`);
  }

  return { token: res.json('token') };
}

export default function (data) {
  const res = http.get(`${BASE_URL}/api/projects/page?page=0&size=10`, {
    headers: { Authorization: `Bearer ${data.token}` },
    tags: { name: 'projects_page' },
  });

  projectsLatency.add(res.timings.duration);
  check(res, { 'status 200': (r) => r.status === 200 });
}
