param([string[]]$Tasks = @('assembleDebug', 'testDebugUnitTest'), [switch]$Clean)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$localJdk = Join-Path $projectRoot '.tooling/jdk'
if (Test-Path -LiteralPath (Join-Path $localJdk 'bin/java.exe')) {
    $env:JAVA_HOME = $localJdk
    $env:PATH = "$localJdk\bin;$env:PATH"
}
$projectCache = Join-Path $projectRoot '.gradle-user-home'
$cacheAlias = Join-Path $env:TEMP 'PlannerApp-gradle-cache'
New-Item -ItemType Directory -Force -Path $projectCache | Out-Null
# Java 17 cannot read Gradle worker bootstrap classpaths with accented Windows paths.
if (!(Test-Path -LiteralPath $cacheAlias)) {
    New-Item -ItemType Junction -Path $cacheAlias -Target $projectCache | Out-Null
} elseif ((Get-Item -LiteralPath $cacheAlias -Force).Target -ne $projectCache) {
    throw 'O alias de cache pertence a outro projeto. Escolha outro caminho em scripts/build.ps1.'
}
$env:GRADLE_USER_HOME = $cacheAlias
$projectAlias = Join-Path $env:TEMP 'PlannerApp-project'
if (!(Test-Path -LiteralPath $projectAlias)) {
    New-Item -ItemType Junction -Path $projectAlias -Target $projectRoot | Out-Null
} elseif ((Get-Item -LiteralPath $projectAlias -Force).Target -ne $projectRoot) {
    throw 'O alias de projeto pertence a outra pasta. Escolha outro caminho em scripts/build.ps1.'
}
Push-Location $projectAlias
try {
    if ($Clean) { $Tasks = @('clean') + $Tasks }
    & "$env:JAVA_HOME/bin/java.exe" -cp (Join-Path $projectAlias 'gradle/wrapper/gradle-wrapper.jar') org.gradle.wrapper.GradleWrapperMain @Tasks --console=plain
    exit $LASTEXITCODE
} finally { Pop-Location }
