$ErrorActionPreference='Stop'
$maven=Join-Path $PSScriptRoot '..\..\tools\apache-maven-3.9.16\bin\mvn.cmd'
if(-not (Test-Path $maven)){throw "Maven was not found at $maven"}
& $maven clean verify
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
Write-Host "Coverage: $PSScriptRoot\..\target\site\jacoco\index.html" -ForegroundColor Green
