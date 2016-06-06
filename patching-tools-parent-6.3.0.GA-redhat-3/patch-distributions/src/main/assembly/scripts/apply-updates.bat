@ECHO OFF
rem Copyright 2015 JBoss by Red Hat
rem
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem      http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

rem Script used to execute the BRMS/BPM Suite client patching tool

setLocal enableExtensions enableDelayedExpansion

if exist %JAVA_HOME%\bin\java.exe (
    set "JAVA_BIN=%JAVA_HOME%\bin\java"
    echo Using Java binary found at %JAVA_HOME%\bin\java
) else (
    java 2> NUL
    if not "%ERRORLEVEL%"==9009 (
        set "JAVA_BIN=java"
        echo JAVA_HOME system variable not set, using Java binary found on Path
    ) else (
        echo.
        echo Error^^! Java installation not found on your system^^! Please install Java from http://www.java.com first.
        goto :end_process
    )
)

%JAVA_BIN% -Xms64m -Xmx512m -cp libs\*; -Dlogback.configurationFile=conf/logback.xml ${patcher.mainClass} %*

:end_process
    endLocal
    