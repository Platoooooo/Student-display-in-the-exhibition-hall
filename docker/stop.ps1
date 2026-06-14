# ============================================
# 校友成果展览系统 - 停止基础设施 (PowerShell)
# 用法: .\stop.ps1
# ============================================
$OutputEncoding = [Console]::OutputEncoding = [Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Alumni Exhibition - Stop Infrastructure" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check running containers
$running = docker ps --filter "name=exhibition" --format "{{.Names}}" 2>$null

if (-not $running) {
    Write-Host "  (i) No running exhibition containers" -ForegroundColor Yellow
    Write-Host ""
    exit 0
}

Write-Host "  Stopping containers:" -ForegroundColor Yellow
$running | ForEach-Object { Write-Host "    - $_" }
Write-Host ""

docker-compose stop mysql redis minio

Write-Host "  Current status:" -ForegroundColor Yellow
docker ps -a --filter "name=exhibition" --format "  - {{.Names}} -> {{.Status}}"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Done. Restart: .\start.ps1" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
