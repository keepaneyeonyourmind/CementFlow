# Локальный запуск учебного сервера в текущем сеансе Windows.
$ErrorActionPreference = 'Stop'
$secretPath = Join-Path $env:LOCALAPPDATA 'CementFlow\postgres-app.secure'
if (-not (Test-Path -LiteralPath $secretPath)) { throw 'Не найден зашифрованный пароль учебной базы для текущего пользователя Windows.' }
$javaPath = Join-Path $env:LOCALAPPDATA 'Programs\Android Studio\jbr'
if (-not (Test-Path -LiteralPath (Join-Path $javaPath 'bin\java.exe'))) { throw 'Не найдена Java из Android Studio; настройте JAVA_HOME вручную и используйте инструкции README.' }
$secret = (Get-Content -Raw -LiteralPath $secretPath).Trim() | ConvertTo-SecureString
$env:JAVA_HOME = $javaPath
$env:CEMENTFLOW_DB_URL = 'jdbc:postgresql://localhost:5432/cementflow_demo'
$env:CEMENTFLOW_DB_USER = 'cementflow_app'
$env:CEMENTFLOW_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $secret).Password
Push-Location $PSScriptRoot
try { & .\gradlew.bat bootRun; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE } }
finally { Remove-Item Env:CEMENTFLOW_DB_PASSWORD -ErrorAction SilentlyContinue; Pop-Location }
