@echo off
setlocal
cd /d "%~dp0"

set "RESULT=0"
set "PAUSE_AFTER=1"
if /i "%~1"=="nopause" set "PAUSE_AFTER=0"

set "JAVA=java"
if not defined JAVA_HOME goto :java_ready
set "JAVA_DIR=%JAVA_HOME%"
set "JAVA_DIR=%JAVA_DIR:"=%"
if "%JAVA_DIR:~-1%"=="\" set "JAVA_DIR=%JAVA_DIR:~0,-1%"
if not exist "%JAVA_DIR%\bin\java.exe" goto :java_ready
set "JAVA=%JAVA_DIR%\bin\java.exe"
set "JAVA_HOME=%JAVA_DIR%"
:java_ready

"%JAVA%" -version >nul 2>nul
if errorlevel 1 (
  echo Java was not found. Install JDK 17 LTS or newer and set JAVA_HOME if needed.
  set "RESULT=1"
  goto :finish
)

call mvnw.cmd -q compile exec:java
if errorlevel 1 set "RESULT=1"

:finish
if "%PAUSE_AFTER%"=="1" pause
exit /b %RESULT%
