# ============================================
# 校友成果展览系统 - 启动基础设施 (PowerShell)
# 用法: .\start.ps1
# ============================================
$OutputEncoding = [Console]::OutputEncoding = [Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Alumni Exhibition - Start Infrastructure" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ---------- Start containers ----------
Write-Host "[1/3] Starting Docker containers (MySQL + Redis + MinIO)..." -ForegroundColor Yellow
docker-compose up -d mysql redis minio

# ---------- Wait for MySQL to initialize ----------
Write-Host "[2/3] Waiting for MySQL to initialize (~25s)..." -ForegroundColor Yellow
for ($i = 25; $i -gt 0; $i--) {
    Write-Host -NoNewline ("  Waiting {0,2}s...`r" -f $i)
    Start-Sleep -Seconds 1
}
Write-Host ""

# ---------- Check services ----------
Write-Host "[3/3] Checking services..." -ForegroundColor Yellow
Write-Host ""

$Pass = 0
$Fail = 0

# Check MySQL
$mysqlResult = docker exec exhibition-mysql mysql -uroot -proot -N -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='exhibition_db'" 2>$null
if ($mysqlResult -match '^\d+$' -and [int]$mysqlResult -gt 0) {
    Write-Host "  [OK] MySQL  (3306) - exhibition_db ready, $mysqlResult tables" -ForegroundColor Green
    $Pass++
} else {
    Write-Host "  [FAIL] MySQL  (3306) - connection failed or DB not ready" -ForegroundColor Red
    $Fail++
}

# Check Redis
$redisResult = docker exec exhibition-redis redis-cli -a redis123456 PING 2>&1
if ($redisResult -match "PONG") {
    Write-Host "  [OK] Redis  (6379) - PONG" -ForegroundColor Green
    $Pass++
} else {
    Write-Host "  [FAIL] Redis  (6379) - connection failed" -ForegroundColor Red
    $Fail++
}

# Check MinIO (container running = healthy)
Start-Sleep -Seconds 2
$minioRunning = docker ps --filter "name=exhibition-minio" --filter "status=running" --format "{{.Names}}" 2>$null
if ($minioRunning) {
    Write-Host "  [OK] MinIO  (9000) API / (9001) Console -> http://localhost:9001" -ForegroundColor Green
    $Pass++
} else {
    Write-Host "  [FAIL] MinIO  (9000) - container not running" -ForegroundColor Red
    $Fail++
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
if ($Fail -gt 0) {
    Write-Host "  Result: $Pass passed, $Fail FAILED" -ForegroundColor Red
} else {
    Write-Host "  Result: $Pass passed, $Fail failed" -ForegroundColor Green
}
Write-Host "========================================" -ForegroundColor Cyan

if ($Fail -gt 0) {
    Write-Host "  Debug: docker ps -a --filter 'name=exhibition'" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "  MinIO Console : http://localhost:9001" -ForegroundColor White
Write-Host "  Start backend  : mvn spring-boot:run" -ForegroundColor White
Write-Host "  Stop services  : .\stop.ps1" -ForegroundColor White
Write-Host ""
