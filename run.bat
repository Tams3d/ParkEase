@echo off
setlocal
cd /d "%~dp0"

set "RESULT=0"
set "PAUSE_AFTER=1"
if /i "%~1"=="nopause" set "PAUSE_AFTER=0"

set "JAVA=java"
if not defined JAVA_HOME goto :java_ready
set "JAVA_DIR=%JAVA_HOME:"=%"
if exist "%JAVA_DIR%\bin\java.exe" set "JAVA=%JAVA_DIR%\bin\java.exe"
:java_ready

"%JAVA%" -version >nul 2>nul
if errorlevel 1 (
  echo Java was not found. Install JDK 17 LTS or newer and set JAVA_HOME if needed.
  set "RESULT=1"
  goto :finish
)

call build.bat nopause
if errorlevel 1 (
  set "RESULT=1"
  goto :finish
)

dir /b "lib\mysql-connector-j-*.jar" >nul 2>nul
if errorlevel 1 (
  echo.
  echo MySQL Connector/J was not found in lib\.
  echo Download mysql-connector-j-26.7.0.jar from:
  echo   https://dev.mysql.com/downloads/connector/j/
  set "RESULT=1"
  goto :finish
)

"%JAVA%" -cp "out\classes;lib\*" parkease.Main
if errorlevel 1 set "RESULT=1"

:finish
if "%PAUSE_AFTER%"=="1" pause
exit /b %RESULT%
