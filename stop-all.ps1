# ============================================
# Alumni Exhibition System - Stop All Services
# Usage: .\stop-all.ps1
# ============================================
$ErrorActionPreference = "SilentlyContinue"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Alumni Exhibition - Stop All" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# ---------- 1. Stop frontends ----------
Write-Host "[1/3] Stopping frontend dev servers..." -ForegroundColor Yellow
Get-Job -Name "exhibition-admin" | Stop-Job -PassThru | Remove-Job -Force
Get-Job -Name "exhibition-student" | Stop-Job -PassThru | Remove-Job -Force

$vitePids = Get-NetTCPConnection -LocalPort 5173,5174 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
foreach ($p in $vitePids) { Stop-Process -Id $p -Force -ErrorAction SilentlyContinue }
Write-Host "       [OK] Frontends stopped" -ForegroundColor Green

# ---------- 2. Stop backend ----------
Write-Host "[2/3] Stopping backend..." -ForegroundColor Yellow
Get-Job -Name "exhibition-backend" | Stop-Job -PassThru | Remove-Job -Force

$javaPids = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
foreach ($p in $javaPids) {
    $proc = Get-Process -Id $p -ErrorAction SilentlyContinue
    if ($proc -and $proc.ProcessName -like "*java*") {
        Stop-Process -Id $p -Force -ErrorAction SilentlyContinue
    }
}
Write-Host "       [OK] Backend stopped" -ForegroundColor Green

# ---------- 3. Stop Docker ----------
Write-Host "[3/3] Stopping Docker containers..." -ForegroundColor Yellow
Push-Location docker
docker compose stop 2>&1 | Out-Null
Pop-Location
Write-Host "       [OK] Docker containers stopped" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All services stopped" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
