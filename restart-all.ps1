# ============================================
# Alumni Exhibition System - Restart All Services
# Usage: .\restart-all.ps1
# ============================================
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Alumni Exhibition - Restart All" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

& "$root\stop-all.ps1"
Write-Host ""
Start-Sleep -Seconds 2
& "$root\start-all.ps1"
