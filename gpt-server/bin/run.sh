#!/bin/bash
# Spring-Boot常规启动脚本，支持 启动/停止/重启/查看状态
# 使用脚本执行权限sudo chmod +x ./脚本文件.sh
# DEBUG模式启动：sh 脚本文件 [start|restart] debug

# 自动查找jar包名，删除后缀jar
APP_NAME=`ls -t |grep .jar$ |head -n1 | sed 's/\.[^.]*$//'`

# logs文件下日志文件名
LOG_FILE_NAME="info.log"

# 等待程序关闭时间 15s
KILL_MAX=15
# 日志等待时间
LOG_DELAY=5

# 备份目录和后缀
BACKUP_PATH="backup"
BACKUP_SUFFIX=`date +%y%m%d`

JAR_NAME=$APP_NAME\.jar
APP_HOME=`pwd`
LOG_PATH=$APP_HOME/logs
GC_LOG_PATH=$LOG_PATH/gc
LOG_FILE=$LOG_PATH/$LOG_FILE_NAME
DEBUG_FLAG=$2

########### jvm 参数 #############
# 基础参数, server模式加载慢运行快，如无特殊需求，推荐只配置堆+元空间
SERVICE_OPTS="-server -Xms2g -Xmx4g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m"
# THIN 参数 spring-boot-thin-launcher 打包使用,未使用请去掉 --离线模式阿里云依赖无法获取offline
THIN_ARGS="-Dthin.root=. -Dthin.offline=false"
# GC日志参数
GC_LOG_OPTS="-XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:$GC_LOG_PATH/gc-%t.log"
# 发生OOM时dump内存，并存储到指定文件
DUMP_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_PATH}/java.hprof"
# JVM DEBUG参数，用于调试，默认不开启
# ClassLoader和Method Compile日志，用于调试
COMPILE_LOADER_OPTS="-XX:+TraceClassLoading -XX:+TraceClassUnloading -XX:-PrintCompilation"
# 远程调试参数
REMOTE_DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
# DEBUG参数
DEBUG_OPTS="$COMPILE_LOADER_OPTS $REMOTE_DEBUG_OPTS"
# 至于Garbage Collector，虽然Java8已经支持G1了，但是不一定必须用，CMS在默认场景下也是一个优秀的回收器
# 对年轻代采用多线程并行回收，这样收得快
GC_OPTS="-XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
# JVM 启动参数
JVM_OPTIONS="${SERVICE_OPTS} ${THIN_ARGS} ${GC_LOG_OPTS} ${DUMP_OPTS} ${GC_OPTS}"

########### 高亮打印内容 #############
JAR_ECHO="\033[34m$JAR_NAME\033[0m"
CTRLC_ECHO="\033[34mCTRL+C\033[0m"
START_ECHO="\033[34mstop\033[0m"
STOP_ECHO="\033[34mstart\033[0m"
RESTART_ECHO="\033[34mrestart\033[0m"
STATUS_ECHO="\033[34mstatus\033[0m"
LOG_ECHO="\033[34mlog\033[0m"
DEBUG_ECHO="\033[34mdebug\033[0m"

# 确认jar包名称
if [ ! $APP_NAME ]; then
    echo -e "\033[31m--- 未查找到jar，请确认jar包存在当前目录 ---\033[0m"
    exit 0
fi

# 生成日志目录
if [ ! -d $LOG_PATH ]; then
    mkdir $LOG_PATH
fi

if [ ! -d $GC_LOG_PATH ]; then
    mkdir $GC_LOG_PATH
fi

#使用说明，用来提示输入参数
usage() {
    echo -e "使用方式: \033[34m$0\033[0m [${START_ECHO}|${STOP_ECHO}|${RESTART_ECHO}|${STATUS_ECHO}|${LOG_ECHO}] ${DEBUG_ECHO}"
    echo -e "[${START_ECHO}]启动当前目录jar包"
    echo -e "[${STOP_ECHO}]停止已启动的jar"
    echo -e "[${RESTART_ECHO}]重启已启动的jar"
    echo -e "[${STATUS_ECHO}]查看程序运行状态"
    echo -e "[${LOG_ECHO}]打印日志"
    echo -e "[${DEBUG_ECHO}]传入则启动时打开DEBUG模式"
    echo -e "启动示例:\033[34m $0 start \033[0m"
    exit 1
}

#检查程序是否在运行
is_exist(){
    pid=`ps -ef|grep $APP_HOME/$JAR_NAME|grep -v grep|awk '{print $2}' `
    #如果不存在返回1，存在返回0
    if [ -z "${pid}" ]; then
        return 1
    else
        return 0
    fi
}

