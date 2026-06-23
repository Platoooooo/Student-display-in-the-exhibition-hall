# ============================================
# Alumni Exhibition System - Start All Services
# Usage: .\start-all.ps1
# ============================================
$ErrorActionPreference = "SilentlyContinue"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Alumni Exhibition - Start All" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ---------- 1. Infrastructure ----------
Write-Host "[1/5] Starting Docker (MySQL + Redis + MinIO)..." -ForegroundColor Yellow
Push-Location docker
docker compose up -d mysql redis minio 2>&1 | Out-Null
Pop-Location

Write-Host "       Waiting for MySQL (~25s)..." -ForegroundColor DarkGray
Start-Sleep -Seconds 25

$mysqlOk = docker exec exhibition-mysql mysqladmin ping -uroot -proot --silent 2>$null
$redisOk = docker exec exhibition-redis redis-cli -a redis123456 PING 2>&1 | Select-String "PONG"
if ($mysqlOk -and $redisOk) {
    Write-Host "       [OK] MySQL + Redis + MinIO ready" -ForegroundColor Green
} else {
    Write-Host "       [WARN] Some services may not be ready" -ForegroundColor Yellow
}

# ---------- 2. Backend ----------
Write-Host "[2/5] Starting Backend (:8080)..." -ForegroundColor Yellow
$env:ARCFACE_APP_ID = if ($env:ARCFACE_APP_ID) { $env:ARCFACE_APP_ID } else { "" }
$env:ARCFACE_SDK_KEY = if ($env:ARCFACE_SDK_KEY) { $env:ARCFACE_SDK_KEY } else { "" }

$backendJob = Start-Job -Name "exhibition-backend" -ScriptBlock {
    Set-Location $using:root\backend
    if (-not (Test-Path "target/libarcsoft_face_engine.dll")) {
        Copy-Item "$using:root\unity-display\Assets\Plugins\x86_64\libarcsoft_face*.dll" target\ -Force -ErrorAction SilentlyContinue
    }
    mvn -q spring-boot:run 2>&1 | Out-File -Append $using:root\logs\backend.log
}

Write-Host "       Waiting for backend..." -ForegroundColor DarkGray
$ready = $false
for ($i = 0; $i -lt 30; $i++) {
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080/api/display/playlist" -UseBasicParsing -TimeoutSec 3
        if ($r.StatusCode -eq 200) { $ready = $true; break }
    } catch { Start-Sleep -Seconds 2 }
}
if ($ready) { Write-Host "       [OK] Backend ready" -ForegroundColor Green }
else { Write-Host "       [WARN] Backend still starting (check logs\backend.log)" -ForegroundColor Yellow }

# ---------- 3. Admin Web ----------
Write-Host "[3/5] Starting Admin Web (:5173)..." -ForegroundColor Yellow
Start-Job -Name "exhibition-admin" -ScriptBlock {
    Set-Location $using:root\admin-web
    npm run dev 2>&1 | Out-File -Append $using:root\logs\admin-web.log
}
Start-Sleep -Seconds 5
try {
    $r = Invoke-WebRequest -Uri "http://localhost:5173" -UseBasicParsing -TimeoutSec 3
    if ($r.StatusCode -eq 200) { Write-Host "       [OK] Admin Web ready" -ForegroundColor Green }
} catch { Write-Host "       [WARN] Admin Web may still be starting" -ForegroundColor Yellow }

# ---------- 4. Student H5 ----------
Write-Host "[4/5] Starting Student H5 (:5174)..." -ForegroundColor Yellow
Start-Job -Name "exhibition-student" -ScriptBlock {
    Set-Location $using:root\student-h5
    npm run dev 2>&1 | Out-File -Append $using:root\logs\student-h5.log
}
Start-Sleep -Seconds 5
try {
    $r = Invoke-WebRequest -Uri "http://localhost:5174" -UseBasicParsing -TimeoutSec 3
    if ($r.StatusCode -eq 200) { Write-Host "       [OK] Student H5 ready" -ForegroundColor Green }
} catch { Write-Host "       [WARN] Student H5 may still be starting" -ForegroundColor Yellow }

# ---------- 5. Summary ----------
Write-Host "[5/5] Done!" -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All services started" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Backend     : http://localhost:8080" -ForegroundColor White
Write-Host "  Admin Panel : http://localhost:5173" -ForegroundColor White
Write-Host "  Student H5  : http://localhost:5174" -ForegroundColor White
Write-Host "  Display     : http://localhost:5173/screen" -ForegroundColor White
Write-Host "  MinIO       : http://localhost:9001 (minioadmin/minio123456)" -ForegroundColor DarkGray
Write-Host ""
Write-Host "  Stop  : .\stop-all.ps1" -ForegroundColor White
Write-Host "  Restart: .\restart-all.ps1" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
