#!/usr/bin/env bash
# run-dev.sh 로 띄운 백그라운드 개발 서버를 중지한다.
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_DIR"

PID_FILE="build/bootRun.pid"

if [ ! -f "$PID_FILE" ]; then
  echo "실행 중인 서버가 없습니다 ($PID_FILE 없음)."
  exit 0
fi

PID="$(cat "$PID_FILE")"
if kill -0 "$PID" 2>/dev/null; then
  # bootRun 은 자식 Gradle/JVM 프로세스를 띄우므로 프로세스 그룹까지 정리
  pkill -P "$PID" 2>/dev/null || true
  kill "$PID" 2>/dev/null || true
  echo "서버 중지 (pid $PID)"
else
  echo "프로세스가 이미 종료됨 (pid $PID)"
fi
rm -f "$PID_FILE"
