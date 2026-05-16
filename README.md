# projektAgile

nawet nie będę się bawić w accesstokeny

Arkusz:
https://utpedupl-my.sharepoint.com/:x:/g/personal/nikgeb000_o365_student_pbs_edu_pl/IQBk9ekMGGuKSZz4JBtHvlD2Ad9iiqXZNQLZGc55bGROTxs?e=c8fBlN

## Wymagania

Do uruchomienia projektu potrzebujesz:
- Docker
- Docker Compose

Opcjonalnie (uruchomienie w Kubernetes):
- minikube
- kubectl

## Uruchomienie projektu

Z głównego katalogu projektu uruchom:

```bash
docker compose up --build
```

## Development mode

Tryb developerski uruchamia aplikację bez przebudowywania obrazów Dockera po każdej zmianie w kodzie.

Start:
```bash
docker compose -f docker-compose.dev.yml up
```

---

## Skróty `make`

Wszystkie typowe polecenia są zapięte w [Makefile](Makefile). Lista:

```bash
make help              # pokazuje wszystkie cele
```

Najczęściej używane:

| Polecenie | Co robi |
|---|---|
| `make up` | Aplikuje manifesty (app + monitoring) |
| `make down` | Usuwa wszystko |
| `make reload` | Pełny cykl: `build` → `load` → `rollout` |
| `make pods` | Status podów w obu namespace'ach |
| `make logs-backend` | Tail logów backendu |
| `make logs-prev` | Logi z poprzedniej (rozbitej) instancji |
| `make psql` | psql w podzie postgresa |
| `make db-reset` | Wyczyść schemat + restart backendu |
| `make urls` | Wypisuje URL-e aplikacji i Grafany |
| `make grafana` | Otwiera Grafanę |
| `make forward-prometheus` | Port-forward Prometheusa na :9090 |
| `make load-test` | Wysyła ruch w pętli na frontend |

---

## Uruchomienie w Kubernetes (minikube)

### 1. Start klastra

```bash
minikube start
```

### 2. Zbuduj obrazy i załaduj do minikube

`docker compose build` taguje obrazy jako `projektagile-backend:1.0` i `projektagile-frontend:1.0` — dokładnie tak, jak oczekują tego manifesty w `k8s/`.

```bash
docker compose build
minikube image load projektagile-backend:1.0
minikube image load projektagile-frontend:1.0
```

> ⚠️ Jeśli przebudowujesz obrazy bez zmiany tagu, minikube nie nadpisze starych warstw. Wtedy najpierw:
> ```bash
> minikube image rm docker.io/library/projektagile-backend:1.0 docker.io/library/projektagile-frontend:1.0
> ```

### 3. Aplikuj manifesty

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/                  # aplikacja (backend, frontend, postgres, ingress, secret, configmap)
kubectl apply -f k8s/monitoring/       # Prometheus + Grafana
```

### 4. Sprawdź, czy wszystko działa

```bash
kubectl get pods -n projektagile
kubectl get pods -n monitoring
```

Każdy pod powinien być `Running` z `READY 1/1`. Pierwsze uruchomienie backendu może trwać ~70s (Spring Boot).

### 5. Wymuś redeploy po zmianie kodu

Sam tag `:1.0` się nie zmienia, więc Kubernetes nie zauważy nowego obrazu. Po przebudowaniu i przeładowaniu do minikube:

```bash
kubectl rollout restart deployment/backend deployment/frontend -n projektagile
```

## Dostęp w przeglądarce

`minikube ip` zwraca adres klastra (zwykle `192.168.49.2`).

| Co | URL | Jak |
|---|---|---|
| Aplikacja (frontend) | http://192.168.49.2 | Ingress |
| Grafana | http://192.168.49.2:30030 | NodePort, login `admin` / `admin` |
| Prometheus | http://localhost:9090 | wymaga `port-forward` |
| Backend (bezpośrednio) | http://localhost:8080 | wymaga `port-forward` |

### Port-forward dla usług wewnętrznych

W osobnym terminalu (zostaw otwarty):

```bash
# Prometheus
kubectl port-forward -n monitoring svc/prometheus 9090:9090

