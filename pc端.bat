@echo off
setlocal

echo ============================================
echo   Community Rental System - H5 Launcher
echo   (Owner + Tenant in ONE app: /owner/* and /tenant/*)
echo   (Backend must be running on port 48080)
echo ============================================
echo.

cd /d "%~dp0h5"

echo [1/2] Checking dependencies ...
if exist node_modules (
  echo Dependencies already installed.
) else (
  echo Installing dependencies, please wait...
  call pnpm install
  if errorlevel 1 (
    echo.
    echo [ERROR] pnpm install failed.
    echo Please install Node.js 22+ and pnpm first.
    pause
    exit /b 1
  )
)

echo [2/2] Starting H5 dev server on port 8081 ...
start "H5-8081" cmd /k pnpm dev

echo.
echo H5 will be available at http://localhost:8081/
echo Owner login:  owner phone  + password  --^> /owner/home
echo Tenant login: tenant phone + password  --^> /tenant/home
echo Both roles can stay logged in at the same time (separate sessions).
echo Make sure the backend (port 48080) and Redis are running.
echo.
pause
