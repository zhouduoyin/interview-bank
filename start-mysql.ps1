# Start the Spring Boot API with the MySQL profile.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$jar = Join-Path $root 'server\target\interview-bank.jar'

if (-not (Test-Path $jar)) {
    Write-Host ''
    Write-Host '  Missing server\target\interview-bank.jar. Build it first:' -ForegroundColor Red
    Write-Host '    cd server' -ForegroundColor Gray
    Write-Host '    mvn -DskipTests package' -ForegroundColor Gray
    Write-Host ''
    exit 1
}

$env:SPRING_PROFILES_ACTIVE = 'mysql'
if (-not $env:DB_URL) {
    $env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/interview_bank?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
}
if (-not $env:DB_USERNAME) { $env:DB_USERNAME = 'interview' }
if (-not $env:DB_PASSWORD) { $env:DB_PASSWORD = 'interview_pass' }
if (-not $env:SERVER_PORT) { $env:SERVER_PORT = '8090' }

Set-Location $root
Write-Host ''
Write-Host '  Interview Bank API (MySQL profile)' -ForegroundColor Cyan
Write-Host "  DB: $($env:DB_URL)" -ForegroundColor DarkGray
Write-Host "  API: http://127.0.0.1:$($env:SERVER_PORT)/api" -ForegroundColor DarkGray
Write-Host ''
java -jar $jar
