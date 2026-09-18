@echo off
REM ============================================================
REM  LAN Access Helper - double-click this file.
REM
REM  Shows the URLs to open from a phone or another computer on
REM  the same Wi-Fi, and self-tests each one.
REM
REM  All real logic lives in scripts\lan-access.ps1 (batch quoting
REM  of PowerShell pipelines is too fragile). This file is
REM  intentionally ASCII-only and CRLF-terminated.
REM ============================================================

cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\lan-access.ps1"
echo.
pause
