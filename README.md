# projektAgile

nawet nie będę się bawić w accesstokeny

Arkusz:
https://utpedupl-my.sharepoint.com/:x:/g/personal/nikgeb000_o365_student_pbs_edu_pl/IQBk9ekMGGuKSZz4JBtHvlD2Ad9iiqXZNQLZGc55bGROTxs?e=c8fBlN

## Wymagania

Do uruchomienia projektu potrzebujesz:
- Docker
- Docker Compose

## Uruchomienie projektu

Z głównego katalogu projektu uruchom:

```bash
docker compose up --build

## Development mode

Tryb developerski uruchamia aplikację bez przebudowywania obrazów Dockera po każdej zmianie w kodzie.

Start:
```bash
docker compose -f docker-compose.dev.yml up