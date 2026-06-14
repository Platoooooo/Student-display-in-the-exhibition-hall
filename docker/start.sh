#!/bin/bash
# ============================================
# 校友成果展览系统 - 启动基础设施
# 用法: bash start.sh
# ============================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "========================================"
echo "  校友成果展览系统 - 启动基础设施"
echo "========================================"
echo ""

# ---------- 启动容器 ----------
echo "[1/3] 启动 Docker 容器 (MySQL + Redis + MinIO)..."
docker-compose up -d mysql redis minio

# ---------- 等待 MySQL 初始化 ----------
echo "[2/3] 等待 MySQL 初始化 (约25秒)..."
i=25
while [ $i -gt 0 ]; do
  printf "  剩余 %2ds\r" $i
  sleep 1
  i=$((i - 1))
done
echo ""

# ---------- 检查服务 ----------
echo "[3/3] 检查服务状态..."
echo ""

PASS=0
FAIL=0

# 检查 MySQL
MYSQL_OUTPUT=$(docker exec exhibition-mysql mysql -uroot -proot -N -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='exhibition_db'" 2>/dev/null)
if [ -n "$MYSQL_OUTPUT" ] && [ "$MYSQL_OUTPUT" -gt 0 ] 2>/dev/null; then
  echo "  ✅ MySQL  (3306) - 数据库 exhibition_db 就绪, ${MYSQL_OUTPUT} 张表"
  PASS=$((PASS + 1))
else
  echo "  ❌ MySQL  (3306) - 连接失败或数据库未就绪"
  FAIL=$((FAIL + 1))
fi

# 检查 Redis
REDIS_OUTPUT=$(docker exec exhibition-redis redis-cli -a redis123456 PING 2>&1)
if echo "$REDIS_OUTPUT" | grep -q "PONG"; then
  echo "  ✅ Redis  (6379) - PONG"
  PASS=$((PASS + 1))
else
  echo "  ❌ Redis  (6379) - 连接失败"
  FAIL=$((FAIL + 1))
fi

# 检查 MinIO API
sleep 2
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9000 2>/dev/null)
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "403" ]; then
  echo "  ✅ MinIO  (9000) API / (9001) 控制台 ← http://localhost:9001"
  PASS=$((PASS + 1))
else
  echo "  ❌ MinIO  (9000) - 未响应"
  FAIL=$((FAIL + 1))
fi

echo ""
echo "========================================"
echo "  结果: ${PASS} 通过, ${FAIL} 失败"
echo "========================================"

if [ "$FAIL" -gt 0 ]; then
  echo "  请检查: docker ps -a --filter 'name=exhibition'"
  exit 1
fi

echo ""
echo "  👉 MinIO 控制台: http://localhost:9001"
echo "  👉 后端可本地启动: mvn spring-boot:run"
echo "  👉 停止服务: bash stop.sh"
echo ""
