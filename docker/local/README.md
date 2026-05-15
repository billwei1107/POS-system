# Docker Local

本目錄是 POS 本地 Docker 環境。環境變數來源為 `../../env/local/.env`，範本為 `../../env/.env.example`。

## 啟動

```bash
cd /Users/wei/Desktop/code/POS
cp env/.env.example env/local/.env
docker compose -f docker/local/docker-compose.yml up -d --build backend frontend
```

服務與埠：

| 服務 | 容器 | 對外埠 |
|------|------|--------|
| PostgreSQL | `pos-postgres` | `5432` |
| Redis | `pos-redis` | `6379` |
| Backend | `pos-backend` | `38180` |
| Frontend | `pos-frontend` | `38182` |

## 驗證

```bash
curl http://localhost:38180/actuator/health
```

前端開啟：

```text
http://localhost:38182
```

POS demo PIN：

```text
1234
```

## DB 查詢

本地 PostgreSQL 預設帳密請看 `env/local/.env`。目前預設：

```text
DB_NAME=pos_db
DB_USER=pos_user
```

範例：

```bash
docker exec -i pos-postgres psql -U pos_user -d pos_db -c "select status from pos_orders order by created_at desc limit 1;"
```

## 常用維護指令

只重建前端：

```bash
docker compose -f docker/local/docker-compose.yml build frontend
docker compose -f docker/local/docker-compose.yml up -d --no-deps frontend
```

重建後端與前端：

```bash
docker compose -f docker/local/docker-compose.yml up -d --build backend frontend
```

查看 log：

```bash
docker compose -f docker/local/docker-compose.yml logs -f backend
docker compose -f docker/local/docker-compose.yml logs -f frontend
```
