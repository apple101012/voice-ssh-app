param(
    [int]$Port = 2222,
    [switch]$ForceRegenerate
)

$ErrorActionPreference = "Stop"

$baseDir = Join-Path $env:LOCALAPPDATA "VoiceSshApp\local-sshd"
$hostKeyPath = Join-Path $baseDir "host_ed25519"
$clientKeyPath = Join-Path $baseDir "client_rsa"
$authorizedKeysPath = Join-Path $baseDir "authorized_keys"
$configPath = Join-Path $baseDir "sshd_config"
$logPath = Join-Path $baseDir "sshd.log"
$pidPath = Join-Path $baseDir "sshd.pid"

$sshKeygen = (Get-Command ssh-keygen.exe).Source
$sshd = (Get-Command sshd.exe).Source

New-Item -ItemType Directory -Path $baseDir -Force | Out-Null

if ($ForceRegenerate) {
    Remove-Item $hostKeyPath, "$hostKeyPath.pub", $clientKeyPath, "$clientKeyPath.pub", $authorizedKeysPath, $configPath, $logPath, $pidPath -Force -ErrorAction SilentlyContinue
}

if (-not (Test-Path $hostKeyPath)) {
    & $sshKeygen -q -t ed25519 -N "" -f $hostKeyPath | Out-Null
}

if (-not (Test-Path $clientKeyPath)) {
    & $sshKeygen -q -t rsa -b 3072 -m PEM -N "" -f $clientKeyPath | Out-Null
}

Get-Content "$clientKeyPath.pub" | Set-Content $authorizedKeysPath

$config = @"
Port $Port
ListenAddress 127.0.0.1
HostKey $($hostKeyPath -replace "\\", "/")
AuthorizedKeysFile $($authorizedKeysPath -replace "\\", "/")
PidFile $($pidPath -replace "\\", "/")
PasswordAuthentication no
KbdInteractiveAuthentication no
PubkeyAuthentication yes
ChallengeResponseAuthentication no
PermitEmptyPasswords no
StrictModes no
PermitTTY yes
AllowUsers $env:USERNAME
Subsystem sftp sftp-server.exe
LogLevel VERBOSE
"@

Set-Content -Path $configPath -Value $config

if (Test-Path $pidPath) {
    $existingPid = (Get-Content $pidPath -ErrorAction SilentlyContinue).Trim()
    if ($existingPid) {
        Stop-Process -Id $existingPid -Force -ErrorAction SilentlyContinue
    }
    Remove-Item $pidPath -Force -ErrorAction SilentlyContinue
}

& $sshd -t -f $configPath

$process = Start-Process -FilePath $sshd -ArgumentList @("-D", "-f", $configPath, "-E", $logPath) -PassThru -WindowStyle Hidden
Start-Sleep -Seconds 1

if ($process.HasExited) {
    throw "Local test sshd exited immediately. Check $logPath."
}

if (-not (Test-NetConnection -ComputerName 127.0.0.1 -Port $Port -InformationLevel Quiet)) {
    throw "Local test sshd did not open port $Port."
}

Write-Output "Local SSH test server is running."
Write-Output "Emulator host: 10.0.2.2"
Write-Output "Port: $Port"
Write-Output "Username: $env:USERNAME"
Write-Output "Private key path: $clientKeyPath"
Write-Output "Config path: $configPath"
Write-Output "Log path: $logPath"
