@echo off

IF NOT EXIST "%ProgramFiles%\JetBrains" (
	echo.
	echo Warning: "%ProgramFiles%\JetBrains" not found, build may break!
	echo Make sure build is running in 32bit mode to fix path issues.
	echo.
)

@echo on
mvn clean assembly:assembly site