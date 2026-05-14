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
