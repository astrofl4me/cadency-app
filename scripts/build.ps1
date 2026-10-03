param([string[]]$Tasks = @('assembleDebug', 'testDebugUnitTest'))
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$localJdk = Join-Path $projectRoot '.tooling/jdk'
if (Test-Path -LiteralPath (Join-Path $localJdk 'bin/java.exe')) {
    $env:JAVA_HOME = $localJdk
    $env:PATH = "$localJdk\bin;$env:PATH"
}
$env:GRADLE_USER_HOME = Join-Path $projectRoot '.gradle-user-home'
Push-Location $projectRoot
try {
    & .\gradlew.bat @Tasks --console=plain
    exit $LASTEXITCODE
} finally { Pop-Location }
