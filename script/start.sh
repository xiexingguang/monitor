#!/bin/bash

APP_HOME=`dirname $0`/../
cd $APP_HOME
MAIN_CLASS=com.ec.monitor.app.AppMain
PROJECT_NAME=monitor
PROJECT_VERSION=0.0.1-SNAPSHOT

for arg in $*
do
    case "$arg" in
        "-nohup"|"-n")
            JS_NOHUP=1
            ;;
        "-i")
            ;;
        -*)
            echo "Invalid argument: "$arg
            exit 8
            ;;
        *)
            if [[ "$MAIN_ARG" = "" ]]; then
                MAIN_ARG=$arg
            else
                echo "Already specified the main argument: "$MAIN_ARG
                exit 8
            fi
            ;;
    esac
done

JAVA_OPTION="$JAVA_OPTION -Xms512m -Xmx512m -XX:PermSize=128m -XX:MaxPermSize=256m"
JAVA_OPTION="$JAVA_OPTION -Dlog4j.configurationFile=/ec/apps/${PROJECT_NAME}/config/log4j2.xml -Dec.file.encoding=latin1"

if [[ "$MAIN_ARG" = "" ]]; then
    GC_LOGFILE=${PROJECT_NAME}.gc.log
else
    GC_LOGFILE=${PROJECT_NAME}_${MAIN_ARG}.gc.log
fi

JAVA_OPTION="$JAVA_OPTION -Dfile.encoding=utf-8"
JAVA_OPTION="$JAVA_OPTION -verbosegc -XX:+PrintGCTimeStamps -Xloggc:./${GC_LOGFILE}"

CLASSPATH=$CLASSPATH:./lib/${PROJECT_NAME}-${PROJECT_VERSION}.jar

for jarpath in `ls ./lib/*.jar`
do
   if [ './lib/'${PROJECT_NAME}'-'${PROJECT_VERSION}'.jar' != $jarpath  ]
   then
      CLASSPATH=$CLASSPATH:$jarpath
   fi
done

export CLASSPATH

nohup $JAVA_HOME/bin/java $JAVA_OPTION $MAIN_CLASS $MAIN_ARG > /dev/null 2>&1 &

RETURN_CODE=$?

if [ $RETURN_CODE != 0 ]; then
    echo ""
    echo "ERROR: Exit with non-zero code, "$RETURN_CODE
    exit $RETURN_CODE;
fi
