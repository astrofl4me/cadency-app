$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$env:ANDROID_HOME = Join-Path $env:TEMP 'PlannerApp-android-sdk'
$env:ANDROID_USER_HOME = Join-Path $env:ANDROID_HOME 'user'
$env:ANDROID_AVD_HOME = Join-Path $env:ANDROID_HOME 'avd'
$logRoot = Join-Path $projectRoot 'verification/local'
New-Item -ItemType Directory -Force -Path $logRoot | Out-Null
$emulatorPath = Join-Path $env:ANDROID_HOME 'emulator/emulator.exe'
$emulator = Start-Process -FilePath $emulatorPath -ArgumentList @('-avd', 'PlannerApp_API_35', '-no-window', '-no-audio', '-no-boot-anim', '-no-snapshot', '-accel', 'off', '-gpu', 'swiftshader', '-cores', '2', '-memory', '2048', '-port', '5554') -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $logRoot 'emulator-out.log') -RedirectStandardError (Join-Path $logRoot 'emulator-error.log')
Write-Output "Emulador iniciado: PID $($emulator.Id). Logs em $logRoot"
$emulator.WaitForExit()
exit $emulator.ExitCode
