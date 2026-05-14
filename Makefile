NS         := projektagile
MON_NS     := monitoring
BACKEND    := projektagile-backend:1.0
FRONTEND   := projektagile-frontend:1.0
MINIKUBE_IP := $(shell minikube ip 2>/dev/null)

.DEFAULT_GOAL := help

help: ## Show this help
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

## --- compose (local dev) ---

compose-up: ## Run full stack via docker compose
	docker compose up --build

compose-dev: ## Run dev compose (no image rebuild on change)
	docker compose -f docker-compose.dev.yml up

compose-down: ## Stop docker compose stack
	docker compose down

## --- kubernetes lifecycle ---

mk-start: ## Start minikube
	minikube start

mk-stop: ## Stop minikube (preserves state)
	minikube stop

build: ## Build both images via docker compose
	docker compose build

load: ## Remove stale images from minikube and load fresh ones
	-minikube image rm docker.io/library/$(BACKEND) docker.io/library/$(FRONTEND)
	minikube image load $(BACKEND) $(FRONTEND)

up: ## Apply all manifests (app + monitoring)
	kubectl apply -f k8s/namespace.yaml
	kubectl apply -f k8s/
	kubectl apply -f k8s/monitoring/

down: ## Delete all manifests
	-kubectl delete -f k8s/monitoring/
	-kubectl delete -f k8s/

reload: build load rollout ## Full rebuild → reload → rollout

rollout: ## Restart backend + frontend deployments
	kubectl rollout restart deployment/backend deployment/frontend -n $(NS)
	kubectl rollout status deployment/backend -n $(NS) --timeout=300s
	kubectl rollout status deployment/frontend -n $(NS) --timeout=300s

## --- inspection ---

pods: ## Show pods in both namespaces
	@kubectl get pods -n $(NS)
	@echo
	@kubectl get pods -n $(MON_NS)

events: ## Tail recent events in app namespace
	kubectl get events -n $(NS) --sort-by='.lastTimestamp' | tail -20

logs-backend: ## Follow backend logs
	kubectl logs -n $(NS) -l app=backend -f --tail=100

logs-frontend: ## Follow frontend logs
	kubectl logs -n $(NS) -l app=frontend -f --tail=100

logs-prev: ## Logs from previous (crashed) backend container
	kubectl logs -n $(NS) -l app=backend --previous --tail=100

describe-backend: ## Describe backend pod
	kubectl describe pod -n $(NS) -l app=backend

## --- browser access ---

urls: ## Print URLs for app and observability
	@echo "App:        http://$(MINIKUBE_IP)"
	@echo "Grafana:    http://$(MINIKUBE_IP):30030  (admin/admin)"
	@echo "Prometheus: http://localhost:9090       (needs make forward-prometheus)"

grafana: ## Open Grafana in browser
	minikube service grafana -n $(MON_NS)

forward-prometheus: ## Port-forward Prometheus to localhost:9090
	kubectl port-forward -n $(MON_NS) svc/prometheus 9090:9090

forward-backend: ## Port-forward backend to localhost:8080
	kubectl port-forward -n $(NS) svc/backend-service 8080:8080

forward-postgres: ## Port-forward postgres to localhost:5432
	kubectl port-forward -n $(NS) svc/postgres-service 5432:5432

## --- database ---

psql: ## Open psql in the postgres pod
	kubectl exec -it -n $(NS) postgres-0 -- psql -U postgres -d app

db-reset: ## Drop and recreate public schema (wipes all data), then restart backend
	kubectl exec -n $(NS) postgres-0 -- psql -U postgres -d app -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO postgres;"
	kubectl rollout restart deployment/backend -n $(NS)
	kubectl rollout status deployment/backend -n $(NS) --timeout=300s

## --- load testing ---

load-test: ## Hammer the frontend with curl (Ctrl-C to stop)
	@echo "Hitting http://$(MINIKUBE_IP) — Ctrl-C to stop"
	@while true; do curl -s -o /dev/null http://$(MINIKUBE_IP)/; done

.PHONY: help compose-up compose-dev compose-down mk-start mk-stop build load up down reload rollout pods events logs-backend logs-frontend logs-prev describe-backend urls grafana forward-prometheus forward-backend forward-postgres psql db-reset load-test
