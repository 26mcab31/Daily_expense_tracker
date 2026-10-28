$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$sourceRoot = Join-Path $projectRoot "src"
$webRoot = Join-Path $projectRoot "web"
$dataRoot = Join-Path $projectRoot "data"
$webInfRoot = Join-Path $projectRoot "webapp\WEB-INF"
$servletApi = Join-Path $projectRoot "lib\servlet-api.jar.jar"
$mysqlConnector = Join-Path $projectRoot "lib\mysql-connector-j-26.10.0.jar"
$usersJson = Join-Path $dataRoot "users.json"
$stagingRoot = Join-Path $projectRoot "build\DailyExpenseTracker"
$classesRoot = Join-Path $stagingRoot "WEB-INF\classes"
$librariesRoot = Join-Path $stagingRoot "WEB-INF\lib"
$stagingDataRoot = Join-Path $stagingRoot "data"

foreach ($requiredPath in @(
    $sourceRoot,
    $webRoot,
    $usersJson,
    $webInfRoot,
    $servletApi,
    $mysqlConnector
)) {
    if (-not (Test-Path $requiredPath)) {
        throw "Required project path not found: $requiredPath"
    }
}

$javac = Get-Command javac -ErrorAction SilentlyContinue
if ($null -eq $javac) {
    throw "javac is required. Install a JDK 11 or newer and add it to PATH."
}

if (Test-Path $stagingRoot) {
    Remove-Item $stagingRoot -Recurse -Force
}

New-Item $classesRoot -ItemType Directory -Force | Out-Null
New-Item $librariesRoot -ItemType Directory -Force | Out-Null
New-Item $stagingDataRoot -ItemType Directory -Force | Out-Null

$sourceFiles = Get-ChildItem $sourceRoot -Filter "*.java" -Recurse |
    ForEach-Object { $_.FullName }
if ($sourceFiles.Count -eq 0) {
    throw "No Java sources found under $sourceRoot"
}

$classpath = "$servletApi;$mysqlConnector"
& $javac.Source --release 11 -encoding UTF-8 -classpath $classpath `
    -d $classesRoot @sourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed with exit code $LASTEXITCODE."
}

Copy-Item (Join-Path $webRoot "*") $stagingRoot -Recurse -Force
Copy-Item (Join-Path $webInfRoot "web.xml") `
    (Join-Path $stagingRoot "WEB-INF\web.xml") -Force
Copy-Item (Join-Path $webInfRoot "dashboard.jsp") `
    (Join-Path $stagingRoot "WEB-INF\dashboard.jsp") -Force
Copy-Item $mysqlConnector $librariesRoot -Force
Copy-Item $usersJson $stagingDataRoot -Force

Write-Output "Build complete. Staged web application: $stagingRoot"
Write-Output "The live webapp folder was not modified or deployed."
