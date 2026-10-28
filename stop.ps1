# 停止面试题库（占用 8090 端口的 Java 进程）
$conn = Get-NetTCPConnection -LocalPort 8090 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $conn) {
    Write-Host '8090 端口没有正在运行的服务。' -ForegroundColor Yellow
    exit 0
}
$proc = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
Stop-Process -Id $conn.OwningProcess -Force
Write-Host "已停止面试题库服务（PID $($conn.OwningProcess)，$($proc.ProcessName)）。" -ForegroundColor Green
