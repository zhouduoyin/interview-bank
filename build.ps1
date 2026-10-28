# 重新构建前端与后端（产物：server\target\interview-bank.jar）
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$static = Join-Path $root 'server\src\main\resources\static'

Write-Host '==> 构建前端' -ForegroundColor Cyan
Push-Location (Join-Path $root 'web')
try {
    npm run build
} finally {
    Pop-Location
}

Write-Host '==> 复制前端产物到后端静态目录' -ForegroundColor Cyan
if (Test-Path $static) { Remove-Item -Recurse -Force $static }
New-Item -ItemType Directory -Force -Path $static | Out-Null
Copy-Item -Path (Join-Path $root 'web\dist\*') -Destination $static -Recurse -Force

Write-Host '==> 打包后端' -ForegroundColor Cyan
Push-Location (Join-Path $root 'server')
try {
    mvn -DskipTests package
} finally {
    Pop-Location
}

Write-Host ''
Write-Host "构建完成：$root\server\target\interview-bank.jar" -ForegroundColor Green
Write-Host '运行 start.bat 即可启动。' -ForegroundColor DarkGray
