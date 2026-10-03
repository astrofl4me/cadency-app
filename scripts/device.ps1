param([Parameter(ValueFromRemainingArguments = $true)][string[]]$AdbArguments)
$ErrorActionPreference = 'Stop'
$sdkRoot = Join-Path $env:TEMP 'PlannerApp-android-sdk'
$adb = Join-Path $sdkRoot 'platform-tools/adb.exe'
if (!(Test-Path -LiteralPath $adb)) {
    $adb = (Get-Command adb -ErrorAction Stop).Source
}
& $adb @AdbArguments
exit $LASTEXITCODE
