@echo off
SETLOCAL

IF NOT EXIST "%ProgramFiles(x86)%" SET ProgramFiles(x86)=%ProgramFiles%
IF NOT EXIST "%ProgramFiles%\JetBrains" (
	echo.
	echo Warning: "%ProgramFiles%\JetBrains" not found, build may break!
	echo.
)

@echo on
mvn clean package site

ENDLOCAL