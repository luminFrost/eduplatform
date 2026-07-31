#!/usr/bin/env bash
# 클래스 단위 의존관계 그래프를 생성한다 (jdeps + Graphviz).
# 우리 코드(com.edu.eduplatform.*) 간의 참조만 남기고 JDK/Spring 등 외부 의존성은 제외한다.
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_DIR"

if ! command -v dot >/dev/null 2>&1; then
  echo "graphviz(dot)가 필요합니다: brew install graphviz" >&2
  exit 1
fi

OUT_DIR="build/code-graph"
DOT_DIR="$OUT_DIR/dot"
PACKAGE_PREFIX="com.edu.eduplatform"

echo "==> 컴파일"
./gradlew compileJava -q

rm -rf "$DOT_DIR"
mkdir -p "$DOT_DIR"

echo "==> jdeps로 클래스 단위 의존성 추출"
jdeps -verbose:class -filter:none -dotoutput "$DOT_DIR" build/classes/java/main >/dev/null

RAW_DOT="$DOT_DIR/main.dot"
FILTERED_DOT="$OUT_DIR/eduplatform-classes.dot"

echo "==> ${PACKAGE_PREFIX} 내부 의존성만 필터링"
{
  echo 'digraph "eduplatform-classes" {'
  echo '  rankdir=LR;'
  echo '  node [shape=box, fontsize=10, fontname="Helvetica", style=filled, fillcolor="#eef3fb"];'
  grep -E "\"${PACKAGE_PREFIX}[^\"]*\" +-> +\"${PACKAGE_PREFIX}[^\"]*\\(main\\)\";" "$RAW_DOT" \
    | sed -E 's/ \(main\)//g' \
    | sort -u
  echo '}'
} > "$FILTERED_DOT"

echo "==> 렌더링 (svg, png)"
dot -Tsvg "$FILTERED_DOT" -o "$OUT_DIR/eduplatform-classes.svg"
dot -Tpng "$FILTERED_DOT" -o "$OUT_DIR/eduplatform-classes.png"

EDGE_COUNT=$(grep -c '\->' "$FILTERED_DOT" || true)
echo ""
echo "완료: 클래스 간 의존 엣지 ${EDGE_COUNT}개"
echo "  dot: $FILTERED_DOT"
echo "  svg: $OUT_DIR/eduplatform-classes.svg"
echo "  png: $OUT_DIR/eduplatform-classes.png"