#启动方法
start(){
    echo -e "--- 开始启动程序 ${JAR_ECHO} ---"
    is_exist
    if [ $? -eq "0" ]; then
        echo -e "--- ${JAR_ECHO} 运行中，PID=${pid} ---"
    else
        if [ "$DEBUG_FLAG" = "debug" ]; then
            JVM_OPTIONS="$JVM_OPTIONS $DEBUG_OPTS"
            echo -e "\033[33m--- 当前正在调试模式下运行！ 此模式可实现远程调试、打印、编译等信息 ---\033[0m"
        fi
        echo -e "---------------- JVM参数: ------------------"
        echo -e "\033[34m$JVM_OPTIONS\033[0m"
        echo -e "--------------------------------------------"
        if [ "$DEBUG_FLAG" = "debug" ]; then
            nohup java -jar $JVM_OPTIONS $APP_HOME/$JAR_NAME >nohup.out 2>&1 &
            LOG_FILE="$APP_HOME/nohup.out"
        else
            nohup java -jar $JVM_OPTIONS $APP_HOME/$JAR_NAME >/dev/null 2>&1 &
        fi
        echo -e "\033[32m--- 启动 $JAR_NAME 成功，PID = $! ---\033[0m"
        tailLog
    fi
}

# 打印日志
tailLog() {
    if [ ! -f "$LOG_FILE" ]; then
        echo -e "\033[33m--- 日志文件不存在,请确认日志路径 => ${LOG_FILE} ---\033[0m"
        exit 0
    fi
    echo -e "--- ${LOG_DELAY}秒后开始打印日志，按${CTRLC_ECHO}即可退出日志 ---"
    sleep $LOG_DELAY
    tail -f $LOG_FILE
}

#停止方法
stop(){
    echo -e "--- 开始关闭程序 ${JAR_ECHO} ---"
    is_exist
    if [ $? -eq "0" ]; then
        echo "--- 程序 PID = $pid ，开始关闭进程 $pid ---"
        kill $pid
        rm -rf $PID
        sleep 2
        pidCount=`ps -ef | grep "$APP_HOME/$JAR_NAME" | grep -vc grep`
        sleepCount=0
        echo "--- 程序关闭中，请稍等... ---"
        while [ $pidCount -ne 0 ]
        do
            sleep 1
            sleepCount=`expr $sleepCount + 1`
            if [ $sleepCount -gt $KILL_MAX ]; then
                mStop
            fi
            pidCount=`ps -ef | grep "$APP_HOME/$JAR_NAME" | grep -vc grep`
            echo -n ">>"
        done
        echo ">>"
        echo -e "\033[32m--- ${JAR_NAME} 进程已关闭 ---\033[0m"
    else
        echo -e "--- ${JAR_ECHO} 未运行 ---"
    fi
}

# 强制关闭
mStop() {
    is_exist
    if [ $? -eq "0" ]; then
        echo ">>"
        echo -e "\033[33m--- ${KILL_MAX}秒程序仍未关闭，开始强制关闭：kill -9 $pid ---\033[0m"
        kill -9  $pid
        sleep 2
    fi
}

#输出运行状态
status(){
    is_exist
    if [ $? -eq "0" ]; then
        echo -e "--- ${JAR_ECHO} 运行中，PID是：${pid} ---"
        echo -e "--- ${LOG_DELAY}秒后开始打印 jvm 概要信息，按${CTRLC_ECHO}即可退出 ---"
        sleep $LOG_DELAY
        jmap -heap ${pid}
    else
        echo -e "--- ${JAR_ECHO} 未运行 ---"
    fi
}

#打印日志
log(){
    is_exist
    if [ $? -eq "0" ]; then
        echo -e "--- ${JAR_ECHO} 运行中，PID是：${pid} ---"
       if [ ! -f "$LOG_FILE" ]; then
             echo -e "\033[33m--- 日志文件不存在,请确认日志路径 => ${LOG_FILE} ---\033[0m"
             exit 0
         fi
         echo -e "--- 开始打印日志，按${CTRLC_ECHO}即可退出日志 ---"
         tail -f $LOG_FILE
    else
        echo -e "--- ${JAR_ECHO} 未运行 ---"
    fi
}

#重启
restart(){
    echo -e "--- 开始重启程序 ${JAR_ECHO} ---"
    backup
    stop
    start
}

backup() {
    echo -e "--- 开始备份 ${JAR_ECHO} ---"
    if [ ! -d $BACKUP_PATH ];then
        mkdir $BACKUP_PATH
    fi
    
    backupName="$BACKUP_PATH/$JAR_NAME.$BACKUP_SUFFIX"
    if [ -f $backupName ];then
        echo -e "\033[33m--- $backupName 备份文件已存在 ---\033[0m"
    else
        cp "$JAR_NAME" "$backupName"
        echo -e "\033[32m--- jar包已备份至 -> $backupName ---\033[0m"
    fi
}

#根据输入参数，选择执行对应方法，不输入则执行使用说明
case "$1" in
    "start")
        start
    ;;
    "stop")
        stop
    ;;
    "status")
        status
    ;;
   "log")
          log
      ;;
    "restart")
        restart
    ;;
    *)
        usage
    ;;
esac

exit 0
