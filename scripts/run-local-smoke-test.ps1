param(
    [string]$Command = "codex",
    [string]$OutputDir = (Join-Path $PSScriptRoot "..\artifacts\local-smoke"),
    [string]$AdbPath
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path $PSScriptRoot -Parent
$gradleWrapper = Join-Path $repoRoot "gradlew.bat"
$startSshdScript = Join-Path $PSScriptRoot "start-local-test-sshd.ps1"
$keyPath = Join-Path $env:LOCALAPPDATA "VoiceSshApp\local-sshd\client_rsa"
$defaultAdbPath = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
$remoteScreenshotDir = "/sdcard/Android/data/com.apple101012.voicessh/files/test-screenshots/local-ssh-smoke"

function Assert-LastExitCode {
    param(
        [string]$Step
    )

    if ($LASTEXITCODE -ne 0) {
        throw "$Step failed with exit code $LASTEXITCODE."
    }
}

if (-not $AdbPath) {
    $adbCommand = Get-Command adb.exe -ErrorAction SilentlyContinue
    if ($adbCommand) {
        $AdbPath = $adbCommand.Source
    } elseif (Test-Path $defaultAdbPath) {
        $AdbPath = $defaultAdbPath
    } else {
        throw "adb.exe was not found. Start from an Android SDK shell or pass -AdbPath."
    }
}

if (-not (Test-Path $gradleWrapper)) {
    throw "gradlew.bat was not found at $gradleWrapper."
}

& $startSshdScript

if (-not (Test-Path $keyPath)) {
    throw "Local SSH smoke-test key was not found at $keyPath."
}

$privateKey = Get-Content -Raw $keyPath
$encodedKey = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($privateKey))

$connectedDevices = (& $AdbPath devices) | Select-String "`tdevice$"
if (-not $connectedDevices) {
    throw "No Android emulator or device is connected. Start one, then rerun this script."
}

& $AdbPath wait-for-device | Out-Null
& $gradleWrapper installDebug installDebugAndroidTest
Assert-LastExitCode "Gradle installDebug installDebugAndroidTest"

& $AdbPath shell rm -rf $remoteScreenshotDir | Out-Null

$instrumentationOutput = & $AdbPath shell am instrument -w `
    -e class com.apple101012.voicessh.LocalSshSmokeTest `
    -e voiceSshCommand $Command `
    -e voiceSshPrivateKeyBase64 $encodedKey `
    com.apple101012.voicessh.test/androidx.test.runner.AndroidJUnitRunner
$instrumentationText = ($instrumentationOutput | Out-String)
$instrumentationOutput | Write-Output
Assert-LastExitCode "Instrumentation smoke test"
if ($instrumentationText -notmatch 'OK \(') {
    throw "Instrumentation smoke test did not report success."
}

$resolvedOutputDir = if ([System.IO.Path]::IsPathRooted($OutputDir)) {
    [System.IO.Path]::GetFullPath($OutputDir)
} else {
    [System.IO.Path]::GetFullPath((Join-Path $repoRoot $OutputDir))
}
if (Test-Path $resolvedOutputDir) {
    Remove-Item $resolvedOutputDir -Recurse -Force
}
New-Item -ItemType Directory -Path $resolvedOutputDir -Force | Out-Null

& $AdbPath pull $remoteScreenshotDir $resolvedOutputDir | Out-Null
Assert-LastExitCode "adb pull screenshots"

$pulledScreenshotDir = Join-Path $resolvedOutputDir "local-ssh-smoke"
if (-not (Test-Path $pulledScreenshotDir)) {
    throw "Screenshots were not pulled from the emulator."
}

Write-Output "Smoke test passed."
Write-Output "Screenshots: $pulledScreenshotDir"
