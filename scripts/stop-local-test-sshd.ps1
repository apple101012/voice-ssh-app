$baseDir = Join-Path $env:LOCALAPPDATA "VoiceSshApp\local-sshd"
$pidPath = Join-Path $baseDir "sshd.pid"

$ErrorActionPreference = "Stop"

if (-not (Test-Path $pidPath)) {
    Write-Output "No local test sshd pid file found."
    exit 0
}

$pidValue = (Get-Content $pidPath).Trim()
if ($pidValue) {
    Stop-Process -Id $pidValue -Force -ErrorAction SilentlyContinue
}

Remove-Item $pidPath -Force -ErrorAction SilentlyContinue
Write-Output "Stopped local test sshd."
