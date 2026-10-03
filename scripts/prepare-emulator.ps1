$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$env:JAVA_HOME = Join-Path $projectRoot '.tooling/jdk'
$env:ANDROID_HOME = Join-Path $env:TEMP 'PlannerApp-android-sdk'
$env:ANDROID_USER_HOME = Join-Path $env:ANDROID_HOME 'user'
$env:ANDROID_AVD_HOME = Join-Path $env:ANDROID_HOME 'avd'
New-Item -ItemType Directory -Force -Path $env:ANDROID_USER_HOME, $env:ANDROID_AVD_HOME | Out-Null
$sdkClassPath = Join-Path $env:ANDROID_HOME 'cmdline-tools/latest/lib/sdkmanager-classpath.jar'
& "$env:JAVA_HOME/bin/java.exe" -cp $sdkClassPath com.android.sdklib.tool.sdkmanager.SdkManagerCli "--sdk_root=$env:ANDROID_HOME" 'emulator' 'system-images;android-35;default;x86_64'
if ($LASTEXITCODE -ne 0) { throw 'Falha ao instalar pacotes do emulador.' }
$avdClassPath = Join-Path $env:ANDROID_HOME 'cmdline-tools/latest/lib/avdmanager-classpath.jar'
if (!(Test-Path -LiteralPath (Join-Path $env:ANDROID_AVD_HOME 'PlannerApp_API_35.ini'))) {
    'no' | & "$env:JAVA_HOME/bin/java.exe" "-Dcom.android.sdkmanager.toolsdir=$env:ANDROID_HOME/cmdline-tools/latest" -cp $avdClassPath com.android.sdklib.tool.AvdManagerCli create avd -n PlannerApp_API_35 -k 'system-images;android-35;default;x86_64' -d pixel_5
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar AVD.' }
}
& "$env:ANDROID_HOME/emulator/emulator.exe" -accel-check
Write-Output "AVD preparado em $env:ANDROID_AVD_HOME"
