@echo off
setlocal

cd /d "%~dp0"

where javac >nul 2>&1
if errorlevel 1 (
  echo Java JDK not found. Install a JDK and make sure javac is on PATH.
  pause
  exit /b 1
)

if not exist out mkdir out

echo Compiling sources...
javac -d out src\falafelshop\*.java
if errorlevel 1 (
  echo.
  echo Build failed. Fix compilation errors and try again.
  pause
  exit /b 1
)

echo Launching Falafel Shop Cashier System...
java -cp out falafelshop.Main
if errorlevel 1 (
  echo.
  echo Application exited with an error.
  pause
  exit /b 1
)

endlocal
