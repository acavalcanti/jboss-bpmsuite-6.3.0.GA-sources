#!/bin/bash

scriptname=$(basename $0)
fatalmsg () 
{
    echo "$scriptname [ERROR]: $@"
    exit
}
infomsg () 
{
    echo "scriptname [INFO]: $@"
}

infomsg "Setting location of .m2 directory"
export MAVEN_OPTS="$MAVEN_OPTS -Duser.home=`pwd`"
infomsg "MAVEN_OPTS =" $MAVEN_OPTS

if [ "$1" = "clean" ]; then
    infomsg "Cleaning Installers . . ."
    actiondesc="Clean Installer"
    infomsg "Removing project's .m2 directory"
    rm -rf .m2
    infomsg "Removing runtime dependencies JARS"
    rm -rf src/main/resources/common-files/runtime-dependencies/*.jar
    infomsg "Removing Izpack Compiler JAR"
    rm -f src/main/resources/standalone-compiler/*
    infomsg "Removing FSW Zips"
    rm -rf fsw-installer/zips/
    infomsg "Removing DV Zips"
    rm -rf dv-installer/zips/
    infomsg "Removing BRMS Zips"
    rm -rf brms-installer/zips/
    infomsg "Removing BPMS Zips"
    rm -rf bpms-installer/zips/

    infomsg "Cleaning installer-commons"
    mvn clean
    infomsg "Cleaning fsw-installer"
    (cd fsw-installer; mvn clean)
    infomsg "Cleaning dv-installer"
    (cd dv-installer; mvn clean)
    infomsg "Cleaning brms-installer"
    (cd brms-installer; mvn clean)
    infomsg "Cleaning bpms-installer"
    (cd bpms-installer; mvn clean)
    infomsg "Finished cleaning all projects"

    if [ ! -z "$2" ] && [ -f "$2/pom.xml" ]; then
       infomsg "Cleaning IzPack . . ."
       actiondesc="$actiondesc and IzPack"

       infomsg "Cleaning Izpack"
       (cd $2; mvn clean)
       infomsg "Finished cleaning IzPack"
   fi

elif [ ! -z "$1" ] && [ -f "$1/pom.xml" ]; then
    actiondesc="Build IzPack and Installer Commons"

    infomsg "Creating target folder"
    mkdir target
    infomsg "Creating project .m2 folder"
    mkdir -p .m2/

    infomsg "Building IzPack"
    (cd $1; mvn install || fatalmsg "IzPack Build Failed")

    infomsg "Building Installer Commons"
    mvn install || fatalmsg "Installer Commons Build Failed"

    if [ ! -z "$2" ]; then
        actiondesc="Build IzPack Installer Commons and Installer $2"
        restOfArgs=${@:3:$(($#))}
        infomsg "Building Installer $2"
        case $2 in
            fsw ) (cd fsw-installer ; mvn install $restOfArgs || fatalmsg "Building $2 Failed") ;;
            dv  ) (cd dv-installer  ; mvn install $restOfArgs || fatalmsg "Building $2 Failed") ;;
            brms) (cd brms-installer; mvn install $restOfArgs || fatalmsg "Building $2 Failed") ;;
            bpms) (cd bpms-installer; mvn install $restOfArgs || fatalmsg "Building $2 Failed") ;;
        esac
        infomsg "Installer $2 has been built"
    fi

else
    actiondesc="Build Installer Commons"
    infomsg "Creating target folder"
    mkdir target
    infomsg "Creating project .m2 folder"
    mkdir -p .m2/

    infomsg "Building Installer Commons"
    mvn install || fatalmsg "Installer Commons Build Failed"

    if [ ! -z "$1" ]; then
        actiondesc="Build Installer Commons and Installer $1"
        restOfArgs=${@:2:$(($#))}
        infomsg "Building Installer $1"
        case $1 in
            fsw ) (cd fsw-installer ; mvn install $restOfArgs || fatalmsg "Building $2 Failed") ;;
            dv  ) (cd dv-installer  ; mvn install $restOfArgs || fatalmsg "Building $2 Failed") ;;
            brms) (cd brms-installer; mvn install $restOfArgs || fatalmsg "Building $2 Failed") ;;
            bpms) (cd bpms-installer; mvn install $restOfArgs || fatalmsg "Building $2 Failed") ;;
        esac
        infomsg "Installer $1 has been built"
    fi  
fi

infomsg "Completed action ('$actiondesc') successfully"
