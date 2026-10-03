param([Parameter(Mandatory = $true)][string]$Message)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$gitPath = $projectRoot.Replace('\', '/')
$authorName = & git -c "safe.directory=$gitPath" config --get user.name
$authorEmail = & git -c "safe.directory=$gitPath" config --get user.email
if (!$authorName -or !$authorEmail) {
    Write-Output 'Identidade Git ausente. Nenhum autor foi inventado; arquivos preparados para commit.'
    exit 0
}
& git -c "safe.directory=$gitPath" -C $projectRoot add --all
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& git -c "safe.directory=$gitPath" -C $projectRoot commit -m $Message
exit $LASTEXITCODE
