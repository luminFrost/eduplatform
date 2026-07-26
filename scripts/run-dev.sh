#!/usr/bin/env bash
# 개발 서버(bootRun)를 백그라운드로 실행하고 로그를 남긴다.
# 세션이 끊겨도 서버는 계속 돌아간다.
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_DIR"
mkdir -p build

PID_FILE="build/bootRun.pid"
LOG_FILE="build/bootRun.log"

if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  echo "이미 실행 중입니다 (pid $(cat "$PID_FILE")). 먼저 scripts/stop-dev.sh 로 중지하세요."
  exit 1
fi

nohup ./gradlew bootRun --console=plain > "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"
echo "bootRun 시작 (pid $(cat "$PID_FILE"))"
echo "로그: $LOG_FILE  |  실시간 확인: tail -f $LOG_FILE"
echo "앱: http://localhost:8080"
