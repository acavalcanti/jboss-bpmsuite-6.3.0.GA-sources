#!/bin/sh
# Copyright 2015 JBoss by Red Hat
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Script used to execute the BRMS/BPM Suite client patching tool

# check if there is a Java installation available
if [ ! -z "$JAVA_HOME" -a -f "$JAVA_HOME/bin/java" ]; then
   echo "Using Java binary found at $JAVA_HOME/bin/java"
   JAVA_BIN=$JAVA_HOME/bin/java
else
    java -version 2> /dev/null
    if [ $? -ne 0 ]; then
        echo "Error! Java installation not found on your system! Please install Java from http://www.java.com first."
        echo ""
        exit -1
    fi
    echo "JAVA_HOME system variable not set, using Java binary found on PATH"
    JAVA_BIN=java
fi

${JAVA_BIN} -Xms64m -Xmx512m -cp "libs/*" -Dlogback.configurationFile="conf/logback.xml" ${patcher.mainClass} "$@"
