# 面试记忆助手 · 一键启动
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$jar = Join-Path $root 'server\target\interview-bank.jar'
$url = 'http://127.0.0.1:8090'

if (-not (Test-Path $jar)) {
    Write-Host ''
    Write-Host '  未找到构建产物：server\target\interview-bank.jar' -ForegroundColor Red
    Write-Host '  请先构建后端：' -ForegroundColor Yellow
    Write-Host '    cd server' -ForegroundColor Gray
    Write-Host '    mvn -DskipTests package' -ForegroundColor Gray
    Write-Host ''
    Read-Host '按回车退出'
    exit 1
}

Set-Location $root
Write-Host ''
Write-Host '  面试记忆助手正在启动...' -ForegroundColor Cyan
Write-Host '  首次启动会创建本地数据库并写入示例题，大约需要 20 秒。' -ForegroundColor DarkGray
Write-Host "  启动完成后会自动打开 $url" -ForegroundColor DarkGray
Write-Host ''

# 如果已经有一个实例在跑，直接打开浏览器即可
try {
    Invoke-WebRequest -Uri "$url/api/stats/dashboard" -UseBasicParsing -TimeoutSec 2 | Out-Null
    Write-Host '  检测到面试记忆助手已经在运行，直接打开页面。' -ForegroundColor Green
    Start-Process $url
    exit 0
} catch {
    # 没有运行中的实例，继续启动
}

# 等后端真正就绪后再打开浏览器，避免打开一个报错页面
$helper = Start-Job -ScriptBlock {
    param($u)
    for ($i = 0; $i -lt 150; $i++) {
        try {
            Invoke-WebRequest -Uri "$u/api/stats/dashboard" -UseBasicParsing -TimeoutSec 1 | Out-Null
            Start-Process $u
            return
        } catch {
            Start-Sleep -Milliseconds 800
        }
    }
} -ArgumentList $url

try {
    java -jar $jar
} finally {
    Stop-Job $helper -ErrorAction SilentlyContinue
    Remove-Job $helper -Force -ErrorAction SilentlyContinue
}
