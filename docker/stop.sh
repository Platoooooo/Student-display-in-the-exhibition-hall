#!/bin/bash
# ============================================
# 校友成果展览系统 - 停止基础设施
# 用法: bash stop.sh
# ============================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "========================================"
echo "  校友成果展览系统 - 停止基础设施"
echo "========================================"
echo ""

# 检查是否有运行中的容器
RUNNING=$(docker ps --filter "name=exhibition" --format "{{.Names}}" 2>/dev/null)

if [ -z "$RUNNING" ]; then
  echo "  ℹ️  没有运行中的 exhibition 容器"
  echo ""
  echo "========================================"
  exit 0
fi

echo "  停止容器:"
echo "$RUNNING" | while read name; do
  echo "    • $name"
done
echo ""

docker-compose stop mysql redis minio

echo "  当前状态:"
docker ps -a --filter "name=exhibition" --format "  • {{.Names}} → {{.Status}}"

echo ""
echo "========================================"
echo "  ✅ 停止完成。重新启动: bash start.sh"
echo "========================================"
