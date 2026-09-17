@echo off
setlocal

echo ============================================
echo   Community Rental System - Startup
echo   Order: Redis - Backend (wait) - Frontend
echo ============================================
echo.

echo [1/3] Starting Redis on port 6379 ...
start "Redis-6379" /D "%USERPROFILE%\tools\redis" cmd /k redis-server.exe

echo [2/3] Starting Backend on port 48080 ...
start "Backend-48080" /D "%~dp0ruoyi-vue-pro" cmd /k %USERPROFILE%\.jdks\ms-17.0.19\bin\java.exe -jar yudao-server\target\yudao-server.jar

echo [2/3] Waiting for backend to be ready (about 15-30s, please wait)...
:wait_backend
timeout /t 2 /nobreak >nul
netstat -ano | findstr ":48080" | findstr "LISTENING" >nul
if errorlevel 1 goto wait_backend

echo [3/3] Backend is up. Starting Frontend on port 80 ...
start "Frontend-80" /D "%~dp0yudao-ui-admin-vue3" cmd /k pnpm dev

echo.
echo All started. The browser will open http://localhost/ automatically.
echo Login: admin / admin123
echo.
pause
