# ============================================================
#  Neo4flix - START ALL
#  Lance : Neo4j + 5 microservices + Frontend Angular
# ============================================================

$MVN  = "E:\DevTools\maven-mvnd-1.0.3-windows-amd64\maven-mvnd-1.0.3-windows-amd64\mvn\bin"
$ROOT = "E:\pZone01\neo4flix"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   NEO4FLIX - DEMARRAGE COMPLET"            -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# 0. Docker Desktop
Write-Host "`n[0/7] Verification Docker Desktop..." -ForegroundColor Yellow
$dockerRunning = Get-Process -Name "Docker Desktop" -ErrorAction SilentlyContinue
if (-not $dockerRunning) {
    Write-Host "      Docker Desktop non lance, demarrage..." -ForegroundColor Yellow
    Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    Write-Host "      Attente demarrage Docker Desktop (30s)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
} else {
    Write-Host "      [OK] Docker Desktop deja en cours d'execution" -ForegroundColor Green
}
# Attendre que le daemon Docker soit pret
$dockerReady = $false
for ($i = 0; $i -lt 12; $i++) {
    try {
        docker info 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) { $dockerReady = $true; break }
    } catch {}
    Write-Host "      Attente daemon Docker..." -ForegroundColor Gray
    Start-Sleep -Seconds 5
}
if (-not $dockerReady) {
    Write-Host "      [ERREUR] Docker daemon non disponible !" -ForegroundColor Red
    exit 1
}
Write-Host "      [OK] Docker pret" -ForegroundColor Green

# 1. Neo4j
Write-Host "`n[1/7] Demarrage Neo4j..." -ForegroundColor Yellow
docker start neo4flix-neo4j | Out-Null
Start-Sleep -Seconds 5
$neo4jStatus = docker ps --filter "name=neo4flix-neo4j" --format "{{.Status}}"
if ($neo4jStatus -like "Up*") {
    Write-Host "      [OK] Neo4j - http://localhost:7474" -ForegroundColor Green
} else {
    Write-Host "      [ERREUR] Neo4j n'a pas demarre !" -ForegroundColor Red
    exit 1
}

# 2. movie-service
Write-Host "[2/7] Demarrage movie-service (8081)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $ROOT\movie-service; `$env:PATH += ';$MVN'; mvn spring-boot:run"
Start-Sleep -Seconds 2

# 3. user-service
Write-Host "[3/7] Demarrage user-service (8082)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $ROOT\user-service; `$env:PATH += ';$MVN'; mvn spring-boot:run"
Start-Sleep -Seconds 2

# 4. rating-service
Write-Host "[4/7] Demarrage rating-service (8083)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $ROOT\rating-service; `$env:PATH += ';$MVN'; mvn spring-boot:run"
Start-Sleep -Seconds 2

# 5. recommendation-service
Write-Host "[5/7] Demarrage recommendation-service (8084)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $ROOT\recommendation-service; `$env:PATH += ';$MVN'; mvn spring-boot:run"
Start-Sleep -Seconds 2

# 6. api-gateway
Write-Host "[6/7] Demarrage api-gateway (8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $ROOT\api-gateway; `$env:PATH += ';$MVN'; mvn spring-boot:run"
Start-Sleep -Seconds 2

# 7. Frontend Angular
Write-Host "[7/7] Demarrage Frontend Angular (4200)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $ROOT\frontend; ng serve"

# Attente
Write-Host "`nAttente du demarrage des services (60s)..." -ForegroundColor Cyan
Start-Sleep -Seconds 60

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   VERIFICATION DES PORTS"                    -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$ports = @(8080, 8081, 8082, 8083, 8084, 4200)
foreach ($port in $ports) {
    $result = netstat -ano | Select-String ":$port " | Select-String "LISTENING"
    if ($result) {
        Write-Host "   [OK] Port $port LISTENING" -ForegroundColor Green
    } else {
        Write-Host "   [!!] Port $port PAS ENCORE PRET" -ForegroundColor Red
    }
}

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   Frontend : http://localhost:4200"           -ForegroundColor White
Write-Host "   Gateway  : http://localhost:8080/api"       -ForegroundColor White
Write-Host "   Neo4j UI : http://localhost:7474"           -ForegroundColor White
Write-Host "============================================"  -ForegroundColor Cyan
