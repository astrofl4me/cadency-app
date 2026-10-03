$ErrorActionPreference = 'Stop'
$plannerProjectRoot = Split-Path -Parent $PSScriptRoot
$resultDirectory = Join-Path $plannerProjectRoot 'verification/local'
New-Item -ItemType Directory -Force -Path $resultDirectory | Out-Null
$resultFile = Join-Path $resultDirectory 'acceleration-result.json'
$plannerPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (!$plannerPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Output 'O Windows solicitara autorizacao de administrador para habilitar HypervisorPlatform. Nao havera reinicio automatico.'
    $elevatedArguments = '-NoProfile -ExecutionPolicy Bypass -File "{0}"' -f $PSCommandPath
    $worker = Start-Process -FilePath (Join-Path $PSHOME 'powershell.exe') -ArgumentList $elevatedArguments -Verb RunAs -WindowStyle Hidden -PassThru -Wait
    $worker.Refresh()
    if (Test-Path -LiteralPath $resultFile) { Get-Content -LiteralPath $resultFile -Encoding UTF8 }
    exit $worker.ExitCode
}

try {
    $hypervisorActive = [bool](Get-CimInstance Win32_ComputerSystem).HypervisorPresent
    $processor = Get-CimInstance Win32_Processor | Select-Object -First 1
    if (!$hypervisorActive -and !$processor.VirtualizationFirmwareEnabled) { throw 'Ative VT-x/SVM na BIOS antes de habilitar a aceleracao.' }
    $featureBefore = Get-WindowsOptionalFeature -Online -FeatureName HypervisorPlatform
    $restartNeeded = $false
    if ($featureBefore.State -ne 'Enabled') {
        $featureChange = Enable-WindowsOptionalFeature -Online -FeatureName HypervisorPlatform -All -NoRestart
        $restartNeeded = [bool]$featureChange.RestartNeeded
    }
    $featureAfter = Get-WindowsOptionalFeature -Online -FeatureName HypervisorPlatform
    [pscustomobject]@{
        Success = $true
        Feature = 'HypervisorPlatform'
        PreviousState = [string]$featureBefore.State
        CurrentState = [string]$featureAfter.State
        RestartNeeded = $restartNeeded -or $featureAfter.State -eq 'EnablePending' -or !$hypervisorActive
        CheckedAt = [DateTime]::UtcNow.ToString('o')
    } | ConvertTo-Json | Set-Content -LiteralPath $resultFile -Encoding UTF8
    exit 0
} catch {
    [pscustomobject]@{ Success = $false; Error = $_.Exception.Message; CheckedAt = [DateTime]::UtcNow.ToString('o') } |
        ConvertTo-Json | Set-Content -LiteralPath $resultFile -Encoding UTF8
    exit 1
}
