param(
    [string]$BackendDir = "$PSScriptRoot",  # path to your Spring Boot project (folder with mvnw)
    [int]$BackendPort = 8080,
    [string[]]$Containers = @("postgres_container","pgadmin_container"), # container names to ensure running
    [string]$NgrokRegion = "ap", # your ngrok region (e.g., us, eu, ap)
    [switch]$WriteEnv = $false,   # if set, will write BACKEND_URL to $PSScriptRoot\.env
    [string]$EnvKey = "BACKEND_URL",

     # FRONTEND
    [string]$FrontendDir  = (Join-Path $PSScriptRoot 'Frontend'),  # parent folder that contains your web files
    [int]   $FrontendPort = 5500,
    [switch]$OpenBrowser  = $true,
    [switch]$PatchApiBase = $true
)

if (-not (Test-Path $BackendDir)) {
  if (Test-Path (Join-Path $PSScriptRoot 'mvnw')) { $BackendDir = $PSScriptRoot }
  elseif (Test-Path (Join-Path $PSScriptRoot 'mvnw.cmd')) { $BackendDir = $PSScriptRoot }
}

$ErrorActionPreference = "Stop"

function Write-Section($title) {
    Write-Host ("`n==== {0} ====" -f $title) -ForegroundColor Cyan
}

function Start-DockerDesktop {
    # Start Docker Desktop if not running; then wait until 'docker info' succeeds
    $dockerOk = $false
    Write-Section "Starting Docker Desktop (if needed)"
    try {
        docker version | Out-Null
        $dockerOk = $true
        Write-Host "Docker CLI found."
    } catch {
        Write-Host "Docker CLI not found in PATH. Make sure Docker Desktop is installed." -ForegroundColor Red
        throw
    }

    $isRunning = $false
    try {
        docker info | Out-Null
        $isRunning = $true
    } catch {}

    if (-not $isRunning) {
        $dockerExe = "${Env:ProgramFiles}\Docker\Docker\Docker Desktop.exe"
        if (-not (Test-Path $dockerExe)) {
            $dockerExe = "${Env:ProgramFiles(x86)}\Docker\Docker\Docker Desktop.exe"
        }
        if (-not (Test-Path $dockerExe)) {
            Write-Host "Couldn't find Docker Desktop at the default path. If Docker Desktop is installed, start it once so it's added to PATH." -ForegroundColor Yellow
        } else {
            Write-Host "Launching Docker Desktop..."
            Start-Process -FilePath $dockerExe | Out-Null
        }

        Write-Host "Waiting for Docker engine to be ready (this can take ~30-60s)..."
        $retries = 120
        while ($retries -gt 0) {
            try {
                docker info | Out-Null
                $isRunning = $true
                break
            } catch {
                Start-Sleep -Seconds 1
                $retries--
            }
        }
        if (-not $isRunning) {
            throw "Docker did not become ready in time."
        }
    }
    Write-Host "Docker is ready."
}

function Ensure-ContainersRunning([string[]]$names) {
    if (-not $names -or $names.Count -eq 0) { return }
    Write-Section "Ensuring containers are running"
    $existing = (& docker ps -a --format "{{.Names}}")
    foreach ($name in $names) {
        if (-not $existing -or ($existing -notcontains $name)) {
            Write-Host "Container '$name' not found. Skipping start. (Make sure you've created it, or use compose.)" -ForegroundColor Yellow
            continue
        }
        $isUp = (& docker ps --format "{{.Names}}" | Where-Object { $_ -eq $name })
        if ($isUp) {
            Write-Host "Container '$name' already running."
        } else {
            Write-Host "Starting container '$name'..."
            & docker start $name | Out-Null
            Write-Host "Started '$name'."
        }
    }
}

function Start-Backend([string]$dir, [int]$port) {
    Write-Section "Starting Spring Boot backend"
    if (-not (Test-Path $dir)) { throw "BackendDir not found: $dir" }

    # Prefer Windows launcher when present
    $mvnwCmd = Join-Path $dir "mvnw.cmd"
    if (-not (Test-Path $mvnwCmd)) { $mvnwCmd = Join-Path $dir "mvnw" }
    if (-not (Test-Path $mvnwCmd)) { throw "Could not find mvnw(.cmd) in $dir" }

    $logDir = Join-Path $PSScriptRoot "logs"
    New-Item -ItemType Directory -Force -Path $logDir | Out-Null
    $backendOut = Join-Path $logDir "backend.out.log"
    $backendErr = Join-Path $logDir "backend.err.log"

    $env:SERVER_PORT = "$port"
    Write-Host "Launching: $mvnwCmd spring-boot:run (port $port)"
    $proc = Start-Process `
        -FilePath $mvnwCmd `
        -ArgumentList "spring-boot:run" `
        -WorkingDirectory $dir `
        -RedirectStandardOutput $backendOut `
        -RedirectStandardError  $backendErr `
        -PassThru

    $proc.Id | Out-File -FilePath (Join-Path $PSScriptRoot ".backend.pid") -Encoding ascii

    Write-Host "Waiting for backend to accept connections on http://localhost:$port ..."
    $max = 180
    while ($max -gt 0) {
        try {
            $resp = Invoke-WebRequest -Uri "http://localhost:$port/actuator/health" -UseBasicParsing -TimeoutSec 2
            if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500) { break }
        } catch {}
        Start-Sleep -Seconds 2
        $max -= 2
    }
    Write-Host "Backend is starting (logs: $backendOut and $backendErr)."
}


