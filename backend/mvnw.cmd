@echo off
setlocal

where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  mvn %*
  exit /b %ERRORLEVEL%
)

set "BASE_DIR=%~dp0"
if not defined MAVEN_VERSION set "MAVEN_VERSION=3.9.9"
set "WRAPPER_DIR=%BASE_DIR%.mvn\wrapper"
set "MAVEN_HOME=%WRAPPER_DIR%\apache-maven-%MAVEN_VERSION%"
set "ARCHIVE=%WRAPPER_DIR%\apache-maven-%MAVEN_VERSION%-bin.zip"
set "DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
  if not exist "%ARCHIVE%" (
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference = 'Stop'; Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%ARCHIVE%'"
    if errorlevel 1 exit /b %ERRORLEVEL%
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference = 'Stop'; Expand-Archive -Path '%ARCHIVE%' -DestinationPath '%WRAPPER_DIR%' -Force"
  if errorlevel 1 exit /b %ERRORLEVEL%
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
