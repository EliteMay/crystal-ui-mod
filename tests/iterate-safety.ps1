# Regression guard: the development runner must never destroy save data.
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$f = Join-Path $root 'iterate.ps1'
if (!(Test-Path -LiteralPath $f)) { throw 'iterate.ps1 missing' }
$tokens = $null
$errors = $null
$ast = [System.Management.Automation.Language.Parser]::ParseFile($f, [ref]$tokens, [ref]$errors)
if ($errors.Count -gt 0) { throw "PowerShell parser errors: $($errors.Count)" }
$forbidden = @('Remove-Item','Stop-Process','Clear-Content','Move-Item','Rename-Item')
$commands = $ast.FindAll({ param($node) $node -is [System.Management.Automation.Language.CommandAst] }, $true)
foreach($command in $commands) {
    if ($command.GetCommandName() -in $forbidden) { throw "Forbidden command in iterate.ps1: $($command.GetCommandName())" }
}
$source = Get-Content -LiteralPath $f -Raw
if ($source -match '(?i)(run[\\/]saves|SendKeys|-Force\b)') {
    throw 'Destructive save operation, forced termination or keyboard automation detected'
}
if ($source -notmatch '\bgradlew\.bat\b') { throw 'Gradle build step missing' }
Write-Output 'PASS: safe iteration syntax, no destructive world/process/keyboard operations'