function Start-Ngrok([int]$port, [string]$region) {
    Write-Section "Starting ngrok tunnel"
    $logDir = Join-Path $PSScriptRoot "logs"
    New-Item -ItemType Directory -Force -Path $logDir | Out-Null
    $ngrokOut = Join-Path $logDir "ngrok.out.log"
    $ngrokErr = Join-Path $logDir "ngrok.err.log"

    $args = @("http", "$port", "--region", "$region", "--log", "stdout")
    $ngProc = Start-Process -FilePath "ngrok" -ArgumentList $args `
        -RedirectStandardOutput $ngrokOut `
        -RedirectStandardError  $ngrokErr `
        -PassThru
    $ngProc.Id | Out-File -FilePath (Join-Path $PSScriptRoot ".ngrok.pid") -Encoding ascii

    # Wait for ngrok API, then fetch the https URL
    $publicUrl = $null
    $timeout = (Get-Date).AddSeconds(90)
    while (-not $publicUrl -and (Get-Date) -lt $timeout) {
        try {
            $tunnels = Invoke-RestMethod -Uri "http://127.0.0.1:4040/api/tunnels"
            foreach ($t in $tunnels.tunnels) {
                if ($t.proto -eq "https") { $publicUrl = $t.public_url; break }
            }
        } catch {}
        if (-not $publicUrl) { Start-Sleep -Seconds 2 }
    }
    if ($publicUrl) { Write-Host ("ngrok is ready: {0}" -f $publicUrl) -ForegroundColor Green }
    else { Write-Host "Couldn't get ngrok public URL (see $ngrokOut / $ngrokErr)" }

    return $publicUrl
}

function Update-ApiBaseInHtml([string]$dir, [string]$newUrl) {
    Write-Section "Updating API_BASE in HTML files"
    if (-not (Test-Path $dir)) { Write-Host "FrontendDir not found: $dir" -ForegroundColor Yellow; return }

    # If index.html isn't directly inside $dir, find it and reset $dir to that folder.
    if (-not (Test-Path (Join-Path $dir 'index.html'))) {
        $idx = Get-ChildItem -Path $dir -Filter index.html -Recurse -File -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($idx) { $dir = $idx.DirectoryName }
    }

    $files = Get-ChildItem -Path $dir -Recurse -Filter *.html
    if (-not $files) { Write-Host "No .html files found under $dir" -ForegroundColor Yellow; return }

    $pattern = 'const\s+API_BASE\s*=\s*([\"''][^\"'']+[\"''])\s*;'
    foreach ($f in $files) {
        $text = Get-Content $f.FullName -Raw
        if ($text -match $pattern) {
            $bak = "$($f.FullName).bak"; if (-not (Test-Path $bak)) { Copy-Item $f.FullName $bak -Force }
            $repl = "const API_BASE = '$newUrl';"
            $new  = [regex]::Replace($text, $pattern, $repl)
            Set-Content -Path $f.FullName -Value $new -Encoding UTF8
            Write-Host "Patched: $($f.FullName)"
        }
    }
}

function Start-Frontend([string]$dir, [int]$port) {
    Write-Section "Starting static frontend server (python)"
    if (-not (Test-Path $dir)) { Write-Host "FrontendDir not found: $dir" -ForegroundColor Yellow; return $null }

    # If index.html isn't directly inside $dir, serve from the folder that contains it.
    if (-not (Test-Path (Join-Path $dir 'index.html'))) {
        $idx = Get-ChildItem -Path $dir -Filter index.html -Recurse -File -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($idx) { $dir = $idx.DirectoryName }
    }

    $logDir = Join-Path $PSScriptRoot "logs"; New-Item -ItemType Directory -Force -Path $logDir | Out-Null
    $feOut = Join-Path $logDir "frontend.out.log"; $feErr = Join-Path $logDir "frontend.err.log"
    $args = @("-m","http.server","$port","--directory","$dir")
    $proc = Start-Process -FilePath "python" -ArgumentList $args -RedirectStandardOutput $feOut -RedirectStandardError $feErr -PassThru
    $proc.Id | Out-File -FilePath (Join-Path $PSScriptRoot ".frontend.pid") -Encoding ascii
    Write-Host "Frontend server: http://localhost:$port (root: $dir)  (logs: $feOut, $feErr)"
    return $proc
}

function Open-Edge([string]$url) {
    try {
        $edge = "${Env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe"
        if (-not (Test-Path $edge)) { $edge = "${Env:ProgramFiles}\Microsoft\Edge\Application\msedge.exe" }
        if (Test-Path $edge) { Start-Process -FilePath $edge -ArgumentList $url | Out-Null } else { Start-Process $url | Out-Null }
        Write-Host "Opened $url"
    } catch { Write-Host "Could not open browser: $($_.Exception.Message)" -ForegroundColor Yellow }
}

# ---------- Orchestration ----------
Start-DockerDesktop
Ensure-ContainersRunning -names $Containers
Start-Backend -dir $BackendDir -port $BackendPort
$publicUrl = Start-Ngrok -port $BackendPort -region $NgrokRegion

if ($WriteEnv -and $publicUrl) {
    $envPath = Join-Path $PSScriptRoot ".env"
    "$EnvKey=$publicUrl" | Out-File -FilePath $envPath -Encoding ascii
    Write-Host "Wrote $EnvKey to $envPath"
}

if ($PatchApiBase -and $publicUrl) {
    Update-ApiBaseInHtml -dir $FrontendDir -newUrl $publicUrl
}

$feProc = Start-Frontend -dir $FrontendDir -port $FrontendPort
if ($OpenBrowser) { Open-Edge ("http://localhost:{0}/index.html" -f $FrontendPort) }

Write-Host "`nAll set. Backend on http://localhost:$BackendPort"
if ($publicUrl) { Write-Host "Public URL: $publicUrl" }
Write-Host "Use stop-dev.ps1 to stop background processes."
