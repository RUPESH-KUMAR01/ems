#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

PATTERN='event-management-system-1.0-SNAPSHOT.jar server'

if [[ "${1:-}" == "--restart" ]]; then
  bash "$ROOT/scripts/stop-backend.sh" || true
  sleep 1
  echo "Building backend (pick up latest code)…"
  mvn -q -DskipTests package
fi

if pgrep -f "$PATTERN" >/dev/null; then
  if curl -sf --max-time 2 http://localhost:8081/healthcheck >/dev/null 2>&1; then
    echo "EMS backend is already running on http://localhost:8080 (admin health: :8081)."
    echo "To pick up config or code changes, restart with:"
    echo "  ./scripts/dev-backend.sh --restart"
    exit 0
  fi
  echo "Port 8080 may be in use by a stuck EMS process. Run:"
  echo "  ./scripts/stop-backend.sh"
  echo "then start again with ./scripts/dev-backend.sh"
  exit 1
fi

if [[ -f "$ROOT/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT/.env"
  set +a
fi

JAR="$ROOT/target/event-management-system-1.0-SNAPSHOT.jar"
if [[ ! -f "$JAR" ]]; then
  echo "Building backend…"
  mvn -q -DskipTests package
fi

echo "Starting EMS backend on :8080 …"
exec java -jar "$JAR" server config.yml
