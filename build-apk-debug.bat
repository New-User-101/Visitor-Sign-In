@echo off
set JAVA_HOME=%~dp0jdk-17\jdk-17.0.12+7
set ANDROID_PREFS_ROOT=
set PATH=%JAVA_HOME%\bin;%PATH%
echo JAVA_HOME set to: %JAVA_HOME%
echo Building APK with stacktrace...
gradlew.bat assembleRelease --stacktrace
pause
