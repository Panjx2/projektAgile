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

## Jak to odpalić — po ludzku

Masz pięć opcji. Wybierz **jedną**, nie wszystkie naraz, bo się posypią.

Po każdej z metod wchodzisz na **http://localhost:8081** w przeglądarce i logujesz się `admin` / `admin`.

### Opcja 1: "Po prostu odpal" — najprostsze, nie ruszasz kodu

```bash
docker compose up --build
```

Robi się magia, czekasz 5 minut, wchodzisz w przeglądarce. Gotowe.

Jak chcesz zatrzymać:
```bash
docker compose down            # wyłącz
docker compose down -v         # wyłącz i wywal bazę danych
```

**Plus:** jedna komenda i działa.
**Minus:** każda zmiana w kodzie wymaga drugiego `--build`, czeka kolejne 5 minut.

---

### Opcja 2: "Piszę kod, chcę szybko widzieć zmiany"

```bash
docker compose -f docker-compose.dev.yml up
```

Pierwsze uruchomienie jest wolne (~8 minut), bo ściąga sobie wszystkie biblioteki Mavena i Gradle'a. **Drugie i każde kolejne** już szybkie — i co najważniejsze, jak coś zmienisz w pliku `.java` w backendzie, to **samo się przeładuje** w kilka sekund. Frontend musisz zrestartować ręcznie (Ctrl-C i znowu komenda).

**Plus:** zmiana → restart liczy się w sekundach.
**Minus:** dużo RAM-u (~3 GB), bo dwie maszyny wirtualne Javy + postgres pakują się obok siebie.

---

### Opcja 3: "Chcę debugger i wszystko lokalnie"

Potrzebujesz mieć zainstalowaną **Javę 21**. Sprawdzasz:
```bash
java -version
```
Jeśli pokazuje 21 — git. Jeśli nie — `sudo apt install openjdk-21-jdk` na Mincie/Ubuntu.

**Krok 1:** Odpal samego postgresa w Dockerze (terminal 1, zostaw otwarty):
```bash
docker run -d --name pg-dev \
  -p 5432:5432 \
  -e POSTGRES_DB=app \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  postgres:16-alpine
```

**Krok 2:** Backend w drugim terminalu:
```bash
cd app
./mvnw spring-boot:run
```
Czekasz ~70 sekund aż wypisze `Started AppApplication`.

**Krok 3:** Frontend w trzecim terminalu:
```bash
cd frontend
./gradlew bootRun
```
Czekasz aż wypisze `Started FrontendApplication`.

**Krok 4:** Wchodzisz na http://localhost:8081.

Jak skończysz:
- Ctrl-C w terminalach 2 i 3
- `docker stop pg-dev && docker rm pg-dev` jak chcesz całkiem posprzątać

**Plus:** najmniej zasobów, najszybsza pętla, możesz wepchać breakpointy w IntelliJ.
**Minus:** trzy terminale do pilnowania, musisz mieć lokalnie Javę.

---

### Opcja 4: "Klikam w IntelliJ i się odpala"

To samo co Opcja 3, tylko zamiast `./mvnw spring-boot:run` i `./gradlew bootRun`:

1. Otwórz IntelliJ → File → Open → wybierz folder `projektAgile`
2. IntelliJ wykryje oba moduły (`app/` i `frontend/`)
3. Znajdź klasę z `@SpringBootApplication`:
   - W backendzie: `com.example.app.AppApplication`
   - W frontendzie: główna klasa w `com.project`
4. Kliknij prawym → **Run** (albo **Debug** jak chcesz breakpointy)
5. Zrób to dla obu osobno

Postgres dalej musi chodzić (`docker run` z Opcji 3, krok 1).

**Plus:** debugger, hot reload, klikanie myszką.
**Minus:** musisz raz spędzić 15 minut konfigurując IntelliJ.

---

### Opcja 5: "Chcę pobawić się w Kubernetes" — pełny zestaw

Przejdź do sekcji [Uruchomienie w Kubernetes (minikube)](#uruchomienie-w-kubernetes-minikube) niżej. To **dużo** dodatkowej roboty (~20 min setup), sens ma tylko jak chcesz nauczyć się k8s, nie jak chcesz tylko odpalić aplikację.

---

### Która opcja dla kogo?

| Sytuacja | Opcja |
|---|---|
| "Chcę zobaczyć jak to wygląda" | **1** |
| "Będę dziś pisać kod" | **2** lub **3** |
| "Chcę debugować, mam IDE" | **3** lub **4** |
| "Pracuję nad backendem, frontend mi obojętny" | **3** (sam backend, frontend opcjonalnie) |
| "Jest mi nudno, chcę zabaw" | **5** |

---

### Najczęstsze "ale czemu to nie działa?"

**"Port already in use" / "Address already in use":**
Coś już chodzi na 8080, 8081 albo 5432. Znajdź i zabij:
```bash
sudo lsof -i :8080         # zobacz co siedzi
sudo kill <PID>            # zabij to
```
Najczęściej to poprzednie uruchomienie compose'a — `docker compose down`.

**"Bad credentials" przy logowaniu:**
Wywal bazę i odpal od nowa — pewnie zostały stare hasła z poprzedniego startu:
```bash
docker compose down -v     # to "-v" wywala wolumen z bazą
docker compose up --build
```

**Backend startuje i od razu zdycha z "null value in column user_id":**
Stary schemat w bazie. Tak samo:
```bash
docker compose down -v
docker compose up --build
```

**"Connection refused" frontend → backend:**
Backend jeszcze nie wstał. Daj mu minutę i odśwież stronę. Jak po 2 minutach dalej, sprawdź:
```bash
curl http://localhost:8080/actuator/health/liveness
```
Powinno zwrócić `{"status":"UP"}`. Jak nie zwraca — patrz w logi backendu.

**Wszystko gotowe ale strona nie ładuje się:**
Sprawdź czy łazisz po dobrym adresie. **Frontend to 8081, NIE 8080.** 8080 to tylko API.

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
