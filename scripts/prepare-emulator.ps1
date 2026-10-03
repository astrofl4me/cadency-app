param([string]$AvdName = 'PlannerApp_API_35')
. (Join-Path $PSScriptRoot 'android-environment.ps1')
$env:JAVA_HOME = Join-Path $plannerProjectRoot '.tooling/jdk'
$env:ANDROID_USER_HOME = Join-Path $env:USERPROFILE '.android'
$env:ANDROID_AVD_HOME = Join-Path $env:ANDROID_USER_HOME 'avd'
New-Item -ItemType Directory -Force -Path $env:ANDROID_USER_HOME, $env:ANDROID_AVD_HOME | Out-Null
$imagePath = Join-Path $plannerSdkRoot 'system-images/android-35/default/x86_64/system.img'
if (!(Test-Path -LiteralPath $plannerEmulator) -or !(Test-Path -LiteralPath $imagePath)) {
    $sdkClassPath = Join-Path $plannerSdkRoot 'cmdline-tools/latest/lib/sdkmanager-classpath.jar'
    & "$env:JAVA_HOME/bin/java.exe" -cp $sdkClassPath com.android.sdklib.tool.sdkmanager.SdkManagerCli "--sdk_root=$plannerSdkRoot" 'emulator' 'system-images;android-35;default;x86_64'
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao instalar pacotes do emulador.' }
}
$avdIni = Join-Path $env:ANDROID_AVD_HOME "$AvdName.ini"
$legacyIni = Join-Path $plannerSdkRoot "avd/$AvdName.ini"
if (!(Test-Path -LiteralPath $avdIni) -and (Test-Path -LiteralPath $legacyIni)) {
    # Register the existing AVD at the standard location; keep its data in place.
    Copy-Item -LiteralPath $legacyIni -Destination $avdIni
}
$avdClassPath = Join-Path $env:ANDROID_HOME 'cmdline-tools/latest/lib/avdmanager-classpath.jar'
if (!(Test-Path -LiteralPath $avdIni)) {
    'no' | & "$env:JAVA_HOME/bin/java.exe" "-Dcom.android.sdkmanager.toolsdir=$env:ANDROID_HOME/cmdline-tools/latest" -cp $avdClassPath com.android.sdklib.tool.AvdManagerCli create avd -n $AvdName -k 'system-images;android-35;default;x86_64' -d pixel_5
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar AVD.' }
}
& $plannerEmulator -list-avds
Write-Output "AVD registrado em $avdIni"