# Backend bezpośrednio (np. dla /actuator/health)
kubectl port-forward -n projektagile svc/backend-service 8080:8080

# Postgres dla klienta SQL
kubectl port-forward -n projektagile svc/postgres-service 5432:5432
```

### Grafana — pierwsze uruchomienie

1. Otwórz http://192.168.49.2:30030 (`admin` / `admin`).
2. **Dashboards → New → Import** → wpisz ID **4701** (JVM Micrometer) → Load.
3. Wybierz źródło danych `Prometheus` → Import.
4. Drugi przydatny dashboard: ID **6756** (Spring Boot HTTP).

## Debugowanie

```bash
kubectl get pods -n projektagile                       # status podów
kubectl describe pod <nazwa-poda> -n projektagile      # eventy na dole — pierwsza rzecz do sprawdzenia
kubectl logs <nazwa-poda> -n projektagile              # logi
kubectl logs <nazwa-poda> -n projektagile --previous   # logi z poprzedniej (rozbitej) instancji
kubectl exec -it <nazwa-poda> -n projektagile -- sh    # shell w kontenerze
```

## Wyłączenie

```bash
kubectl delete -f k8s/monitoring/
kubectl delete -f k8s/
minikube stop                  # zachowuje stan klastra
# albo
minikube delete                # kasuje wszystko
```

---

## Autoskalowanie (HPA)

Backend i frontend mają zdefiniowany `HorizontalPodAutoscaler` w [k8s/hpa.yaml](k8s/hpa.yaml). HPA skaluje liczbę podów w zależności od CPU/RAM.

```bash
make metrics-server   # jednorazowo: włącz addon w minikube
make hpa              # pokaż obecne metryki HPA
make watch-scale      # śledź na żywo (terminal się odświeża)
make stress           # uderz 50 równoległymi curlami żeby wywołać scale-up
```

Domyślne progi:
- **backend**: 1–5 podów, scale przy 70% CPU lub 80% RAM
- **frontend**: 1–3 pody, scale przy 70% CPU

---

## Helm chart

Pełny stack można też zainstalować jako Helm chart z [charts/projektagile/](charts/projektagile/):

```bash
make helm-lint        # walidacja
make helm-render      # podgląd wyrenderowanego YAML
make helm-install     # instalacja / upgrade
make helm-uninstall   # odinstalowanie
```

Parametry w [charts/projektagile/values.yaml](charts/projektagile/values.yaml) — można nadpisać przez `--set` lub własny `values-prod.yaml`:

```bash
helm upgrade --install projektagile charts/projektagile \
  --set backend.replicas=3 \
  --set credentials.dbPassword=tajne123
```

> Helm musi być zainstalowany. `sudo snap install helm --classic` lub instrukcja z [helm.sh/docs/intro/install](https://helm.sh/docs/intro/install/).

---

## ArgoCD (GitOps)

ArgoCD śledzi repo i synchronizuje stan klastra z gałęzią `main`. Pushujesz YAML/chart do gita → klaster sam się aktualizuje.

```bash
make argocd-install       # jednorazowo: instaluje ArgoCD do namespace argocd
make argocd-app           # rejestruje aplikację projektagile (z chartu z gita)
make argocd-password      # pokaż hasło początkowe admina
make argocd-ui            # port-forward na https://localhost:8080
```

W UI: zalogowane jako `admin` / hasło z `argocd-password`. Aplikacja `projektagile` pokaże się od razu i synchronizuje się automatycznie (`automated: prune+selfHeal`).

Konfiguracja aplikacji w [argocd/application.yaml](argocd/application.yaml). Wskazuje na chart `charts/projektagile` na branchu `main`.
