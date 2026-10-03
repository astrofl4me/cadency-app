$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$toolingRoot = Join-Path $projectRoot '.tooling'
$sdkRoot = Join-Path $env:TEMP 'PlannerApp-android-sdk'
New-Item -ItemType Directory -Force -Path $toolingRoot, $sdkRoot | Out-Null

function Get-Download([string]$Url, [string]$Destination) {
    if (!(Test-Path -LiteralPath $Destination)) {
        & curl.exe -fsSL --retry 3 $Url -o $Destination
        if ($LASTEXITCODE -ne 0) { throw "Download falhou: $Url" }
    }
}

if (!(Test-Path -LiteralPath (Join-Path $toolingRoot 'jdk/bin/java.exe'))) {
    Get-Download 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse' (Join-Path $toolingRoot 'jdk.zip')
    Expand-Archive -LiteralPath (Join-Path $toolingRoot 'jdk.zip') -DestinationPath (Join-Path $toolingRoot 'jdk-extract') -Force
    $jdkFolder = Get-ChildItem -LiteralPath (Join-Path $toolingRoot 'jdk-extract') -Directory | Select-Object -First 1
    # Copy, rather than move, keeps bootstrap recovery safe if interrupted.
    Copy-Item -LiteralPath $jdkFolder.FullName -Destination (Join-Path $toolingRoot 'jdk') -Recurse -Force
}
$env:JAVA_HOME = Join-Path $toolingRoot 'jdk'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:ANDROID_HOME = $sdkRoot

if (!(Test-Path -LiteralPath (Join-Path $sdkRoot 'cmdline-tools/latest/bin/sdkmanager.bat'))) {
    Get-Download 'https://dl.google.com/android/repository/commandlinetools-win-13114758_latest.zip' (Join-Path $toolingRoot 'sdk-tools.zip')
    Expand-Archive -LiteralPath (Join-Path $toolingRoot 'sdk-tools.zip') -DestinationPath (Join-Path $toolingRoot 'sdk-extract') -Force
    New-Item -ItemType Directory -Force -Path (Join-Path $sdkRoot 'cmdline-tools') | Out-Null
    Copy-Item -LiteralPath (Join-Path $toolingRoot 'sdk-extract/cmdline-tools') -Destination (Join-Path $sdkRoot 'cmdline-tools/latest') -Recurse -Force
}
$sdkManager = Join-Path $sdkRoot 'cmdline-tools/latest/bin/sdkmanager.bat'
1..30 | ForEach-Object { 'y' } | & $sdkManager "--sdk_root=$sdkRoot" --licenses
if ($LASTEXITCODE -ne 0) { throw 'Falha ao configurar licencas do SDK.' }
& $sdkManager "--sdk_root=$sdkRoot" 'platform-tools' 'platforms;android-36' 'build-tools;36.0.0'
if ($LASTEXITCODE -ne 0) { throw 'Falha ao instalar SDK.' }

$sdkProperty = $sdkRoot.Replace('\', '/').Replace(':', '\:')
[IO.File]::WriteAllText((Join-Path $projectRoot 'local.properties'), "sdk.dir=$sdkProperty`n", [Text.Encoding]::ASCII)
Get-Download 'https://raw.githubusercontent.com/gradle/gradle/v8.13.0/gradlew' (Join-Path $projectRoot 'gradlew')
Get-Download 'https://raw.githubusercontent.com/gradle/gradle/v8.13.0/gradlew.bat' (Join-Path $projectRoot 'gradlew.bat')
Get-Download 'https://raw.githubusercontent.com/gradle/gradle/v8.13.0/gradle/wrapper/gradle-wrapper.jar' (Join-Path $projectRoot 'gradle/wrapper/gradle-wrapper.jar')
Get-Download 'https://services.gradle.org/distributions/gradle-8.13-bin.zip.sha256' (Join-Path $toolingRoot 'gradle.sha256')
$checksum = (Get-Content -LiteralPath (Join-Path $toolingRoot 'gradle.sha256') -Raw).Trim()
$wrapperProperties = Join-Path $projectRoot 'gradle/wrapper/gradle-wrapper.properties'
$wrapperText = [IO.File]::ReadAllText($wrapperProperties)
$wrapperText = [regex]::Replace($wrapperText, '(?m)^distributionSha256Sum=.*\r?\n?', '')
[IO.File]::WriteAllText($wrapperProperties, ($wrapperText.TrimEnd() + "`ndistributionSha256Sum=$checksum`n"), [Text.Encoding]::ASCII)
& (Join-Path $env:JAVA_HOME 'bin/java.exe') -version
& (Join-Path $sdkRoot 'platform-tools/adb.exe') devices -l
Write-Output "SDK configurado em $sdkRoot"
