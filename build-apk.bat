@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.2"
set "JDK17_HOME=%~dp0jdk-17\jdk-17.0.12+7"
set "ANDROID_PREFS_ROOT="
set "ANDROID_HOME=C:\Users\Stu\AppData\Local\Android\Sdk"
set "PATH=%JDK17_HOME%\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\build-tools\36.1.0;%PATH%"
echo Using JDK17 at: %JDK17_HOME%
echo ANDROID_HOME set to: %ANDROID_HOME%
echo Building APK...
"%JDK17_HOME%\bin\java.exe" -version
gradlew.bat assembleRelease
pause
