@echo off
SETLOCAL
SET JDK32=C:\Projects\DevEnv\jdk32

IF EXIST %JDK32% (
	echo.
	echo Ensuring build is 32bit, using JDK %JDK32%
	SET JAVA_HOME=%JDK32%
)

IF NOT EXIST "%ProgramFiles%\JetBrains" (
	echo.
	echo Warning: "%ProgramFiles%\JetBrains" not found, build may break!
	echo Make sure build is running in 32bit mode to fix path issues.
	echo.
)

@echo on
mvn clean package site

ENDLOCAL