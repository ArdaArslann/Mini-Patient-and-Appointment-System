COMPOSE = docker compose
SERVICE = mysql

.PHONY: up down restart logs ps status shell clean

up:
	$(COMPOSE) up -d

down:
	$(COMPOSE) down

restart: down up

logs:
	$(COMPOSE) logs -f $(SERVICE)

ps:
	$(COMPOSE) ps

status: ps

shell:
	$(COMPOSE) exec $(SERVICE) mysql -uroot -p123456 openemr

clean:
	$(COMPOSE) down -v
