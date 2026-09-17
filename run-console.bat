@echo off
echo ========================================================
echo        STARTING TUNEFLOW 🎵 CONSOLE APPLICATION
echo ========================================================

:: Check if MySQL is running on port 3306
netstat -ano | findstr /R /C:":3306 " >nul
if %ERRORLEVEL% NEQ 0 (
    echo [INFO] MySQL is not detected on port 3306.
    if exist "C:\xampp\mysql\bin\mysqld.exe" (
        echo [INFO] Automatically launching MySQL from XAMPP...
        start "" /D "C:\xampp" /B "C:\xampp\mysql\bin\mysqld.exe" --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone
        timeout /t 3 /nobreak >nul
    ) else (
        echo [WARNING] Please ensure your MySQL server is running.
    )
)

echo [INFO] Launching Console CLI Application...
java --module-path "javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.media,javafx.fxml -cp "lib\*;bin" Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with code %ERRORLEVEL%
    pause
)
