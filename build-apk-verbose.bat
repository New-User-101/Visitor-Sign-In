@echo off
set "JAVA_HOME=%~dp0jdk-17\jdk-17.0.12+7"
set "ANDROID_PREFS_ROOT="
set "ANDROID_HOME=C:\Users\Stu\AppData\Local\Android\Sdk"
set "PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\build-tools\36.1.0;%PATH%"
echo JAVA_HOME set to: %JAVA_HOME%
echo ANDROID_HOME set to: %ANDROID_HOME%
echo Building APK...
gradlew.bat assembleRelease --stacktrace --info > build.log 2>&1
echo Build completed. Check build.log for details.
type build.log
pause
