#!/usr/bin/env bash
set -euo pipefail
PATTERN='event-management-system-1.0-SNAPSHOT.jar server'
if pgrep -f "$PATTERN" >/dev/null; then
  pkill -f "$PATTERN" || true
  sleep 1
  if pgrep -f "$PATTERN" >/dev/null; then
    pkill -9 -f "$PATTERN" || true
    sleep 1
  fi
  if pgrep -f "$PATTERN" >/dev/null; then
    echo "Backend still running; try: pkill -9 -f '$PATTERN'"
    exit 1
  fi
  echo "Stopped EMS backend."
else
  echo "No EMS backend process found."
fi
