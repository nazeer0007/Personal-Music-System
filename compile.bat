@echo off
echo ========================================================
echo           TUNEFLOW 🎵 - COMPILING PROJECT
echo ========================================================

if not exist "bin" mkdir bin

echo Compiling Java sources with JavaFX and MySQL driver...
javac -d bin --module-path "javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.media,javafx.fxml -cp "lib\*" src\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Compilation finished successfully!
    echo Compiled classes are in the 'bin' folder.
) else (
    echo.
    echo [ERROR] Compilation failed. Please check errors above.
)
pause
