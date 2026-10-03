$ErrorActionPreference = 'Stop'
$plannerProjectRoot = Split-Path -Parent $PSScriptRoot
$plannerSdkCandidates = @()
$plannerPropertiesPath = Join-Path $plannerProjectRoot 'local.properties'
if (Test-Path -LiteralPath $plannerPropertiesPath) {
    $plannerSdkProperty = Get-Content -LiteralPath $plannerPropertiesPath -Encoding UTF8 | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
    if ($plannerSdkProperty) {
        $plannerSdkCandidates += $plannerSdkProperty.Substring(8).Replace('\:', ':').Replace('\\', '\').Replace('\ ', ' ')
    }
}
$plannerSdkCandidates += @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT, (Join-Path $env:LOCALAPPDATA 'Android/Sdk'), (Join-Path $env:TEMP 'PlannerApp-android-sdk'))
$plannerSdkRoot = $plannerSdkCandidates | Where-Object { $_ -and (Test-Path -LiteralPath (Join-Path $_ 'platform-tools/adb.exe')) } | Select-Object -First 1
if (!$plannerSdkRoot) { throw 'Android SDK nao encontrado. Configure local.properties pelo Android Studio ou execute scripts/bootstrap.ps1.' }
$plannerAdb = Join-Path $plannerSdkRoot 'platform-tools/adb.exe'
$plannerEmulator = Join-Path $plannerSdkRoot 'emulator/emulator.exe'
$env:ANDROID_HOME = $plannerSdkRoot
$env:ANDROID_SDK_ROOT = $plannerSdkRoot
