param(
  [switch]$StopContainers = $true,
  [string[]]$Containers = @("postgres_container","pgadmin_container")
)

$ErrorActionPreference = "Stop"

function Stop-ByPidFile {
    param([string]$path,[string]$name)
    if (Test-Path $path) {
        $pidText   = Get-Content $path | Select-Object -First 1
        $targetPid = 0; [void][int]::TryParse($pidText, [ref]$targetPid)
        if ($targetPid -gt 0) {
            $proc = Get-Process -Id $targetPid -ErrorAction SilentlyContinue
            if ($proc) { Write-Host ("Stopping {0} (PID {1})..." -f $name,$targetPid); Stop-Process -Id $targetPid -Force }
            else       { Write-Host ("{0} not running (PID {1} not found)." -f $name,$targetPid) }
        } else { Write-Host ("Invalid PID in {0}: '{1}'" -f $path,$pidText) }
        Remove-Item $path -Force -ErrorAction SilentlyContinue
    } else { Write-Host ("No PID file for {0}." -f $name) }
}

function Stop-Containers([string[]]$names) {
    if (-not $names -or $names.Count -eq 0) { return }
    foreach ($name in $names) {
        $id = & docker ps -q --filter "name=^${name}$"
        if ($id) { Write-Host "Stopping container '$name'..."; & docker stop $id | Out-Null; Write-Host "Stopped '$name'." }
        else     { Write-Host "Container '$name' not running (or name differs)." }
    }
}

# stop processes started by run-dev.ps1
Stop-ByPidFile -path (Join-Path $PSScriptRoot ".ngrok.pid")    -name "ngrok"
Stop-ByPidFile -path (Join-Path $PSScriptRoot ".backend.pid")  -name "backend (mvnw spring-boot:run)"
Stop-ByPidFile -path (Join-Path $PSScriptRoot ".frontend.pid") -name "frontend (python http.server)"

if ($StopContainers) { Stop-Containers -names $Containers }
Write-Host "Done."


