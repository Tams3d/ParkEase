@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

set "RESULT=0"
set "PAUSE_AFTER=1"
if /i "%~1"=="nopause" set "PAUSE_AFTER=0"

set "JAVAC=javac"
if not defined JAVA_HOME goto :java_ready
set "JAVA_DIR=%JAVA_HOME:"=%"
if exist "%JAVA_DIR%\bin\javac.exe" set "JAVAC=%JAVA_DIR%\bin\javac.exe"
:java_ready

"%JAVAC%" -version >nul 2>nul
if errorlevel 1 (
  echo javac was not found. Install JDK 17 LTS or newer and set JAVA_HOME if needed.
  set "RESULT=1"
  goto :finish
)

if not exist "out\classes" mkdir "out\classes"
set "SOURCES=out\sources.txt"
if exist "%SOURCES%" del "%SOURCES%"

for /r "src" %%f in (*.java) do (
  set "FILE=%%f"
  echo "!FILE:\=/!">>"%SOURCES%"
)

"%JAVAC%" -encoding UTF-8 -d "out\classes" @"%SOURCES%"
if errorlevel 1 (
  echo.
  echo Build failed.
  set "RESULT=1"
  goto :finish
)

echo Build successful: out\classes

:finish
if "%PAUSE_AFTER%"=="1" pause
exit /b %RESULT%
