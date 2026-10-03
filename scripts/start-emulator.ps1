param([string]$AvdName = 'PlannerApp_API_35', [switch]$InstallApp, [switch]$Window, [int]$BootTimeoutSeconds = 240)
. (Join-Path $PSScriptRoot 'android-environment.ps1')
if (!(Test-Path -LiteralPath $plannerEmulator)) { throw 'Emulator nao encontrado. Execute scripts/prepare-emulator.ps1.' }
$availableAvds = @(& $plannerEmulator -list-avds)
if ($AvdName -notin $availableAvds) { throw "AVD $AvdName nao encontrado. Execute scripts/prepare-emulator.ps1." }
$acceleration = @(& $plannerEmulator -accel-check 2>&1)
if ($LASTEXITCODE -ne 0) {
    $acceleration | Write-Output
    throw 'Aceleracao indisponivel. Execute scripts/enable-emulator-acceleration.ps1 e reinicie o Windows se solicitado.'
}

function Find-PlannerDevice {
    $deviceRows = @(& $plannerAdb devices)
    foreach ($row in $deviceRows) {
        if ($row -match '^(emulator-\d+)\s+device$') {
            $serial = $Matches[1]
            $name = @(& $plannerAdb -s $serial emu avd name 2>$null) | Where-Object { $_ -and $_ -ne 'OK' } | Select-Object -First 1
            if ($name -eq $AvdName) { return $serial }
        }
    }
    return $null
}

$serial = Find-PlannerDevice
if (!$serial) {
    $logRoot = Join-Path $plannerProjectRoot 'verification/local'
    New-Item -ItemType Directory -Force -Path $logRoot | Out-Null
    $emulatorArguments = @('-avd', $AvdName, '-accel', 'on', '-gpu', 'auto', '-no-snapshot-load', '-no-boot-anim', '-no-audio', '-cores', '2', '-memory', '2048')
    if (!$Window) { $emulatorArguments += '-no-window' }
    $windowStyle = if ($Window) { 'Normal' } else { 'Hidden' }
    $launcher = Start-Process -FilePath $plannerEmulator -ArgumentList $emulatorArguments -WindowStyle $windowStyle -PassThru -RedirectStandardOutput (Join-Path $logRoot 'emulator-out.log') -RedirectStandardError (Join-Path $logRoot 'emulator-error.log')
    Write-Output "Iniciando $AvdName, PID $($launcher.Id). Logs em $logRoot"
}

$deadline = [DateTime]::UtcNow.AddSeconds($BootTimeoutSeconds)
$bootCompleted = $false
while ([DateTime]::UtcNow -lt $deadline) {
    if (!$serial) { $serial = Find-PlannerDevice }
    if ($serial) {
        $bootState = @(& $plannerAdb -s $serial shell getprop sys.boot_completed 2>$null)
        if ($bootState -contains '1') { $bootCompleted = $true; break }
    }
    Start-Sleep -Seconds 2
}
if (!$bootCompleted) { throw 'Android nao concluiu o boot. Confira verification/local/emulator-error.log e emulator-out.log.' }
Write-Output "Android pronto em $serial. Abra Android Emulator: Open Embedded View no EmbeDroid."

if ($InstallApp) {
    $apkPath = Join-Path $plannerProjectRoot 'app/build/outputs/apk/debug/app-debug.apk'
    if (!(Test-Path -LiteralPath $apkPath)) { throw 'APK nao encontrado. Execute scripts/build.ps1.' }
    & $plannerAdb -s $serial install -r $apkPath
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao instalar o APK. Confira a mensagem do adb.' }
    & $plannerAdb -s $serial shell am start -n br.edu.fsa.planner/.MainActivity
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao abrir o planner.' }
}
