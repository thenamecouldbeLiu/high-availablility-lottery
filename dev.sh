#!/usr/bin/env bash

set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${ROOT_DIR}/init-service/docker-compose.yml"
USER_PID=""
LOTTERY_PID=""

if docker compose version >/dev/null 2>&1; then
  COMPOSE=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  echo "錯誤：找不到 docker compose 或 docker-compose。" >&2
  exit 1
fi

cleanup() {
  trap - INT TERM EXIT

  echo
  echo "正在停止 user service 與 lottery service..."
  [[ -n "${USER_PID}" ]] && kill "${USER_PID}" 2>/dev/null || true
  [[ -n "${LOTTERY_PID}" ]] && kill "${LOTTERY_PID}" 2>/dev/null || true
  wait 2>/dev/null || true
}

trap cleanup INT TERM EXIT

echo "正在啟動 PostgreSQL、Redis、Keycloak 與 RabbitMQ..."
"${COMPOSE[@]}" -f "${COMPOSE_FILE}" up -d

echo "正在啟動 user service（port 8082）..."
(cd "${ROOT_DIR}" && exec ./gradlew :user:bootRun) &
USER_PID=$!

echo "正在啟動 lottery service（port 8080）..."
(cd "${ROOT_DIR}" && exec ./gradlew :lottery:bootRun) &
LOTTERY_PID=$!

echo "所有服務已啟動；按 Ctrl+C 停止 user service 與 lottery service。"
echo "若要停止 Docker 服務，請執行：docker compose -f init-service/docker-compose.yml down"

set +e
wait -n "${USER_PID}" "${LOTTERY_PID}"
EXIT_CODE=$?
set -e

if kill -0 "${USER_PID}" 2>/dev/null && kill -0 "${LOTTERY_PID}" 2>/dev/null; then
  exit "${EXIT_CODE}"
fi

echo "其中一個 application service 已結束，正在停止另一個服務。" >&2
exit "${EXIT_CODE}"
