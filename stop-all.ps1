# ============================================================
#  Neo4flix - STOP ALL
#  Arrete : tous les Java + Frontend Angular + Neo4j
# ============================================================

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   NEO4FLIX - ARRET COMPLET" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# --- 1. Tuer tous les processus Java (microservices) ---
Write-Host "`n[1/3] Arret des microservices Java..." -ForegroundColor Yellow
$javaProcs = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcs) {
    $count = $javaProcs.Count
    $javaProcs | Stop-Process -Force
    Write-Host "      $count processus Java arretes" -ForegroundColor Green
} else {
    Write-Host "      Aucun processus Java en cours" -ForegroundColor Gray
}

# --- 2. Tuer Node.js (ng serve - Frontend Angular) ---
Write-Host "[2/3] Arret du Frontend Angular (Node.js)..." -ForegroundColor Yellow
$nodeProcs = Get-Process -Name "node" -ErrorAction SilentlyContinue
if ($nodeProcs) {
    $count = $nodeProcs.Count
    $nodeProcs | Stop-Process -Force
    Write-Host "      $count processus Node.js arretes" -ForegroundColor Green
} else {
    Write-Host "      Aucun processus Node.js en cours" -ForegroundColor Gray
}

# --- 3. Arreter Neo4j proprement ---
Write-Host "[3/3] Arret de Neo4j..." -ForegroundColor Yellow
$neo4jRunning = docker ps --filter "name=neo4flix-neo4j" --format "{{.Status}}" 2>$null
if ($neo4jRunning -like "Up*") {
    docker stop neo4flix-neo4j | Out-Null
    Write-Host "      Neo4j arrete proprement" -ForegroundColor Green
} else {
    Write-Host "      Neo4j etait deja arrete" -ForegroundColor Gray
}

# --- Verification finale ---
Start-Sleep -Seconds 2
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   VERIFICATION DES PORTS" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$ports = @(8080, 8081, 8082, 8083, 8084, 4200)
$allClear = $true
foreach ($port in $ports) {
    $listening = netstat -ano 2>$null | Select-String ":$port " | Select-String "LISTENING"
    if ($listening) {
        Write-Host "   ! Port $port encore utilise" -ForegroundColor Red
        $allClear = $false
    } else {
        Write-Host "   ✓ Port $port libre" -ForegroundColor Green
    }
}

Write-Host ""
if ($allClear) {
    Write-Host "   TOUT EST ARRETE PROPREMENT !" -ForegroundColor Green
} else {
    Write-Host "   Certains ports sont encore utilises (peut-etre Nexus ou autre)" -ForegroundColor Yellow
}
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
